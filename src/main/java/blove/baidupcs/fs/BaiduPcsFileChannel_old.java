package blove.baidupcs.fs;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsException;
import blove.baidupcs.api.error.BaiduPcsFileExistsException;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.request.OnDup;
import blove.baidupcs.api.response.FileMetaWithExtra1;
import blove.baidupcs.fs.util.Cache;

/**
 * @deprecated
 * 
 * @author blove
 */
public class BaiduPcsFileChannel_old implements SeekableByteChannel {
	/**
	 * 文件分块大小
	 */
	private static final int BLOCK_SIZE = 1024 * 1024 * 3;// 3MB

	// TODO 在每个目录下记录当前目录文件的分块信息

	/**
	 * 块内容缓存超时时间。
	 * 
	 * @see #fetchBlock(int, byte[])
	 */
	private static final int BLOCK_EXPIRE_TIME = 20000;

	private final BaiduPcs service;

	private final BaiduPcsPath path;
	private final String pathServiceStr;
	private final boolean readable, writable;
	private final boolean append;
	private final boolean deleteOnClose;
	private final boolean sync;

	private boolean isOpen = true;

	private long position = 0;

	private byte[] readBuffer, writeBuffer;// 读缓存和写缓存，分别都缓存一个块
	private long readBufferPos = -1, writeBufferPos = -1;// 读缓存和写缓存起始字节在整个文件中的位置。-1无效。
	private int readBlockSize, writeBlockSize = -1;// 读缓存和写缓存中的块大小
	private boolean writeBufModified = false;

	BaiduPcsFileChannel_old(BaiduPcsPath path, Set<? extends OpenOption> options) throws IOException {
		this.path = path;
		this.pathServiceStr = path.toServiceString();
		service = path.getFileSystem().getFileStore().getService();

		append = options.contains(APPEND);
		writable = options.contains(WRITE) || append;
		readable = options.contains(READ) || !writable;
		deleteOnClose = options.contains(DELETE_ON_CLOSE);
		sync = options.contains(SYNC) || options.contains(DSYNC);

		boolean truncate = options.contains(TRUNCATE_EXISTING);
		boolean createNew = options.contains(CREATE_NEW);
		boolean create = options.contains(CREATE);

		boolean newCreated = false;// 此文件是否是刚刚因选项指定而创建或截短
		FileMetaWithExtra1 meta = null;
		if (createNew | create) {
			try {
				service.upload(pathServiceStr, EMPTY_BYTES, OnDup.EXCEPTION);
				newCreated = true;
			} catch (BaiduPcsFileExistsException e) {
				if (createNew)
					throw new FileAlreadyExistsException(path.toString());
			}
		} else {
			if (!newCreated) {
				try {
					meta = fetchMeta();// 若文件不存在则会抛异常
				} catch (BaiduPcsFileNotExistsException e) {
					throw new NoSuchFileException(path.toString());
				}
				if (truncate) {
					if (meta.getSize() > 0) {
						service.upload(pathServiceStr, EMPTY_BYTES, OnDup.OVERWRITE);
						newCreated = true;
						meta = null;
					}
				}
			}
		}

		if (!newCreated && append) {
			if (meta == null)
				try {
					meta = fetchMeta();
				} catch (BaiduPcsFileNotExistsException e) {
					throw new NoSuchFileException(path.toString());
				}
			position = meta.getSize();
		}
	}

	/**
	 * 空字节数组。创建文件或截短到0字节时使用。
	 */
	private static final byte[] EMPTY_BYTES = new byte[0];
	/**
	 * 全0满块的MD5值。第一次上传全0满块时得到这个值，以后就不用再上传，直接用就行了。
	 */
	private static String FULL_ZERO_BLOCK_MD5;

	@Override
	public synchronized boolean isOpen() {
		return isOpen;
	}

	@Override
	public synchronized void close() throws IOException {
		if (deleteOnClose) {
			try {
				service.delete(pathServiceStr);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			flush();
		}
		isOpen = false;
	}

	@Override
	public synchronized int read(ByteBuffer dst) throws IOException {
		checkClosed();

		if (!readable)
			throw new NonReadableChannelException();

		if (!dst.hasRemaining())
			return 0;

		int readCount = 0;

		if (readBuffer == null) {
			readBuffer = new byte[BLOCK_SIZE];
			readBufferPos = -1;
		}

		while (dst.hasRemaining() && prepareReadBuffer()) {
			int readBufferStart = (int) (position - readBufferPos);
			int readSize = Math.min(readBlockSize - readBufferStart, dst.remaining());
			dst.put(readBuffer, readBufferStart, readSize);

			readCount += readSize;
			position += readSize;
		}

		return readCount;
	}

	@Override
	public synchronized int write(ByteBuffer src) throws IOException {
		checkClosed();

		if (!writable)
			throw new NonWritableChannelException();

		if (append)
			position = size();

		if (!src.hasRemaining())
			return 0;

		int writeCount = 0;

		if (writeBuffer == null) {
			writeBuffer = new byte[BLOCK_SIZE];
			writeBufferPos = -1;
		}

		while (src.hasRemaining()) {
			prepareWriteBuffer();

			int writeBufferStart = (int) (position - writeBufferPos);
			int writeSize = Math.min(src.remaining(), writeBuffer.length - writeBufferStart);
			src.get(writeBuffer, writeBufferStart, writeSize);

			writeBlockSize = Math.max(writeBlockSize, writeBufferStart + writeSize);

			writeCount += writeSize;
			position += writeSize;
			writeBufModified = true;
		}

		if (sync)
			flush();

		return writeCount;
	}

	@Override
	public synchronized long position() throws IOException {
		checkClosed();

		return position;
	}

	@Override
	public synchronized SeekableByteChannel position(long newPosition) throws IOException {
		checkClosed();

		if (newPosition < 0)
			throw new IllegalArgumentException("Position cannot be negative: " + newPosition);
		position = newPosition;
		return this;
	}

	@Override
	public long size() throws IOException {
		checkClosed();

		try {
			FileMetaWithExtra1 meta = fetchMeta();
			long onlineSize = meta.getSize();
			long bufNeedSize = writeBufferPos + writeBlockSize;
			return Math.max(onlineSize, bufNeedSize);
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		}
	}

	@Override
	public synchronized SeekableByteChannel truncate(long size) throws IOException {
		checkClosed();

		if (!writable)
			throw new NonWritableChannelException();

		if (size < 0)
			throw new IllegalArgumentException("Cannot truncate to negative size: " + size);

		if (position > size)
			position = size;

		FileMetaWithExtra1 meta;
		try {
			meta = fetchMeta();
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		}

		if (size == 0) {
			if (meta.getSize() > 0) {
				service.upload(pathServiceStr, EMPTY_BYTES, OnDup.OVERWRITE);
			}
			return this;
		} else if (size >= size())
			return this;

		List<String> oriMd5s = meta.getBlockList();

		int newBlockCount = (int) (size / BLOCK_SIZE);
		int bufBlockIndex = (int) (writeBufferPos / BLOCK_SIZE);

		List<String> newMd5s = new LinkedList<>();
		for (int i = 0; i < newBlockCount; i++) {
			String md5;
			if (i == newBlockCount) {
				// 最后一块
				int blockSize = (int) (size % BLOCK_SIZE);
				if (i == bufBlockIndex) {
					// 最后一块恰好是缓存块
					writeBlockSize = blockSize;
					md5 = uploadBlock(writeBuffer, writeBlockSize);
					writeBufModified = false;
				} else {
					// 最后一块是线上块
					if (blockSize == BLOCK_SIZE)
						md5 = oriMd5s.get(i);
					else {
						byte[] bytes = new byte[blockSize];
						fetchBlock(i, bytes);
						md5 = uploadBlock(bytes, blockSize);
					}
				}

			} else {
				if (i == bufBlockIndex) {
					// 非最后一块如果恰好是写缓存块，则将缓存写出并重置缓存
					md5 = uploadBlock(writeBuffer, writeBlockSize);
					writeBufModified = false;
				} else {
					// 非最后一块如果不是缓存块，则直接使用原有的块
					md5 = oriMd5s.get(i);
				}
			}
			// 添加块MD5值
			newMd5s.add(md5);
		}

		// 所有块准备完毕，创建新文件
		createFile(newMd5s);

		if (sync)
			flush();

		return this;
	}

	private static final int RETRY_SLEEP = 2000;// 获取元信息重试间隔
	private static final int RETRY_TIMES = 2;// 重试次数。不包括开始获取的那一次。

	/**
	 * 获取元信息。如果返回文件不存在会重试几次，因为有时候刚创建的文件会返回不存在（坑爹）。
	 * 
	 * @return
	 * @throws IOException
	 * @throws BaiduPcsException
	 */
	private FileMetaWithExtra1 fetchMeta() throws BaiduPcsException, IOException {
		FileMetaWithExtra1 meta = null;
		try {
			int retryCount = 0;
			while (meta == null) {
				try {
					meta = service.meta(pathServiceStr);
				} catch (BaiduPcsFileNotExistsException e) {
					if (retryCount < RETRY_TIMES) {
						TimeUnit.MILLISECONDS.sleep(RETRY_SLEEP);
						retryCount++;
					} else
						throw e;
				}
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return meta;
	}

	/**
	 * 将当前位置所在的块fetch到指定buffer中（如果恰好是写缓存块则直接读取之）。如果位置在当前缓存范围内或超出总大小则不fetch。
	 * 
	 * @return position是否在总大小范围内
	 * @throws IOException
	 */
	private boolean prepareReadBuffer() throws IOException {
		int blockIndex = (int) (position / BLOCK_SIZE);
		int writeBlockIndex = writeBufferPos >= 0 ? (int) (writeBufferPos % BLOCK_SIZE) : -1;

		if (blockIndex == writeBlockIndex) {
			System.arraycopy(writeBuffer, 0, readBuffer, 0, BLOCK_SIZE);
			readBlockSize = writeBlockSize;
			readBufferPos = BLOCK_SIZE * blockIndex;
			return true;
		} else {
			if (readBufferPos >= 0 && position >= readBufferPos && position < readBufferPos + readBlockSize)
				return true;

			// 需要从线上获取块

			try {
				FileMetaWithExtra1 meta = fetchMeta();
				if (position >= meta.getSize())
					return false;
				readBlockSize = fetchBlock(blockIndex, readBuffer);
				readBufferPos = BLOCK_SIZE * blockIndex;
				return true;
			} catch (BaiduPcsFileNotExistsException e) {
				return false;
			}

		}
	}

	/**
	 * 将当前位置所在的块fetch到写buffer中（如果恰好是读缓存块则直接读取之），如果位置在当前缓存范围内则不fetch，
	 * 如果位置超出总大小则填充之前所有字节为0。
	 * 
	 * @throws IOException
	 */
	private void prepareWriteBuffer() throws IOException {
		int blockIndex = (int) (position / BLOCK_SIZE);
		int readBlockIndex = readBufferPos >= 0 ? (int) (readBufferPos % BLOCK_SIZE) : -1;

		if (blockIndex == readBlockIndex) {
			System.arraycopy(readBuffer, 0, writeBuffer, 0, BLOCK_SIZE);
			writeBlockSize = readBlockSize;
		} else {
			if (writeBufferPos >= 0 && position >= writeBufferPos && position < writeBufferPos + BLOCK_SIZE)
				return;

			// 需要从线上获取块

			// 先flush未写出的缓存
			flush();

			try {
				FileMetaWithExtra1 meta = fetchMeta();
				int blockCount = meta.getBlockList().size();

				if (blockIndex < blockCount) {
					// 要请求的块下标不超过最后一块，直接获取
					writeBlockSize = fetchBlock(blockIndex, writeBuffer);
				} else {
					// 要请求的块下标超过最后一块，位置所在的块是新块
					Arrays.fill(writeBuffer, (byte) 0);
					writeBlockSize = 0;
				}
			} catch (BaiduPcsFileNotExistsException e) {
				throw new NoSuchFileException(path.toString());
			}
		}
		writeBufferPos = BLOCK_SIZE * blockIndex;
	}

	/**
	 * 块内容缓存。为了使key中有文件系统、路径、块号三个信息，key为path实例resolve块号。<br>
	 * 下载块时优先从缓存读取，更新文件时清理缓存中修改的对应块。
	 */
	static final Cache<Path, byte[]> blockCache = new Cache<>(BLOCK_EXPIRE_TIME);

	/**
	 * 将指定下标的块读入到指定字节数组中。块内容将被缓存下来，缓存超时时间为{@link #BLOCK_EXPIRE_TIME}。
	 * 
	 * @param index
	 * @param bytes
	 * @return 块大小
	 * @throws IOException
	 */
	private int fetchBlock(int index, byte[] bytes) throws IOException {
		int readCount = 0;

		Path cacheKey = path.resolve(Integer.toString(index));

		byte[] cacheBytes = blockCache.get(cacheKey);
		if (cacheBytes != null) {
			readCount = Math.min(cacheBytes.length, bytes.length);
			System.arraycopy(cacheBytes, 0, bytes, 0, readCount);
			System.out.println("MATCH CACHE FOR " + index);
		} else {
			try (InputStream in = service.download(pathServiceStr, BLOCK_SIZE * index, BLOCK_SIZE * (index + 1) - 1)
					.in()) {
				int readOnce = 0;
				int readIndex = 0;
				while (readCount < bytes.length) {
					readOnce = in.read(bytes, readIndex, bytes.length - readIndex);
					if (readOnce >= 0)
						readCount += readOnce;
					else
						break;
				}

				if (in.available() <= 0) {
					// 确定读的是一整块才缓存
					cacheBytes = new byte[readCount];
					System.arraycopy(bytes, 0, cacheBytes, 0, readCount);
					blockCache.put(cacheKey, cacheBytes);
					System.out.println("CACHE FOR " + index);
				}
			} catch (BaiduPcsFileNotExistsException e) {
				throw new NoSuchFileException(path.toString());
			}
		}
		return readCount;
	}

	/**
	 * 上传一个块。
	 * 
	 * @param bytes
	 *             字节数组
	 * @param size
	 *             大小
	 * @return MD5值
	 * @throws IOException
	 */
	private String uploadBlock(byte[] bytes, int size) throws IOException {
		byte[] uploadBytes;
		if (size < bytes.length) {
			uploadBytes = new byte[size];
			System.arraycopy(bytes, 0, uploadBytes, 0, size);
		} else
			uploadBytes = bytes;
		return service.uploadBlock(uploadBytes);
	}

	private void createFile(List<String> md5s) throws BaiduPcsException, IOException {
		List<String> oriMd5s = service.meta(pathServiceStr).getBlockList();
		service.createSuperFile(pathServiceStr, md5s, OnDup.OVERWRITE);

		for (int i = 0; i < oriMd5s.size(); i++) {
			if (md5s.size() <= i || !md5s.get(i).equals(oriMd5s.get(i))) {
				Path cacheKey = path.resolve(Integer.toString(i));
				blockCache.remove(cacheKey);
				System.out.println("CLEAR CACHE FOR " + i);
			}
		}
	}

	/**
	 * 写出写缓存中的内容。
	 * 
	 * @throws IOException
	 */
	private void flush() throws IOException {
		if (writeBufferPos < 0)
			return;
		if (writeBlockSize <= 0)
			return;
		if (!writeBufModified)
			return;

		try {
			FileMetaWithExtra1 meta = fetchMeta();
			List<String> md5s = new LinkedList<>(meta.getBlockList());
			int oriBlockCount = md5s.size();

			int blockIndex = (int) (writeBufferPos / BLOCK_SIZE);

			if (blockIndex >= md5s.size()) {
				// 要请求的块下标超过最后一块，要填充0

				// 如果最后一块不满，需要先填充最后一块
				if (meta.getSize() % BLOCK_SIZE > 0) {
					fetchBlock(oriBlockCount, writeBuffer);
					String md5 = uploadBlock(writeBuffer, BLOCK_SIZE);
					md5s.set(oriBlockCount, md5);
				}

				// 从最后一块的下一块，一直到位置所在块的前一块，都需要填充全0
				for (int i = oriBlockCount + 1; i < blockIndex; i++) {
					if (FULL_ZERO_BLOCK_MD5 == null) {
						byte[] fullZeroBytes = new byte[BLOCK_SIZE];
						FULL_ZERO_BLOCK_MD5 = uploadBlock(fullZeroBytes, BLOCK_SIZE);
					}

					md5s.add(FULL_ZERO_BLOCK_MD5);
				}
			}

			// 上传当前块
			String md5 = uploadBlock(writeBuffer, writeBlockSize);
			md5s.add(md5);

			// 所有块已经准备好，创建新文件
			createFile(md5s);

			// 重置缓存修改状态
			writeBufModified = false;

		} catch (BaiduPcsFileNotExistsException e) {
			throw new IOException(e);
		}
	}

	private void checkClosed() throws ClosedChannelException {
		if (!isOpen)
			throw new ClosedChannelException();
	}
}
