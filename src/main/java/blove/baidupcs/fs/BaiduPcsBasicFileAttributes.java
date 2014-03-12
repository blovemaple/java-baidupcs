package blove.baidupcs.fs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class BaiduPcsBasicFileAttributes implements BasicFileAttributes {

	private final FileTime creationTime, lastModifiedTime;
	private final boolean isDirectory;
	private final long size;
	private final long fsId;

	/**
	 * @param creationTime
	 *            创建时间。单位：毫秒。
	 * @param lastModifiedTime
	 *            最后修改时间。单位：毫秒。
	 * @param isDirectory
	 *            是否是目录
	 * @param size
	 *            大小。单位：字节。
	 * @param fsId
	 *            作为fileKey
	 */
	BaiduPcsBasicFileAttributes(long creationTime, long lastModifiedTime,
			boolean isDirectory, long size, long fsId) {
		this.creationTime = FileTime.fromMillis(creationTime);
		this.lastModifiedTime = FileTime.fromMillis(lastModifiedTime);
		this.isDirectory = isDirectory;
		this.size = size;
		this.fsId = fsId;
	}

	@Override
	public FileTime lastModifiedTime() {
		return lastModifiedTime;
	}

	@Override
	public FileTime lastAccessTime() {
		return lastModifiedTime;
	}

	@Override
	public FileTime creationTime() {
		return creationTime;
	}

	@Override
	public boolean isRegularFile() {
		return !isDirectory;
	}

	@Override
	public boolean isDirectory() {
		return isDirectory;
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public Object fileKey() {
		return fsId;
	}

	@Override
	public String toString() {
		return "BaiduPcsBasicFileAttributes [\n\tcreationTime=" + creationTime
				+ "\n\tlastModifiedTime=" + lastModifiedTime
				+ "\n\tisDirectory=" + isDirectory + "\n\tsize=" + size
				+ "\n\tfsId=" + fsId + "\n]";
	}

}
