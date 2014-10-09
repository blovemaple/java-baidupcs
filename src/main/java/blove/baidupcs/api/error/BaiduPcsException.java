package blove.baidupcs.api.error;

import java.io.IOException;

import retrofit.client.Response;

/**
 * 请求百度云存储服务时抛出与服务相关的所有异常的父类。
 * 
 * @author blove
 */
public class BaiduPcsException extends IOException {
	private static final long serialVersionUID = -4525183341654870516L;

	private final ErrorResponse errorResponse;
	private final int httpStatus;
	private final String httpReason;

	/**
	 * 新建一个实例。
	 * 
	 * @param cause
	 * @param httpResponse
	 */
	public BaiduPcsException(Throwable cause, Response httpResponse) {
		this(cause, null, httpResponse);
	}

	/**
	 * 新建一个实例。
	 * 
	 * @param errorResponse
	 *             服务器的返回数据。
	 * @param httpResponse
	 */
	public BaiduPcsException(ErrorResponse errorResponse, Response httpResponse) {
		this(null, errorResponse, httpResponse);
	}

	private BaiduPcsException(Throwable cause, ErrorResponse errorResponse, Response httpResponse) {
		super("http:"
				+ (httpResponse == null ? null : "(" + httpResponse.getStatus() + ")" + httpResponse.getReason())
				+ "; baidupcs:"
				+ (errorResponse == null ? "null" : "(" + errorResponse.getError_code() + ")"
						+ errorResponse.getError_msg()), cause);
		this.errorResponse = errorResponse;
		this.httpStatus = httpResponse == null ? -1 : httpResponse.getStatus();
		this.httpReason = httpResponse == null ? null : httpResponse.getReason();
	}

	/**
	 * 返回服务器返回的错误数据。如果没有则返回null。
	 * 
	 * @return
	 */
	public ErrorResponse getErrorResponse() {
		return errorResponse;
	}

	/**
	 * 返回HTTP状态码。
	 * 
	 * @return HTTP状态码。若无HTTP响应则返回-1。
	 */
	public int getHttpStatus() {
		return httpStatus;
	}

	/**
	 * 返回HTTP reason。
	 * 
	 * @return HTTP reason。若无HTTP响应则返回null。
	 */
	public String getHttpReason() {
		return httpReason;
	}

}
