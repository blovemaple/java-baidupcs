package blove.baidupcs.api.error.auth;

public class AuthErrorResponse {
	private String error;
	private String error_description;

	/**
	 * 错误码。
	 * 
	 * @return
	 */
	public String getError() {
		return error;
	}

	/**
	 * 错误描述信息，用来帮助理解和解决发生的错误。
	 * 
	 * @return
	 */
	public String getError_description() {
		return error_description;
	}

}
