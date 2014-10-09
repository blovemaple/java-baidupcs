package blove.baidupcs.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 抽象的非默认文件系统Path。
 * 
 * @author blove
 * 
 */
abstract class AbstractNonDefaultFSPath implements Path {
	@Override
	public boolean startsWith(String other) {
		return startsWith(getFileSystem().getPath(other));
	}

	@Override
	public boolean endsWith(String other) {
		return endsWith(getFileSystem().getPath(other));
	}

	@Override
	public Path resolve(String other) {
		return resolve(getFileSystem().getPath(other));
	}

	@Override
	public Path resolveSibling(Path other) {
		if (other == null)
			throw new NullPointerException();
		Path parent = getParent();
		return (parent == null) ? other : parent.resolve(other);
	}

	@Override
	public Path resolveSibling(String other) {
		return resolveSibling(getFileSystem().getPath(other));
	}

	@Override
	public Iterator<Path> iterator() {
		return new Iterator<Path>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return (i < getNameCount());
			}

			@Override
			public Path next() {
				if (i < getNameCount()) {
					Path result = getName(i);
					i++;
					return result;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * 非默认文件系统，无对应的File实例。抛出{@link UnsupportedOperationException}。
	 * 
	 * @throws UnsupportedOperationException
	 * @see java.nio.file.Path#toFile()
	 */
	@Override
	public final File toFile() {
		throw new UnsupportedOperationException(
				"toFile is unsupported, because the the FileSystem is not the default file system.");
	}

	@Override
	public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
		return register(watcher, events, new WatchEvent.Modifier[0]);
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

}
