package blove.baidupcs.api.response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse.QueryTaskInfo;

/**
 * 离线下载任务进度信息。
 * 
 * @author blove
 */
public class CloudDownloadProgress extends CloudDownloadBasic {
	private long fileSize;
	private long finishedSize;
	private long startTime;
	private long finishTime;

	public static CloudDownloadProgress fromSingleQueryResponse(CloudDownloadQueryTaskResponse response) {
		Map<String, QueryTaskInfo> taskInfos = response.getTask_info();
		if (taskInfos == null || taskInfos.isEmpty())
			return null;
		if (taskInfos.size() > 1)
			throw new IllegalArgumentException();

		Map.Entry<String, QueryTaskInfo> singleEntry = taskInfos.entrySet().iterator().next();
		return fromQueryTaskInfo(singleEntry.getKey(), singleEntry.getValue());
	}

	public static Map<String, CloudDownloadProgress> fromBatchQueryResponse(CloudDownloadQueryTaskResponse response) {
		Map<String, QueryTaskInfo> taskInfos = response.getTask_info();
		if (taskInfos == null || taskInfos.isEmpty())
			return Collections.emptyMap();

		Map<String, CloudDownloadProgress> ret = new HashMap<>();
		for (Map.Entry<String, QueryTaskInfo> entry : taskInfos.entrySet()) {
			CloudDownloadProgress meta = fromQueryTaskInfo(entry.getKey(), entry.getValue());
			ret.put(meta.getTaskID(), meta);
		}
		return ret;
	}

	private static CloudDownloadProgress fromQueryTaskInfo(String taskID, QueryTaskInfo info) {
		CloudDownloadProgress ret = new CloudDownloadProgress();
		ret.taskID = taskID;
		ret.result = Result.values()[info.getResult()];
		ret.status = Status.values()[info.getStatus()];
		ret.createTime = info.getCreate_time();
		ret.fileSize = info.getFile_size();
		ret.finishedSize = info.getFinished_size();
		ret.startTime = info.getStart_time();
		ret.finishTime = info.getFinish_time();
		return ret;
	}

	/**
	 * 文件大小。
	 * 
	 * @return
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * 已完成的大小。
	 * 
	 * @return
	 */
	public long getFinishedSize() {
		return finishedSize;
	}

	/**
	 * 任务开始时间。
	 * 
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 任务结束时间。
	 * 
	 * @return
	 */
	public long getFinishTime() {
		return finishTime;
	}

	@Override
	public String toString() {
		return "CloudDownloadProgress [\n\ttaskID=" + taskID + "\n\tresult=" + result + "\n\tstatus=" + status
				+ "\n\tcreateTime=" + createTime + "\n\tfileSize=" + fileSize + "\n\tfinishedSize=" + finishedSize
				+ "\n\tstartTime=" + startTime + "\n\tfinishTime=" + finishTime + "\n]";
	}

}
