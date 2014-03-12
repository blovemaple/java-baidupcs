package blove.baidupcs.api.error;

import retrofit.client.Response;

public class ServerException extends BaiduPcsException {
	private static final long serialVersionUID = -4695869421131958811L;

	/**
	 * 新建一个实例。
	 * 
	 * @param errorResponse
	 * @param httpResponse
	 */
	public ServerException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
