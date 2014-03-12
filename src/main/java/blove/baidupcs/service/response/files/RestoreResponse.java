package blove.baidupcs.service.response.files;

import java.util.List;

import blove.baidupcs.service.response.BasicResponse;

public class RestoreResponse extends BasicResponse {
	private ExtraInfo extra;

	/**
	 * 包含还原成功的元素的fs_id。（之所以多这一层“extra”，大概是为了和错误时返回的json串兼容）
	 * 
	 * @return
	 */
	public ExtraInfo getExtra() {
		return extra;
	}

	public static class ExtraInfo {
		private List<RestoreFileInfo> list;

		/**
		 * 包含还原成功的元素的fs_id。
		 * 
		 * @return
		 */
		public List<RestoreFileInfo> getList() {
			return list;
		}

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

	}
}
