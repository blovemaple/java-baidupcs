package blove.baidupcs.api.error.auth;

public class BaiduPcsAuthException extends Exception {
	private static final long serialVersionUID = 7528438281808230197L;

	private ErrorCode errorCode;
	private String errorDescription;

	public BaiduPcsAuthException(AuthErrorResponse errorResponse) {
		String error = errorResponse.getError();
		if (error == null) {
			errorCode = ErrorCode.UNKNOWN;
		} else {
			for (ErrorCode errorCode : ErrorCode.values()) {
				if (errorCode.name().equalsIgnoreCase(error)) {
					this.errorCode = errorCode;
					break;
				}
			}
			if (errorCode == null) {
				errorCode = ErrorCode.UNKNOWN;
			}
		}
		errorDescription = errorResponse.getError_description();
	}

	/**
	 * 错误码枚举值。
	 * 
	 * @return
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/**
	 * 错误描述信息，用来帮助理解和解决发生的错误。
	 * 
	 * @return
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	public enum ErrorCode {
		/**
		 * 请求缺少某个必需参数，包含一个不支持的参数或参数值，或者格式不正确。
		 */
		INVALID_REQUEST,
		/**
		 * “client_id”、“client_secret”参数无效。
		 */
		INVALID_CLIENT,
		/**
		 * 提供的Access Grant是无效的、过期的或已撤销的，例如，Authorization
		 * Code无效(一个授权码只能使用一次)、Refresh Token无效、redirect_uri与获取Authorization
		 * Code时提供的不一致、Devie Code无效(一个设备授权码只能使用一次)等。
		 */
		INVALID_GRANT,
		/**
		 * 应用没有被授权，无法使用所指定的grant_type。
		 */
		UNAUTHORIZED_CLIENT,
		/**
		 * “grant_type”百度OAuth2.0服务不支持该参数。
		 */
		UNSUPPORTED_GRANT_TYPE,
		/**
		 * 请求的“scope”参数是无效的、未知的、格式不正确的、或所请求的权限范围超过了数据拥有者所授予的权限范围。
		 */
		INVALID_SCOPE,
		/**
		 * 提供的Refresh Token已过期
		 */
		EXPIRED_TOKEN,
		/**
		 * “redirect_uri”所在的根域与开发者注册应用时所填写的根域名不匹配。
		 */
		REDIRECT_URI_MISMATCH,
		/**
		 * “response_type”参数值不为百度OAuth2.0服务所支持，或者应用已经主动禁用了对应的授权模式
		 */
		UNSUPPORTED_RESPONSE_TYPE,
		/**
		 * Device Flow中，设备通过Device Code换取Access Token的接口过于频繁，两次尝试的间隔应大于5秒。
		 */
		SLOW_DOWN,
		/**
		 * Device Flow中，用户还没有对Device Code完成授权操作。
		 */
		AUTHORIZATION_PENDING,
		/**
		 * Device Flow中，用户拒绝了对Device Code的授权操作。
		 */
		AUTHORIZATION_DECLINED,
		/**
		 * Implicit Grant模式中，浏览器请求的Referer与根域名绑定不匹配
		 */
		INVALID_REFERER,

		/**
		 * 未知
		 */
		UNKNOWN
	}
}
