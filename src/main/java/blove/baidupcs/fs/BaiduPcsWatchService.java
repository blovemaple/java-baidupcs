package blove.baidupcs.fs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.response.FileMeta;

//XXX 经测试用服务提供的diff太慢了。。
//锁：从上到下、从外到内；三个加锁点：key列表、等待队列、key对象
public class BaiduPcsWatchService implements WatchService {

	/**
	 * 所有注册的key。
	 */
	private final List<BaiduPcsWatchKey> keys = new LinkedList<>();
	/**
	 * 在signal状态且尚未poll/take的key队列。key当被poll/take时移除。<br>
	 * 需要等待key的操作在此队列上等待。
	 */
	private final Queue<BaiduPcsWatchKey> eventingQueue = new LinkedList<>();

	private boolean open = true;

	/**
	 * 监视线程。
	 */
	private final WatchThread watchThread;

	BaiduPcsWatchService(BaiduPcsFileSystem fs) {
		watchThread = new WatchThread(fs);
		watchThread.start();
	}

	BaiduPcsWatchKey register(BaiduPcsPath dir, Kind<Path>[] events, Modifier... modifiers) throws IOException {
		checkClosed();
		if (dir == null)
			throw new NullPointerException();
		if (events.length == 0)
			throw new IllegalArgumentException("No events to register");
		synchronized (keys) {
			dir = dir.toAbsolutePath().normalize();
			BaiduPcsWatchKey key = new BaiduPcsWatchKey(dir, events);
			keys.add(key);
			return key;
		}
	}

	private class BaiduPcsWatchKey implements WatchKey {

		/**
		 * 被监视的目录。
		 */
		final BaiduPcsPath watchDir;
		/**
		 * 监视的事件类型。
		 */
		final List<Kind<Path>> kinds;

		/**
		 * 当前事件的列表。取出事件时复制并清空。
		 */
		final List<WatchEvent<Path>> events = new ArrayList<>();

		boolean valid;
		boolean signaled;

		// 以下两个字段被检查线程使用，存储上次检查的信息
		long lastCheckTime = 0;// 上次检查时间，<=0表示尚未进行第一次检查
		Set<BaiduPcsPath> lastCheckElements;// 上次检查获取的所有元素（仅fileName）

		BaiduPcsWatchKey(BaiduPcsPath dir, Kind<Path>[] kinds) {
			this.watchDir = dir;
			this.kinds = Arrays.asList(kinds);
			valid = true;
			signaled = false;
		}

		@Override
		public synchronized boolean isValid() {
			return valid;
		}

		@Override
		public synchronized List<WatchEvent<?>> pollEvents() {
			if (events.isEmpty())
				return Collections.emptyList();
			else {
				List<WatchEvent<?>> crtEvents = new ArrayList<>(this.events);
				this.events.clear();
				return crtEvents;
			}
		}

		@Override
		public synchronized boolean reset() {
			if (!valid)
				return false;

			if (signaled) {
				if (events.isEmpty())
					signaled = false;
				else
					signalAndEnqueue(this);
			}
			return true;
		}

		@Override
		public void cancel() {
			synchronized (BaiduPcsWatchService.this.keys) {
				synchronized (this) {
					keys.remove(this);
					valid = false;
				}
			}
		}

		@Override
		public Watchable watchable() {
			return watchDir;
		}

	}

	private static class BaiduPcsWatchEvent implements WatchEvent<Path> {
		private final WatchEvent.Kind<Path> kind;
		private final BaiduPcsPath context;

		private int count;

		BaiduPcsWatchEvent(WatchEvent.Kind<Path> type, BaiduPcsPath context) {
			this.kind = type;
			this.context = context;
			this.count = 1;
		}

		@Override
		public WatchEvent.Kind<Path> kind() {
			return kind;
		}

		@Override
		public BaiduPcsPath context() {
			return context;
		}

		@Override
		public int count() {
			return count;
		}

		void increment() {
			count++;
		}

		@Override
		public String toString() {
			return "BaiduPcsWatchEvent [\n\tkind=" + kind + "\n\tcontext=" + context + "\n\tcount=" + count + "\n]";
		}

	}

	/**
	 * 将指定key置为signal状态并加入等待队列。如果在signal状态则不重复添加。
	 * 
	 * @param key
	 */
	private void signalAndEnqueue(BaiduPcsWatchKey key) {
		if (!keys.contains(key))
			throw new IllegalArgumentException("Key is not registered: " + key);
		synchronized (eventingQueue) {
			synchronized (key) {
				if (key.signaled)
					return;
				key.signaled = true;
				eventingQueue.add(key);
				eventingQueue.notify();
			}
		}
	}

	@Override
	public WatchKey poll() {
		checkClosed();
		synchronized (eventingQueue) {
			return eventingQueue.poll();
		}
	}

	@Override
	public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
		checkClosed();
		synchronized (eventingQueue) {
			while (eventingQueue.isEmpty()) {
				eventingQueue.wait(unit.toMillis(timeout));
				checkClosed();
			}
			return eventingQueue.poll();
		}
	}

	@Override
	public WatchKey take() throws InterruptedException {
		synchronized (eventingQueue) {
			return poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if (!open)
			return;

		synchronized (keys) {
			synchronized (eventingQueue) {
				open = false;

				watchThread.interrupt();
				for (BaiduPcsWatchKey key : keys) {
					synchronized (key) {
						key.valid = false;
					}
				}
				keys.clear();
				eventingQueue.clear();
				eventingQueue.notifyAll();
			}
		}
	}

	/**
	 * 如果已关闭则抛出异常。
	 * 
	 * @throws ClosedWatchServiceException
	 */
	private void checkClosed() throws ClosedWatchServiceException {
		if (!open)
			throw new ClosedWatchServiceException();
	}

	/**
	 * 访问pcs检测文件更新的线程。
	 * 
	 * @author blove
	 */
	// TODO 尚未实现空闲时查询时间间隔增大
	private class WatchThread extends Thread {

		private final BaiduPcsFileSystem fs;
		private final BaiduPcs pcsService;

		private static final long CHECK_IDLE_TIME = 1000;// 遍历所有key的空闲时间（结束到起始）
		private static final long MIN_INTERVAL_TIME = 2000;// 单个目录查询最小间隔时间（起始到起始）

		private Object waitingObject = new Object();// 在此对象上等待

		WatchThread(BaiduPcsFileSystem fs) {
			setDaemon(true);
			this.fs = fs;
			this.pcsService = fs.getFileStore().getService();
		}

		@Override
		public void run() {
			try {
				while (true) {
					try {
						Set<BaiduPcsPath> dirs = new HashSet<>();
						long now = System.currentTimeMillis();

						// 遍历所有key，取出应检查的目录并去重
						synchronized (keys) {
							for (BaiduPcsWatchKey key : keys) {
								if (key.lastCheckTime <= 0 || now - key.lastCheckTime >= MIN_INTERVAL_TIME)
									dirs.add(key.watchDir);
							}
						}

						// 检查并记录结果，并记录目录不存在其key需要cancel的目录
						Map<BaiduPcsPath, Set<BaiduPcsPath>> dirAndElements = new HashMap<>();// 目录到元素（仅fileName）集合的映射
						Set<BaiduPcsPath> dirsToCancel = new HashSet<>();// 目录不存在，key需要cancel的目录
						for (BaiduPcsPath dir : dirs) {
							try {
								Set<BaiduPcsPath> elements = new HashSet<>();
								for (FileMeta file : pcsService.list(dir.toServiceString())) {
									elements.add(new BaiduPcsPath(fs, file.getFileName()));
								}
								dirAndElements.put(dir, elements);
							} catch (BaiduPcsFileNotExistsException e) {
								dirsToCancel.add(dir);
							}
						}

						// 遍历所有key，放置事件，更改key状态，cancel掉目录不存在的key
						// （为了简单，仅cancel上面检查时发现目录不存在的key。那些本次在其它key上检查到其目录删除的key，就等下次被检查时再cancel吧。）
						synchronized (keys) {
							List<BaiduPcsWatchKey> keysToCancel = new ArrayList<>();
							for (BaiduPcsWatchKey key : keys) {
								if (dirsToCancel.contains(key.watchDir))
									keysToCancel.add(key);
								else {
									Set<BaiduPcsPath> elements = dirAndElements.get(key.watchDir);
									if (elements != null)
										putEvents(key, elements, now);
								}
							}

							for (BaiduPcsWatchKey key : keysToCancel)
								key.cancel();
						}
					} catch (IOException e) {
						// XXX 检查时抛出异常
						e.printStackTrace();
					}

					synchronized (waitingObject) {
						waitingObject.wait(CHECK_IDLE_TIME);
					}
				}
			} catch (InterruptedException e) {
				// 监视线程中断，直接return
			}
		}

		private void putEvents(BaiduPcsWatchKey key, Set<BaiduPcsPath> elements, long checkTime) {
			System.out.println(elements);
			synchronized (key) {
				if (key.lastCheckTime > 0) {
					Set<BaiduPcsPath> deletedElements = new HashSet<>(key.lastCheckElements);
					deletedElements.removeAll(elements);
					Set<BaiduPcsPath> newElements = new HashSet<>(elements);
					newElements.removeAll(key.lastCheckElements);

					boolean hasNewEvents = false;
					if (key.kinds.contains(StandardWatchEventKinds.ENTRY_DELETE) && !deletedElements.isEmpty()) {
						for (BaiduPcsPath path : deletedElements)
							putOneEvent(key, path, StandardWatchEventKinds.ENTRY_DELETE);
						hasNewEvents = true;
					}
					if (key.kinds.contains(StandardWatchEventKinds.ENTRY_CREATE) && !newElements.isEmpty()) {
						for (BaiduPcsPath path : newElements)
							putOneEvent(key, path, StandardWatchEventKinds.ENTRY_CREATE);
						hasNewEvents = true;
					}

					if (hasNewEvents)
						signalAndEnqueue(key);
				}

				key.lastCheckElements = elements;
				key.lastCheckTime = checkTime;
			}
		}

		private void putOneEvent(BaiduPcsWatchKey key, BaiduPcsPath path, Kind<Path> kind) {
			BaiduPcsPath contextPath = path.getFileName();
			boolean repeatedEvent = false;
			for (WatchEvent<Path> event : key.events) {
				if (event.context().equals(contextPath) && event.kind().equals(kind)) {
					// 重复事件
					((BaiduPcsWatchEvent) event).increment();
					repeatedEvent = true;
					break;
				}
			}
			if (!repeatedEvent) {
				key.events.add(new BaiduPcsWatchEvent(kind, contextPath));
			}
		}
	}
}
