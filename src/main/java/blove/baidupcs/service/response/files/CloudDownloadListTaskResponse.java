package blove.baidupcs.service.response.files;

import java.util.List;

import blove.baidupcs.service.response.BasicResponse;

public class CloudDownloadListTaskResponse extends BasicResponse {
	private List<ListTaskInfo> task_info;
	private String total;

	/**
	 * 任务列表。
	 * 
	 * @return
	 */
	public List<ListTaskInfo> getTask_info() {
		return task_info;
	}

	/**
	 * 任务总数。
	 * 
	 * @return
	 */
	public String getTotal() {
		return total;
	}

	public static class ListTaskInfo {
		private String task_id;
		private int status;
		private long create_time;
		private String source_url;
		private String save_path;
		private int rate_limit;
		private int timeout;
		private String callback;

		/**
		 * 任务ID。
		 * 
		 * @return
		 */
		public String getTask_id() {
			return task_id;
		}

		/**
		 * 下载状态。仅在查询任务信息时有效。
		 * 
		 * @return （常量已在{@link CloudDownloadQueryTaskResponse}中定义）0下载成功，1下载进行中
		 *         2系统错误，3资源不存在，4下载超时，5资源存在但下载失败 6存储空间不足 7目标地址数据已存在 8任务取消
		 */
		public int getStatus() {
			return status;
		}

		/**
		 * 任务创建时间。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public long getCreate_time() {
			return create_time;
		}

		/**
		 * 下载数据源地址。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public String getSource_url() {
			return source_url;
		}

		/**
		 * 下载完成后的存放地址。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public String getSave_path() {
			return save_path;
		}

		/**
		 * 下载限速。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public int getRate_limit() {
			return rate_limit;
		}

		/**
		 * 下载超时时间。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public int getTimeout() {
			return timeout;
		}

		/**
		 * 下载完毕后的回调。仅在查询任务信息时有效。
		 * 
		 * @return
		 */
		public String getCallback() {
			return callback;
		}

	}
}
