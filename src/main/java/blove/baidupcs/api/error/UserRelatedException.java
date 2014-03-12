package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 用户状态致使无法进行某项操作时抛出此异常。如用户不存在等。
 * 
 * @author blove
 */
public class UserRelatedException extends BaiduPcsException {
	private static final long serialVersionUID = 8307657760163659285L;

	public UserRelatedException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
