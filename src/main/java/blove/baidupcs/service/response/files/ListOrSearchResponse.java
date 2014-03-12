package blove.baidupcs.service.response.files;

import java.util.List;

import blove.baidupcs.service.response.BasicResponse;

public class ListOrSearchResponse extends BasicResponse {
	private List<FileInfo> list;

	/**
	 * 文件或目录信息列表。
	 * 
	 * @return
	 */
	public List<FileInfo> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "ListOrSearchResponse [\n\tlist=" + list + "\n]";
	}

	public static class FileInfo {
		private long fs_id;
		private String path;
		private long ctime;
		private long mtime;
		private String md5;
		private long size;
		private int isdir;

		/**
		 * 文件或目录在PCS的临时唯一标识ID。
		 * 
		 * @return
		 */
		public long getFs_id() {
			return fs_id;
		}

		/**
		 * 文件或目录的绝对路径。
		 * 
		 * @return
		 */
		public String getPath() {
			return path;
		}

		/**
		 * 文件或目录的创建时间。
		 * 
		 * @return
		 */
		public long getCtime() {
			return ctime;
		}

		/**
		 * 文件或目录的最后修改时间。
		 * 
		 * @return
		 */
		public long getMtime() {
			return mtime;
		}

		/**
		 * 文件的md5值。
		 * 
		 * @return
		 */
		public String getMd5() {
			return md5;
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
		 * 是否是目录的标识符：“0”为文件；“1”为目录。
		 * 
		 * @return
		 */
		public int getIsdir() {
			return isdir;
		}

		@Override
		public String toString() {
			return "FileInfo [\n\tfs_id=" + fs_id + "\n\tpath=" + path + "\n\tctime=" + ctime + "\n\tmtime=" + mtime
					+ "\n\tmd5=" + md5 + "\n\tsize=" + size + "\n\tisdir=" + isdir + "\n]";
		}

	}
}
