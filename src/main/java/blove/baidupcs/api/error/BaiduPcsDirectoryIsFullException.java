package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 目录已满导致无法进行某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsDirectoryIsFullException extends FSRelatedException {
	private static final long serialVersionUID = 6421225141996073327L;

	public BaiduPcsDirectoryIsFullException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
