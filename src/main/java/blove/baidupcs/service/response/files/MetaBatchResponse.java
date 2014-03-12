package blove.baidupcs.service.response.files;

import java.util.List;

import blove.baidupcs.service.response.BasicResponse;

public class MetaBatchResponse extends BasicResponse {
	private List<Meta> list;

	/**
	 * 元信息列表。
	 * 
	 * @return
	 */
	public List<Meta> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "MetaResponse [\n\tlist=" + list + "\n]";
	}

	public class Meta {
		private long fs_id;
		private String path;
		private long ctime;
		private long mtime;
		private String block_list;
		private long size;
		private int isdir;
		private int ifhassubdir;

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
		 * 文件所有分片的md5。
		 * 
		 * @return
		 */
		public String getBlock_list() {
			return block_list;
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

		/**
		 * 是否含有子目录的标识符：“0”表示没有子目录；“1”表示有子目录。
		 * 
		 * @return
		 */
		public int getIfhassubdir() {
			return ifhassubdir;
		}

		@Override
		public String toString() {
			return "Meta [\n\tfs_id=" + fs_id + "\n\tpath=" + path + "\n\tctime=" + ctime + "\n\tmtime=" + mtime
					+ "\n\tblock_list=" + getBlock_list() + "\n\tsize=" + size + "\n\tisdir=" + isdir
					+ "\n\tifhassubdir=" + ifhassubdir + "\n]";
		}

	}
}
