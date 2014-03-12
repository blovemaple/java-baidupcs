package blove.baidupcs.api.error;

import blove.baidupcs.service.response.BasicResponse;

/**
 * 服务器返回的错误信息。
 * 
 * @author blove
 */
public class ErrorResponse extends BasicResponse {
	private String error_code;
	private String error_msg;

	/**
	 * 错误码。
	 * 
	 * @return
	 */
	public String getError_code() {
		return error_code;
	}

	/**
	 * 错误信息。
	 * 
	 * @return
	 */
	public String getError_msg() {
		return error_msg;
	}

	@Override
	public String toString() {
		return "ErrorResponse [\n\terror_code=" + error_code + "\n\terror_msg=" + error_msg + "\n]";
	}

}
