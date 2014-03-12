package blove.baidupcs.service.response.files;

import blove.baidupcs.service.response.BasicResponse;

public class UploadFileBlockResponse extends BasicResponse {
	private String md5;

	/**
	 * 文件的md5签名。
	 * 
	 * @return
	 */
	public String getMd5() {
		return md5;
	}

	@Override
	public String toString() {
		return "UploadFileBlockResponse [\n\tmd5=" + md5 + "\n]";
	}

}
