package blove.baidupcs.api.response;

/**
 * 文件或目录的元信息。
 * 
 * @author blove
 */
public class FileMeta {
	protected long fsID;
	protected String path;
	protected long ctime;
	protected long mtime;
	protected long size;
	protected int isDir;

	/**
	 * 文件或目录在PCS的临时唯一标识ID。
	 * 
	 * @return
	 */
	public long getFsID() {
		return fsID;
	}

	/**
	 * 返回以应用目录为根目录的路径。
	 * 
	 * @return
	 */
	public String getPathInApp() {
		String[] paths = path.split("/", 4);
		return "/" + paths[3];
	}

	/**
	 * 返回文件/目录的名称
	 * 
	 * @return
	 */
	public String getFileName() {
		String[] paths = path.split("/");
		return paths[paths.length - 1];
	}

	/**
	 * 文件或目录的绝对时间，单位：秒。
	 * 
	 * @return
	 */
	public long getCtime() {
		return ctime;
	}

	/**
	 * 文件或目录的最后修改时间，单位：秒。
	 * 
	 * @return
	 */
	public long getMtime() {
		return mtime;
	}

	/**
	 * 文件大小（byte）。
	 * 
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 是否是目录。
	 * 
	 * @return
	 */
	public boolean isDir() {
		return isDir == 1;
	}
}
