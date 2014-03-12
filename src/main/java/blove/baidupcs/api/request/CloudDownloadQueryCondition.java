package blove.baidupcs.api.request;

import blove.baidupcs.api.response.CloudDownloadBasic.Status;

/**
 * 查询离线下载任务的条件。
 * 
 * @author blove
 */
public class CloudDownloadQueryCondition {
	private String sourceUrl;
	private String savePath;
	private int createTime = -1;
	private Status status;

	/**
	 * 源地址URL条件。
	 * 
	 * @return
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * 设置源地址URL条件。
	 * 
	 * @param sourceUrl
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	/**
	 * 文件保存路径条件。此路径是以应用文件夹为根目录的路径。
	 * 
	 * @return
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * 设置文件保存路径条件。此路径是以应用文件夹为根目录的路径。
	 * 
	 * @param savePath
	 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	/**
	 * 任务创建时间条件。若为-1则为不指定此条件。
	 * 
	 * @return
	 */
	public int getCreateTime() {
		return createTime;
	}

	/**
	 * 设置任务创建时间条件。若指定-1则视为不指定此条件。
	 * 
	 * @param createTime
	 */
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	/**
	 * 任务状态条件。
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * 设置任务状态条件。
	 * 
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

}
