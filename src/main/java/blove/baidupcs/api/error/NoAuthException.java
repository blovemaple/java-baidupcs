package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 当请求进行无权限的某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class NoAuthException extends BaiduPcsException {
	private static final long serialVersionUID = -2665803164860116002L;

	public NoAuthException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
