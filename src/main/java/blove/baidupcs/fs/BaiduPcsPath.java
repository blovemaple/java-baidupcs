package blove.baidupcs.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BaiduPcsPath extends AbstractNonDefaultFSPath {
	private final BaiduPcsFileSystem fs;
	private final boolean absolute;
	private final List<String> items;

	BaiduPcsPath(BaiduPcsFileSystem fs, boolean absolute, List<String> items) {
		this.fs = fs;
		this.absolute = absolute;
		this.items = new ArrayList<>(items);
	}

	BaiduPcsPath(BaiduPcsFileSystem fs, String pathStr) {
		this.fs = fs;

		this.absolute = pathStr.startsWith(fs.getSeparator());

		List<String> items = new ArrayList<>();
		for (String item : pathStr.split(fs.getSeparator())) {
			if (!item.isEmpty())
				items.add(item);
		}
		this.items = items;
	}

	@Override
	public BaiduPcsFileSystem getFileSystem() {
		return fs;
	}

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public BaiduPcsPath getRoot() {
		return fs.getRootDirectory();
	}

	@Override
	public BaiduPcsPath getFileName() {
		if (items.isEmpty())
			return null;

		return new BaiduPcsPath(fs, false, Collections.singletonList(items
				.get(items.size() - 1)));
	}

	@Override
	public BaiduPcsPath getParent() {
		if (items.size() < 1)
			return null;
		if (!absolute && items.size() <= 1)
			return null;
		return new BaiduPcsPath(fs, absolute,
				items.subList(0, items.size() - 1));
	}

	@Override
	public int getNameCount() {
		return items.size();
	}

	@Override
	public Path getName(int index) {
		return new BaiduPcsPath(fs, false, Collections.singletonList(items
				.get(index)));
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		return new BaiduPcsPath(fs, false, items.subList(beginIndex, endIndex));
	}

	@Override
	public boolean startsWith(Path other) {
		if (!other.getFileSystem().equals(fs))
			return false;
		BaiduPcsPath otherPath = (BaiduPcsPath) other;
		if (otherPath.absolute != absolute)
			return false;
		if (items.isEmpty())
			return otherPath.items.isEmpty() && otherPath.absolute == absolute;
		if (otherPath.items.size() > items.size())
			return false;
		return items.subList(0, otherPath.items.size()).equals(otherPath.items);
	}

	@Override
	public boolean endsWith(Path other) {
		if (!other.getFileSystem().equals(fs))
			return false;
		BaiduPcsPath otherPath = (BaiduPcsPath) other;
		if (otherPath.items.size() > items.size())
			return false;
		if (otherPath.absolute) {
			if (absolute) {
				if (otherPath.items.size() != items.size())
					return false;
				return otherPath.items.equals(items);
			} else {
				return false;
			}
		} else {
			return otherPath.items.equals(items.subList(items.size()
					- otherPath.items.size(), items.size()));
		}
	}

	@Override
	public BaiduPcsPath normalize() {
		if (items.isEmpty())
			return this;

		int index = 0;
		List<String> newItems = new ArrayList<>(items);
		while (index < items.size()) {
			switch (items.get(index)) {
			case ".":
				items.remove(index);
				break;
			case "..":
				if (index > 0) {
					items.remove(index);
					if (index >= 1)
						items.remove(index - 1);
					index--;
					break;
				}
			default:
				index++;
			}
		}

		if (newItems.equals(items))
			return this;
		else
			return new BaiduPcsPath(fs, absolute, newItems);
	}

	@Override
	public BaiduPcsPath resolve(Path other) {
		if (!other.getFileSystem().equals(fs)) {
			throw new IllegalArgumentException(
					"Cannot resolve paths in different file systems.");
		}

		BaiduPcsPath otherPath = (BaiduPcsPath) other;
		if (otherPath.absolute)
			return otherPath;
		if (otherPath.items.isEmpty())
			return this;
		if (items.isEmpty()) {
			if (absolute)
				return new BaiduPcsPath(fs, true, otherPath.items);
			else
				return otherPath;
		} else {
			List<String> newItems = new ArrayList<>(items.size()
					+ otherPath.items.size());
			newItems.addAll(items);
			newItems.addAll(otherPath.items);
			return new BaiduPcsPath(fs, absolute, newItems);
		}
	}

	@Override
	public BaiduPcsPath relativize(Path other) {
		if (!other.getFileSystem().equals(fs)) {
			throw new IllegalArgumentException(
					"Cannot resolve paths in different file systems.");
		}

		BaiduPcsPath otherPath = (BaiduPcsPath) other;
		if (otherPath.absolute != absolute)
			throw new IllegalArgumentException(
					"Cannot relativize paths with different absolutenesses.");

		int firstDiffIndex;
		for (firstDiffIndex = 0; firstDiffIndex < items.size()
				&& firstDiffIndex < otherPath.items.size(); firstDiffIndex++) {
			if (!otherPath.items.get(firstDiffIndex).equals(
					items.get(firstDiffIndex)))
				break;
		}

		List<String> newItems = new ArrayList<>();
		for (int i = items.size() - 1; i >= firstDiffIndex; i--)
			newItems.add("..");
		for (int i = firstDiffIndex; i < otherPath.items.size(); i++)
			newItems.add(otherPath.items.get(i));

		return new BaiduPcsPath(fs, false, newItems);
	}

	@Override
	public URI toUri() {
		return fs.provider().getURI(this);
	}

	@Override
	public BaiduPcsPath toAbsolutePath() {
		if (absolute)
			return this;
		return new BaiduPcsPath(fs, true, items);
	}

	@Override
	public BaiduPcsPath toRealPath(LinkOption... options) throws IOException {

		BaiduPcsPath path = this;

		// 转换为绝对路径
		path = path.toAbsolutePath();

		// normalize
		path = path.normalize();

		// 转换为小写（不区分大小写）
		if (!items.isEmpty()) {
			List<String> items = new ArrayList<>(this.items.size());
			for (String item : this.items)
				items.add(item.toLowerCase());
			if (!items.equals(this.items))
				path = new BaiduPcsPath(fs, absolute, items);
		}

		// 检查是否存在
		fs.provider().readAttributes(this, BasicFileAttributes.class);

		return path;
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events,
			Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("Watching is not supported.");
	}

	@Override
	public int compareTo(Path other) {
		return this.toString().compareTo(((BaiduPcsPath) other).toString());
	}

	/**
	 * 返回以应用目录为根目录的路径字符串。
	 * 
	 * @return
	 */
	String toServiceString() {
		StringBuilder str = new StringBuilder();
		for (String dirItem : fs.getDir())
			str.append("/").append(dirItem);
		str.append(toAbsolutePath().normalize().toString());
		return str.toString();
	}

	@Override
	public String toString() {
		String seperator = fs.getSeparator();

		StringBuilder str = new StringBuilder();

		if (absolute)
			str.append(seperator);
		Iterator<String> itemItr = items.iterator();
		if (itemItr.hasNext())
			str.append(itemItr.next());
		while (itemItr.hasNext())
			str.append(seperator).append(itemItr.next());

		return str.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absolute ? 1231 : 1237);
		result = prime * result + ((fs == null) ? 0 : fs.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BaiduPcsPath))
			return false;
		BaiduPcsPath other = (BaiduPcsPath) obj;
		if (absolute != other.absolute)
			return false;
		if (fs == null) {
			if (other.fs != null)
				return false;
		} else if (!fs.equals(other.fs))
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

}
