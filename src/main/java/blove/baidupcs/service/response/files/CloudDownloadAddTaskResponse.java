package blove.baidupcs.service.response.files;

import blove.baidupcs.service.response.BasicResponse;

public class CloudDownloadAddTaskResponse extends BasicResponse {
	private String task_id;

	/**
	 * 任务ID号。
	 * 
	 * @return
	 */
	public String getTask_id() {
		return task_id;
	}

}
