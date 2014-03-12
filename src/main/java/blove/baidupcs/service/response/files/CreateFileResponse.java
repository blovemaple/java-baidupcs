package blove.baidupcs.service.response.files;

import blove.baidupcs.service.response.BasicResponse;

public class CreateFileResponse extends BasicResponse {
	private String path;
	private long size;
	private long ctime;
	private long mtime;
	private String md5;
	private long fs_id;

	/**
	 * 该文件的绝对路径。
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 文件字节大小。
	 * 
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 文件创建时间。
	 * 
	 * @return
	 */
	public long getCtime() {
		return ctime;
	}

	/**
	 * 文件修改时间。
	 * 
	 * @return
	 */
	public long getMtime() {
		return mtime;
	}

	/**
	 * 文件的md5签名。
	 * 
	 * @return
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * 文件在PCS的临时唯一标识ID。
	 * 
	 * @return
	 */
	public long getFs_id() {
		return fs_id;
	}

	@Override
	public String toString() {
		return "CreateFileResponse [\n\tpath=" + path + "\n\tsize=" + size + "\n\tctime=" + ctime + "\n\tmtime="
				+ mtime + "\n\tmd5=" + md5 + "\n\tfs_id=" + fs_id + "\n]";
	}

}
