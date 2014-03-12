package blove.baidupcs.fs;

import static java.nio.file.StandardOpenOption.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.request.OnDup;
import blove.baidupcs.api.response.FileMeta;

public class BaiduPcsFileChannel implements SeekableByteChannel {

	private final BaiduPcs service;

	private final String pathServiceStr;

	private final boolean readable, writable;
	private final boolean append;
	private final boolean deleteOnClose;
	private final boolean sync;

	private boolean needCreate;

	private boolean isOpen = true;
	private Path cacheFilePath;
	private SeekableByteChannel cacheFileChannel;
	private boolean changed = false;

	BaiduPcsFileChannel(BaiduPcsPath path, Set<? extends OpenOption> options) throws IOException {
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
		needCreate = createNew || create;

		// 检查文件是否存在
		boolean alreadyExists;
		try {
			FileMeta meta = service.meta(pathServiceStr);
			// 没抛异常，文件存在
			if (meta.isDir())// 不可以是目录
				throw new IOException("It is a directory: " + path);
			alreadyExists = true;
		} catch (BaiduPcsFileNotExistsException e) {
			// 文件不存在
			alreadyExists = false;
		}

		// 如果指定了CREATE_NEW，则必须保证文件不存在
		if (createNew && alreadyExists)
			throw new FileAlreadyExistsException(path.toString());

		// 如果CREATE和CREATE_NEW都未指定，则必须保证文件存在
		if (!(create || createNew) && !alreadyExists)
			throw new NoSuchFileException(path.toString());

		// 准备缓存文件
		cacheFilePath = Files.createTempFile(null, null);
		if (!truncate) {
			try {
				try (InputStream in = service.download(pathServiceStr).in()) {
					Files.copy(in, cacheFilePath, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (BaiduPcsFileNotExistsException e) {
			}
		}
	}

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
		Files.deleteIfExists(cacheFilePath);
		cacheFilePath = null;
		cacheFileChannel = null;
		isOpen = false;
	}

	private void checkOpen() throws ClosedChannelException {
		if (!isOpen)
			throw new ClosedChannelException();
	}

	@Override
	public synchronized int read(ByteBuffer dst) throws IOException {
		checkOpen();
		if (!readable)
			throw new NonReadableChannelException();
		prepareCacheFileChannel();
		return cacheFileChannel.read(dst);
	}

	@Override
	public synchronized int write(ByteBuffer src) throws IOException {
		checkOpen();
		if (!writable)
			throw new NonWritableChannelException();
		prepareCacheFileChannel();
		if (append)
			cacheFileChannel.position(cacheFileChannel.size());
		int writeSize = cacheFileChannel.write(src);
		if (writeSize > 0) {
			changed = true;
			if (sync)
				flush();
		}
		return writeSize;
	}

	@Override
	public synchronized long position() throws IOException {
		checkOpen();
		if (!writable)
			throw new NonWritableChannelException();
		prepareCacheFileChannel();
		return cacheFileChannel.position();
	}

	@Override
	public synchronized SeekableByteChannel position(long newPosition) throws IOException {
		checkOpen();
		prepareCacheFileChannel();
		cacheFileChannel.position(newPosition);
		return this;
	}

	@Override
	public synchronized long size() throws IOException {
		checkOpen();
		prepareCacheFileChannel();
		return cacheFileChannel.size();
	}

	@Override
	public synchronized SeekableByteChannel truncate(long size) throws IOException {
		checkOpen();
		prepareCacheFileChannel();
		long oriSize = cacheFileChannel.size();
		cacheFileChannel.truncate(size);
		if (cacheFileChannel.size() != oriSize) {
			changed = true;
			if (sync)
				flush();
		}
		return this;
	}

	/**
	 * 确保cacheFileChannel准备好以供使用。
	 * 
	 * @throws IOException
	 */
	private void prepareCacheFileChannel() throws IOException {
		if (cacheFileChannel == null) {
			cacheFileChannel = Files
					.newByteChannel(cacheFilePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
			changed = false;
		}
	}

	/**
	 * 将未写出的数据写出。
	 * 
	 * @throws IOException
	 */
	private void flush() throws IOException {
		if ((cacheFileChannel != null && changed) || needCreate) {
			if (cacheFileChannel != null) {
				cacheFileChannel.close();
				cacheFileChannel = null;
			}

			try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(cacheFilePath,
					StandardOpenOption.READ))) {
				service.upload(pathServiceStr, in, Files.size(cacheFilePath), OnDup.OVERWRITE);
			}
			needCreate = false;
		}
	}

}
