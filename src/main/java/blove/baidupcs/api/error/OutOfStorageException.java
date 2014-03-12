package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 容量超出限制时抛出此异常。
 * 
 * @author blove
 */
public class OutOfStorageException extends BaiduPcsException {
	private static final long serialVersionUID = -2377368972854222455L;

	public OutOfStorageException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
