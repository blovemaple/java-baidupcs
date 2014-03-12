package blove.baidupcs.service.request.files;

import java.util.List;

/**
 * 批量还原回收站文件的参数，提供需还原文件的列表。
 * 
 * @author blove
 */
public class RestoreBatchParam {
	private List<RestoreFileInfo> list;

	/**
	 * 包含需要还原的元素的fs_id。
	 * 
	 * @return
	 */
	public List<RestoreFileInfo> getList() {
		return list;
	}

	/**
	 * 设置list参数，包含需要还原的元素的fs_id。
	 * 
	 * @param list
	 */
	public void setList(List<RestoreFileInfo> list) {
		this.list = list;
	}

	public static class RestoreFileInfo {
		private String fs_id;

		/**
		 * 文件或目录在PCS的临时唯一标识ID。
		 * 
		 * @return
		 */
		public String getFs_id() {
			return fs_id;
		}

		/**
		 * 设置文件或目录在PCS的临时唯一标识ID。
		 * 
		 * @param fs_id
		 */
		public void setFs_id(String fs_id) {
			this.fs_id = fs_id;
		}

	}
}
