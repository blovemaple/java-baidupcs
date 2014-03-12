package blove.baidupcs.api.response;

/**
 * 离线下载任务元信息和进度信息的公共部分。
 * 
 * @author blove
 */
public abstract class CloudDownloadBasic {
	protected String taskID;
	protected Result result;
	protected Status status;
	protected long createTime;

	/**
	 * 任务ID。
	 * 
	 * @return
	 */
	public String getTaskID() {
		return taskID;
	}

	/**
	 * 查询结果。
	 * 
	 * @return
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * 下载状态。
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * 任务创建时间。
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * 离线下载任务查询结果。
	 * 
	 * @author blove
	 */
	public enum Result {
		// 注意：顺序与编号一致！
		/**
		 * 查询成功，结果有效
		 */
		SUCCESS,
		/**
		 * 要查询的task_id不存在
		 */
		NOT_EXIST
	}

	/**
	 * 离线下载任务下载状态。
	 * 
	 * @author blove
	 */
	public enum Status {
		// 注意：顺序与编号一致！
		/**
		 * 下载成功
		 */
		SUCCESS,
		/**
		 * 下载进行中
		 */
		DOWNLOADING,
		/**
		 * 系统错误
		 */
		SYSTEM_ERROR,
		/**
		 * 资源不存在
		 */
		RESOURCE_NOT_EXIST,
		/**
		 * 下载超时
		 */
		DOWNLOAD_TIMEOUT,
		/**
		 * 资源存在但下载失败
		 */
		DOWNLOAD_FAIL,
		/**
		 * 存储空间不足
		 */
		OUT_OF_QUOTA,
		/**
		 * 存储空间不足
		 */
		TARGET_ALREADY_EXIST,
		/**
		 * 任务取消
		 */
		TASK_CANCELLED
	}

}
