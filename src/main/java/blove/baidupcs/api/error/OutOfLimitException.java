package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 请求数量或流量超出限制时抛出此异常。
 * 
 * @author blove
 */
public class OutOfLimitException extends BaiduPcsException {
	private static final long serialVersionUID = 3005785735219026420L;

	public OutOfLimitException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
