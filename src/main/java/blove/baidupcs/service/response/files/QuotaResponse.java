package blove.baidupcs.service.response.files;

import blove.baidupcs.service.response.BasicResponse;

/**
 * 用户空间配额信息。
 * 
 * @author blove
 */
public class QuotaResponse extends BasicResponse {
	private long quota;
	private long used;

	/**
	 * 空间配额，单位为字节。
	 * 
	 * @return
	 */
	public long getQuota() {
		return quota;
	}

	/**
	 * 已使用空间大小，单位为字节。
	 * 
	 * @return
	 */
	public long getUsed() {
		return used;
	}

	@Override
	public String toString() {
		return "QuotaResponse [\n\tquota=" + quota + "\n\tused=" + used + "\n]";
	}

}
