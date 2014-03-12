package blove.baidupcs.service.response.files;

import blove.baidupcs.service.response.BasicResponse;

public class MkdirResponse extends BasicResponse {
	private long fs_id;
	private String path;
	private long ctime;
	private long mtime;

	/**
	 * 目录在PCS的临时唯一标识id。
	 * 
	 * @return
	 */
	public long getFs_id() {
		return fs_id;
	}

	/**
	 * 该目录的绝对路径。
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 目录创建时间。
	 * 
	 * @return
	 */
	public long getCtime() {
		return ctime;
	}

	/**
	 * 目录修改时间。
	 * 
	 * @return
	 */
	public long getMtime() {
		return mtime;
	}

	@Override
	public String toString() {
		return "MkdirResponse [\n\tfs_id=" + fs_id + "\n\tpath=" + path + "\n\tctime=" + ctime + "\n\tmtime=" + mtime
				+ "\n]";
	}

}
