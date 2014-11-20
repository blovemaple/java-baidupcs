package blove.baidupcs.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 带有缓存的文件读写工具。<br>
 * 每个使用者提供FileAccessor，使用一个AccessorView。FileAccessor提供给此类用于实际访问文件系统的文件，
 * AccessorView向使用者提供此类的功能。所有使用者共用一个读缓存，每个使用者独用自己的写缓存。<br>
 * 缓存有两个容量，分别是缓存总容量和写缓存容量，由构造方法的参数设定，写缓存容量不能超过缓存总容量。缓存总大小等于读缓存和各个使用者的写缓存大小之总和，
 * 写缓存大小等于各个使用者的写缓存大小之和
 * 。当缓存总大小到达缓存总容量时，最长时间未访问的读缓存会被清除；当写缓存大小到达写缓存容量时，写缓存最大的使用者的写缓存会被写出。
 * 
 * @author blove
 */
// TODO 监控文件变化，清除有变化的文件的读缓存。但要注意，自己的修改不要导致读缓存删除。
public class CachedFileAccessor {
	private long totalCacheLimit;
	private long writeCacheLimit;
	private int readMinSize;

	/**
	 * 所有View公用的读缓存。
	 */
	private final Map<Path, Set<CacheItem>> readCache = Collections
			.synchronizedMap(new HashMap<>());

	/**
	 * 现有的views。view的逻辑保证当view有写缓存时，即存在于此集合中，否则不存在于此集合中。另外，此集合使用弱引用，
	 * 以保证view中存在写缓存但使用者将其废弃的情况下，该view可以随垃圾回收自动从此集合中移除。
	 */
	private final Set<AccessorView> currentViews = Collections
			.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	/**
	 * 创建一个实例。
	 * 
	 * @param totalCacheSize
	 *            缓存总容量
	 * @param writeCacheLimit
	 *            写缓存容量
	 * @param readMinSize
	 *            一次实际读取的最小字节数
	 * @throws IllegalArgumentException
	 *             任何一个容量为负数，或写缓存容量大于缓存总容量
	 */
	public CachedFileAccessor(long totalCacheSize, long writeCacheLimit,
			int readMinSize) {
		if (totalCacheSize < 0 || writeCacheLimit < 0)
			throw new IllegalArgumentException("Cache size cannot be negative.");
		if (writeCacheLimit > totalCacheSize)
			throw new IllegalArgumentException(
					"Write cache size cannot be greater than total cache size.");

		this.totalCacheLimit = totalCacheSize;
		this.writeCacheLimit = writeCacheLimit;
		this.readMinSize = readMinSize > 0 ? readMinSize : 0;
	}

	/**
	 * 新建一个指定路径文件的缓存视图。使用者通过此方法返回的视图使用此类提供的功能。
	 * 
	 * @param path
	 *            路径
	 * @param fileAccessor
	 *            文件访问工具
	 * @return 视图实例
	 */
	public AccessorView newView(Path path, FileAccessor fileAccessor) {
		return new AccessorView(path, fileAccessor);
	}

	/**
	 * 从指定缓存item集合中查找包含指定范围内任意字节的item。
	 * 
	 * @param source
	 *            缓存item集合。如果为null则视为没找到。
	 * @param startIndex
	 *            起始索引
	 * @param size
	 *            范围大小
	 * @return 此范围相关的item集合。如果没找到则返回空集合。
	 */
	private static Set<CacheItem> findItemsInRange(Set<CacheItem> source,
			long startIndex, long size) {
		if (source == null || source.isEmpty())
			return Collections.emptySet();
		return source
				.stream()
				.filter(item -> item.startIndex < startIndex + size
						&& item.startIndex + item.bytes.length > startIndex)
				.sorted().collect(Collectors.toSet());
	}

	/**
	 * 从指定缓存item集合中查找包含指定位置的item，如果不存在则查找指定位置之后最近的item，如果再不存在则返回null。
	 * 
	 * @param source
	 *            缓存item集合。如果为null则视为没找到。
	 * @param position
	 *            查找位置
	 * @return 见方法描述
	 */
	private static CacheItemForPosition findItemForPosition(
			Set<CacheItem> source, long position) {
		if (source == null || source.isEmpty())
			return null;
		Optional<CacheItem> foundItem = source.stream()
				.filter(item -> item.startIndex + item.bytes.length > position)
				.min(Comparator.naturalOrder());
		if (!foundItem.isPresent())
			return null;
		CacheItem item = foundItem.get();
		return new CacheItemForPosition(position,
				(int) (position - item.startIndex), item);
	}

	/**
	 * 计算指定缓存item集合的总大小。
	 * 
	 * @param items
	 *            缓存item集合
	 * @return 总大小
	 */
	private static long countItemsSize(Collection<CacheItem> items) {
		if (items == null || items.isEmpty())
			return 0;
		return items.parallelStream().mapToLong(item -> item.bytes.length)
				.sum();
	}

	/**
	 * 判断指定缓存item集合是否全是空的。
	 * 
	 * @param items
	 *            缓存item集合
	 * @return
	 */
	private static boolean isItemsEmpty(Collection<CacheItem> items) {
		if (items == null || items.isEmpty())
			return true;
		return items.parallelStream().allMatch(item -> item.bytes.length == 0);
	}

	/**
	 * 检查写缓存总大小。如果写缓存大小到达写缓存容量，将写缓存最大的若干使用者的写缓存写出。
	 */
	private synchronized void checkWriteCacheSize() {
		// 检查是否超出以及超出的字节数
		long size = currentViews.parallelStream()
				.mapToLong(view -> countItemsSize(view.writeCache)).sum();
		final AtomicLong beyondSize = new AtomicLong(size - writeCacheLimit);// 用AtomicLong是因为下面要在Lambda表达式里用，需要是final的
		if (beyondSize.get() <= 0)
			// 没超出限制
			return;

		// 超出限制了，flush一部分读缓存
		currentViews.parallelStream()
				// 计算所有view的写缓存大小
				.collect(
						Collectors.toMap(
								view -> countItemsSize(view.writeCache),
								view -> view)).entrySet().parallelStream()
				// 从大到小排序
				.sorted(Comparator.comparingLong(entry -> -entry.getKey()))
				// 找出写缓存最大的若干个，直到写缓存总大小不超限制
				.forEachOrdered(entry -> {
					if (beyondSize.getAndAdd(-entry.getKey()) > 0) {
						// TODO 异常因为在lambda表达式里，未处理
						try {
							entry.getValue().flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * 检查缓存总大小。如果缓存总大小超过缓存总容量，清除最长时间未访问的读缓存。
	 */
	private synchronized void checkTotalCacheSize() {
		// 检查是否超出以及超出的字节数
		long size = Stream
				.concat(currentViews.parallelStream().map(
						view -> view.writeCache),
						readCache.values().parallelStream())
				.mapToLong(items -> countItemsSize(items)).sum();
		final AtomicLong beyondSize = new AtomicLong(size - totalCacheLimit);// 用AtomicLong是因为下面要在Lambda表达式里用，需要是final的
		if (beyondSize.longValue() <= 0)
			// 没超过限制
			return;

		// 超出限制了，删除一部分读缓存
		readCache.values().parallelStream().flatMap(items -> items.stream())
				.sorted(Comparator.comparingLong(item -> item.lastAccessTime))
				.forEachOrdered(item -> {
					if (beyondSize.getAndAdd(-item.bytes.length) > 0) {
						readCache.get(item.path).remove(item);
					}
				});

		// 不可能出现读缓存全清空还超限制的情况，因为总缓存限制一定不小于写缓存限制
	}

	/**
	 * 缓存项目，即文件中的一段数据。
	 * 
	 * @author blove
	 */
	public static class CacheItem implements Comparable<CacheItem> {
		Path path;
		long startIndex;
		byte[] bytes;
		long lastAccessTime;// 只有读缓存有用，所以写缓存没有更新此时间

		CacheItem(Path path, long startIndex, byte[] bytes) {
			this.path = path;
			this.startIndex = startIndex;
			this.bytes = bytes;
			this.lastAccessTime = System.currentTimeMillis();
		}

		public long getStartIndex() {
			return startIndex;
		}

		public byte[] getBytes() {
			return bytes;
		}

		void refreshTime() {
			lastAccessTime = System.currentTimeMillis();
		}

		@Override
		public int compareTo(CacheItem o) {
			return this.startIndex < o.startIndex ? -1
					: this.startIndex == o.startIndex ? 0 : 1;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + (int) (startIndex ^ (startIndex >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheItem other = (CacheItem) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (startIndex != other.startIndex)
				return false;
			return true;
		}
	}

	/**
	 * {@link #getItemForPosition}方法的返回值，表示查找指定位置缓存item的结果。
	 * 
	 * @author blove
	 */
	private static class CacheItemForPosition {
		CacheItemForPosition(long forPosition, int offset, CacheItem item) {
			this.forPosition = forPosition;
			this.offset = offset;
			this.item = item;
		}

		/**
		 * 查找的位置。
		 */
		@SuppressWarnings("unused")
		long forPosition;
		/**
		 * 查找的位置在item中的偏移量，如果为负数则表示item起始位置在查找位置之后。
		 */
		int offset;
		/**
		 * 查到的item。
		 */
		CacheItem item;
	}

	/**
	 * 使用者使用的视图。这里“视图”的意思是共用的读缓存的视图，而写缓存是独用的。
	 * 
	 * @author blove
	 */
	public class AccessorView {
		private Set<CacheItem> writeCache = new HashSet<>();

		private final Path path;
		private final FileAccessor fileAccessor;

		public AccessorView(Path path, FileAccessor fileAccessor) {
			this.path = path;
			this.fileAccessor = fileAccessor;
		}

		public int read(long position, ByteBuffer dst) throws IOException {
			int readCount = 0;

			// 从写缓存中搞出这一段相关的item
			Set<CacheItem> wCacheItems = findItemsInRange(writeCache, position,
					dst.remaining());

			// 从读缓存中搞出这一段相关的item
			Set<CacheItem> rCacheItems = findItemsInRange(readCache.get(path),
					position, dst.remaining());

			// 开始往buffer里装
			while (dst.hasRemaining()) {
				long crtPosition = position + readCount;

				// 先在写缓存里找
				CacheItemForPosition wCacheItem = findItemForPosition(
						wCacheItems, crtPosition);
				if (wCacheItem != null && wCacheItem.offset >= 0) {
					readCount += readFromCache(wCacheItem.item,
							wCacheItem.offset, dst);
					continue;
				}

				// 写缓存没有，在读缓存里找
				CacheItemForPosition rCacheItem = findItemForPosition(
						rCacheItems, crtPosition);
				if (rCacheItem != null && rCacheItem.offset >= 0) {
					readCount += readFromCache(rCacheItem.item,
							rCacheItem.offset, dst);
					continue;
				}

				// 读缓存里也没有，实际读入并插入读缓存
				// 需要读取的字节数等于写缓存（如果存在）缺少的、读缓存（如果存在）缺少的、buffer剩余空间中的最小者
				int needReadSize = dst.remaining();
				if (wCacheItem != null)
					needReadSize = Math.min(needReadSize, -wCacheItem.offset);
				if (rCacheItem != null)
					needReadSize = Math.min(needReadSize, -rCacheItem.offset);
				int maxReadSize = Integer.MAX_VALUE;
				if (rCacheItem != null)
					maxReadSize = Math.min(maxReadSize, -rCacheItem.offset);
				rCacheItem = readToCache(crtPosition, needReadSize, maxReadSize);
				if (rCacheItem != null)
					readCount += readFromCache(rCacheItem.item,
							rCacheItem.offset, dst);
				else
					// 读不到，说明已到文件末尾
					break;
			}
			return readCount;
		}

		/**
		 * 将指定的读缓存item内容读到指定ByteBuffer，并且更新缓存时间。
		 * 
		 * @param item
		 *            缓存item
		 * @param offset
		 *            开始读的偏移量
		 * @param buffer
		 *            ByteBuffer
		 * @return 读的字节数
		 */
		private int readFromCache(CacheItem item, int offset, ByteBuffer buffer) {
			int putSize = Math.min(item.bytes.length - offset,
					buffer.remaining());
			buffer.put(item.bytes, offset, putSize);
			item.refreshTime();
			return putSize;
		}

		public void write(long position, ByteBuffer src) {
			int writeCount = 0;

			// 从写缓存中搞出这一段相关的item
			Set<CacheItem> existedItems = findItemsInRange(writeCache,
					position, src.remaining());

			// 开始往写缓存里装
			List<CacheItem> newItems = new LinkedList<>();
			while (src.hasRemaining()) {
				long crtPosition = position + writeCount;

				// 在写缓存里找
				CacheItemForPosition cacheItem = findItemForPosition(
						existedItems, crtPosition);
				if (cacheItem != null && cacheItem.offset >= 0) {
					// 找到了，更新写缓存
					int getSize = Math.min(cacheItem.item.bytes.length
							- cacheItem.offset, src.remaining());
					src.get(cacheItem.item.bytes, cacheItem.offset, getSize);
					writeCount += getSize;
				} else {
					// 没找到，记下要添加的写缓存新item
					int newSize = src.remaining();
					if (cacheItem != null)
						newSize = Math.min(newSize, -cacheItem.offset);
					byte[] newCacheBytes = new byte[newSize];
					src.get(newCacheBytes);
					newItems.add(new CacheItem(path, crtPosition, newCacheBytes));
					writeCount += newSize;
				}
			}

			// 把新item写入写缓存
			if (!newItems.isEmpty()) {
				writeToCache(newItems);
			}
		}

		public void truncate(long size) throws IOException {
			// 先从写缓存中搞出size之内相关的item
			Set<CacheItem> cacheItems = findItemsInRange(writeCache, 0, size);

			// 如果最后一个item超出了size的范围，将其缩小
			if (!cacheItems.isEmpty()) {
				CacheItem lastItem = Collections.max(cacheItems);
				if (lastItem.startIndex + lastItem.bytes.length > size) {
					byte[] newBytes = new byte[(int) (size - lastItem.startIndex)];
					System.arraycopy(lastItem.bytes, 0, newBytes, 0,
							newBytes.length);
					CacheItem newItem = new CacheItem(path,
							lastItem.startIndex, newBytes);
					cacheItems.remove(lastItem);
					cacheItems.add(newItem);
				}
			}

			// 实际写出这些items
			writeOutAndUpdateReadCache(cacheItems);

			// 然后再实际truncate
			fileAccessor.truncate(size);

			// 清除写缓存
			writeCache.clear();
			currentViews.remove(AccessorView.this);

			// 最后，把读缓存中truncate掉的相应部分删除
			// --找出读缓存中相应部分的items
			Set<CacheItem> rCacheItems = findItemsInRange(readCache.get(path),
					size, Long.MAX_VALUE);
			if (!rCacheItems.isEmpty()) {
				// --如果第一个在size范围之内，只将其超过部分删除
				CacheItem firstItem = Collections.min(rCacheItems);
				if (firstItem.startIndex < size) {
					byte[] newBytes = new byte[(int) (size - firstItem.startIndex)];
					System.arraycopy(firstItem.bytes, 0, newBytes, 0,
							newBytes.length);
					firstItem.bytes = newBytes;
					rCacheItems.remove(firstItem);
				}
				// --删除该删除的items
				readCache.get(path).removeAll(rCacheItems);
			}
		}

		public void flush() throws IOException {
			// 把写缓存中的全部写出
			writeOutAndUpdateReadCache(writeCache);
		}

		/**
		 * 读入指定位置指定字节数以上的数据，存到读缓存。处理超出总容量的情况。
		 * 
		 * @param position
		 *            位置
		 * @param minSize
		 *            最小字节数
		 * @param maxSize
		 *            最大字节数
		 * @return 包含指定位置的缓存item。如果没有，返回null。
		 * @throws IOException
		 */
		private CacheItemForPosition readToCache(long position, int minSize,
				int maxSize) throws IOException {
			int readSize = Math.min(Math.max(minSize, readMinSize), maxSize);
			byte[] bytes = fileAccessor.read(position, readSize);
			CacheItem newItem = new CacheItem(path, position, bytes);

			Set<CacheItem> rCacheItems = readCache.get(path);
			if (rCacheItems == null) {
				rCacheItems = new TreeSet<>();
				readCache.put(path, rCacheItems);
			}
			rCacheItems.add(newItem);

			checkTotalCacheSize();

			return new CacheItemForPosition(position, 0, newItem);
		}

		/**
		 * 将指定的新items写入写缓存。处理超出写缓存容量或总容量的情况。
		 * 
		 * @param newItems
		 *            新items
		 */
		private void writeToCache(Collection<CacheItem> newItems) {
			writeCache.addAll(newItems);
			if (!isItemsEmpty(writeCache))
				currentViews.add(AccessorView.this);
			checkWriteCacheSize();
			checkTotalCacheSize();
		}

		/**
		 * 写出指定的items，将它们在写缓存中清除，并将读缓存中相关item更新（不更新时间）。
		 * 
		 * @param items
		 * @throws IOException
		 */
		private void writeOutAndUpdateReadCache(Set<CacheItem> items)
				throws IOException {
			if (items == null || items.isEmpty())
				return;

			// 写出items
			fileAccessor.write(items);

			// 在写缓存中清除
			writeCache.removeAll(items);
			if (isItemsEmpty(writeCache))
				currentViews.remove(AccessorView.this);

			// 更新读缓存
			Set<CacheItem> rCacheItems = readCache.get(path);
			if (rCacheItems != null && rCacheItems.isEmpty()) {
				rCacheItems.forEach(item -> {
					int crtIndex = 0;
					while (crtIndex < item.bytes.length) {
						CacheItemForPosition wItem = findItemForPosition(items,
								item.startIndex + crtIndex);
						if (wItem == null)
							break;
						if (wItem.offset < 0) {
							crtIndex += -wItem.offset;
							wItem.offset = 0;
						}
						int updateSize = Math.min(item.bytes.length - crtIndex,
								wItem.item.bytes.length - wItem.offset);
						System.arraycopy(wItem.item.bytes, wItem.offset,
								item.bytes, crtIndex, updateSize);
						crtIndex += updateSize;
					}
				});
			}
		}

	}

	/**
	 * 实际访问文件系统文件的工具，由使用者提供，以保证此类与具体文件系统解耦。
	 * 
	 * @author blove
	 */
	public interface FileAccessor {
		/**
		 * 从指定位置开始读取指定字节数。
		 * 
		 * @param startIndex
		 *            起始位置
		 * @param size
		 *            读取的字节数
		 * @return 读取到的字节数组。如果到达文件末尾，则返回直到文件末尾。如果起始位置在文件末尾，则返回空数组。
		 * @throws IOException
		 *             出现IO错误
		 */
		byte[] read(long startIndex, long size) throws IOException;

		/**
		 * 将指定的缓存item集合写出。
		 * 
		 * @param cacheItems
		 *            缓存item集合
		 * @throws IOException
		 *             出现IO错误
		 */
		void write(Collection<CacheItem> cacheItems) throws IOException;

		/**
		 * 截断到指定字节数。
		 * 
		 * @param size
		 *            截断到字节数
		 * @throws IOException
		 *             出现IO错误
		 */
		void truncate(long size) throws IOException;
	}
}
