package blove.baidupcs.api.response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blove.baidupcs.service.response.files.CloudDownloadListTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadListTaskResponse.ListTaskInfo;
import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse.QueryTaskInfo;

/**
 * 离线下载任务元信息。
 * 
 * @author blove
 */
public class CloudDownloadMeta extends CloudDownloadBasic {
	private String sourceUrl;
	private String savePath;
	private int rateLimit;
	private int timeout;
	private String callback;

	public static CloudDownloadMeta fromSingleQueryResponse(
			CloudDownloadQueryTaskResponse response) {
		Map<String, QueryTaskInfo> taskInfos = response.getTask_info();
		if (taskInfos == null || taskInfos.isEmpty())
			return null;
		if (taskInfos.size() > 1)
			throw new IllegalArgumentException();

		Map.Entry<String, QueryTaskInfo> singleEntry = taskInfos.entrySet()
				.iterator().next();
		return fromQueryTaskInfo(singleEntry.getKey(), singleEntry.getValue());
	}

	public static Map<String, CloudDownloadMeta> fromBatchQueryResponse(
			CloudDownloadQueryTaskResponse response) {
		Map<String, QueryTaskInfo> taskInfos = response.getTask_info();
		if (taskInfos == null || taskInfos.isEmpty())
			return Collections.emptyMap();

		Map<String, CloudDownloadMeta> ret = new HashMap<>();
		for (Map.Entry<String, QueryTaskInfo> entry : taskInfos.entrySet()) {
			CloudDownloadMeta meta = fromQueryTaskInfo(entry.getKey(),
					entry.getValue());
			ret.put(meta.getTaskID(), meta);
		}
		return ret;
	}

	private static CloudDownloadMeta fromQueryTaskInfo(String taskID,
			QueryTaskInfo info) {
		CloudDownloadMeta ret = new CloudDownloadMeta();
		ret.taskID = taskID;
		ret.result = Result.values()[info.getResult()];
		ret.status = Status.values()[info.getStatus()];
		ret.createTime = info.getCreate_time();
		ret.sourceUrl = info.getSource_url();
		ret.savePath = info.getSave_path();
		ret.rateLimit = info.getRate_limit();
		ret.timeout = info.getTimeout();
		ret.callback = info.getCallback();
		return ret;
	}

	public static Map<String, CloudDownloadMeta> fromListResponse(
			CloudDownloadListTaskResponse response) {
		List<ListTaskInfo> taskInfos = response.getTask_info();
		if (taskInfos == null || taskInfos.isEmpty())
			return Collections.emptyMap();

		Map<String, CloudDownloadMeta> ret = new HashMap<>();
		for (ListTaskInfo taskInfo : taskInfos) {
			CloudDownloadMeta meta = fromListTaskInfo(taskInfo);
			ret.put(meta.getTaskID(), meta);
		}
		return ret;
	}

	private static CloudDownloadMeta fromListTaskInfo(ListTaskInfo info) {
		CloudDownloadMeta ret = new CloudDownloadMeta();
		ret.taskID = info.getTask_id();
		ret.result = Result.SUCCESS;
		ret.status = Status.values()[info.getStatus()];
		ret.createTime = info.getCreate_time();
		ret.sourceUrl = info.getSource_url();
		ret.savePath = info.getSave_path();
		ret.rateLimit = info.getRate_limit();
		ret.timeout = info.getTimeout();
		ret.callback = info.getCallback();
		return ret;
	}

	/**
	 * 源文件的URL。
	 * 
	 * @return
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * 下载后的文件保存路径。以应用目录为根目录。
	 * 
	 * @return
	 */
	public String getSavePathInApp() {
		String[] paths = savePath.split("/", 4);
		return "/" + paths[3];
	}

	/**
	 * 下载限速。
	 * 
	 * @return
	 */
	public int getRateLimit() {
		return rateLimit;
	}

	/**
	 * 下载超时时间。
	 * 
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 下载完毕后的回调。
	 * 
	 * @return
	 */
	public String getCallback() {
		return callback;
	}

	@Override
	public String toString() {
		return "CloudDownloadMeta [\n\ttaskID=" + taskID + "\n\tresult="
				+ result + "\n\tstatus=" + status + "\n\tcreateTime="
				+ createTime + "\n\tsourceUrl=" + sourceUrl + "\n\tsavePath="
				+ savePath + "\n\trateLimit=" + rateLimit + "\n\ttimeout="
				+ timeout + "\n\tcallback=" + callback + "\n]";
	}

}
