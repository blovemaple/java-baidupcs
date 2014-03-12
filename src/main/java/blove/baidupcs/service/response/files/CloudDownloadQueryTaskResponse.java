package blove.baidupcs.service.response.files;

import java.util.Map;

import blove.baidupcs.service.response.BasicResponse;

public class CloudDownloadQueryTaskResponse extends BasicResponse {

	// result字段值
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_NOT_EXIST = 1;

	// status字段值
	public static final int STATUS_SUCCESS = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_SYSTEM_ERROR = 2;
	public static final int STATUS_RESOURCE_NOT_EXIST = 3;
	public static final int STATUS_DOWNLOAD_TIMEOUT = 4;
	public static final int STATUS_DOWNLOAD_FAIL = 5;
	public static final int STATUS_OUT_OF_QUOTA = 6;
	public static final int STATUS_TARGET_ALREADY_EXIST = 7;
	public static final int STATUS_TASK_CANCELLED = 8;

	private Map<String, QueryTaskInfo> task_info;

	/**
	 * 查询到的信息。
	 * 
	 * @return 任务ID到信息的映射
	 */
	public Map<String, QueryTaskInfo> getTask_info() {
		return task_info;
	}

	public static class QueryTaskInfo {
		private int result;
		private int status;
		private long create_time;

		// 任务信息特有字段
		private String source_url;
		private String save_path;
		private int rate_limit;
		private int timeout;
		private String callback;

		// 进度信息特有字段
		private long file_size;
		private long finished_size;
		private long start_time;
		private long finish_time;

		/**
		 * 查询结果。
		 * 
		 * @return （常量已定义）0查询成功，结果有效，1要查询的task_id不存在
		 */
		public int getResult() {
			return result;
		}

		/**
		 * 下载状态。
		 * 
		 * @return （常量已定义）0下载成功，1下载进行中 2系统错误，3资源不存在，4下载超时，5资源存在但下载失败 6存储空间不足
		 *         7目标地址数据已存在 8任务取消
		 */
		public int getStatus() {
			return status;
		}

		/**
		 * 任务创建时间。
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

		/**
		 * 文件大小。仅在查询进度信息时有效。
		 * 
		 * @return
		 */
		public long getFile_size() {
			return file_size;
		}

		/**
		 * 已完成的大小。仅在查询进度信息时有效。
		 * 
		 * @return
		 */
		public long getFinished_size() {
			return finished_size;
		}

		/**
		 * 任务开始时间。仅在查询进度信息时有效。
		 * 
		 * @return
		 */
		public long getStart_time() {
			return start_time;
		}

		/**
		 * 任务结束时间。仅在查询进度信息时有效。
		 * 
		 * @return
		 */
		public long getFinish_time() {
			return finish_time;
		}

	}
}
