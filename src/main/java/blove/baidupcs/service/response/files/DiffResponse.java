package blove.baidupcs.service.response.files;

import java.util.Map;

import blove.baidupcs.service.response.BasicResponse;

public class DiffResponse extends BasicResponse {
	private Map<String, DiffMeta> entries;
	private boolean has_more;
	private boolean reset;
	private String cursor;

	/**
	 * key为path，value为path的meta信息。
	 * 
	 * @return
	 */
	public Map<String, DiffMeta> getEntries() {
		return entries;
	}

	/**
	 * True： 本次调用diff接口，增量更新结果服务器端无法一次性返回，客户端可以立刻再调用一次diff接口获取剩余结果；<br>
	 * False： 截止当前的增量更新结果已经全部返回，客户端可以等待一段时间（1-2分钟）之后再diff一次查看是否有更新。
	 * 
	 * @return
	 */
	public boolean isHas_more() {
		return has_more;
	}

	/**
	 * True： 服务器通知客户端，服务器端将按时间排序从第一条开始向客户端返回一份完整的数据列表；<br>
	 * False：返回上次请求返回cursor之后的增量更新结果。
	 * 
	 * @return
	 */
	public boolean isReset() {
		return reset;
	}

	/**
	 * 用于下一次调用diff接口时传入的断点参数。
	 * 
	 * @return
	 */
	public String getCursor() {
		return cursor;
	}

	public static class DiffMeta {
		private long fs_id;
		private String path;
		private long ctime;
		private long mtime;
		private String md5;
		private long size;
		private int isdir;
		private int isdelete;
		private int revision;

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

		/**
		 * 是否是删除操作。<br>
		 * <li>isdelete=1 该文件被永久删除；<br> <li>isdelete=-1 该文件被放置进回收站；<br> <li>
		 * 如果path为文件，则删除该path对应的文件；<br> <li>
		 * 如果path为目录，则删除该path对应的目录和目录下的所有子目录和文件；<br> <li>
		 * 如果path在本地没有任何记录，则跳过本删除操作。
		 * 
		 * @return
		 */
		public int getIsdelete() {
			return isdelete;
		}

		/**
		 * Revision。
		 * 
		 * @return
		 */
		public int getRevision() {
			return revision;
		}

		@Override
		public String toString() {
			return "DiffMeta [\n\tfs_id=" + fs_id + "\n\tpath=" + path + "\n\tctime=" + ctime + "\n\tmtime=" + mtime
					+ "\n\tmd5=" + md5 + "\n\tsize=" + size + "\n\tisdir=" + isdir + "\n\tisdelete=" + isdelete
					+ "\n\trevision=" + revision + "\n]";
		}

	}

	@Override
	public String toString() {
		return "DiffResponse [\n\tentries=" + entries + "\n\thas_more=" + has_more + "\n\treset=" + reset
				+ "\n\tcursor=" + cursor + "\n]";
	}

}
