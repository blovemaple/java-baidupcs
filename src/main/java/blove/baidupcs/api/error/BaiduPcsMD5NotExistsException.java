package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * MD5代表的文件或文件分块不存在导致不能完成某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsMD5NotExistsException extends FSRelatedException {
	private static final long serialVersionUID = -817103764897931817L;

	public BaiduPcsMD5NotExistsException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
