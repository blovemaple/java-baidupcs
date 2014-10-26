package blove.baidupcs.service.response.auth;

public class TokenResponse {
	private String access_token;
	private int expires_in;
	private String refresh_token;
	private String scope;
	private String session_key;
	private String session_secret;

	/**
	 * 要获取的Access Token。
	 * 
	 * @return
	 */
	public String getAccess_token() {
		return access_token;
	}

	/**
	 * Access Token的有效期，以秒为单位。
	 * 
	 * @return
	 */
	public int getExpires_in() {
		return expires_in;
	}

	/**
	 * 用于刷新Access Token的Refresh Token。
	 * 
	 * @return
	 */
	public String getRefresh_token() {
		return refresh_token;
	}

	/**
	 * Access Token最终的访问范围，即用户实际授予的权限列表（用户在授权页面时，有可能会取消掉某些请求的权限）。
	 * 
	 * @return
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * 基于http调用Open API时所需要的Session Key，其有效期与Access Token一致。
	 * 
	 * @return
	 */
	public String getSession_key() {
		return session_key;
	}

	/**
	 * 基于http调用Open API时计算参数签名用的签名密钥。
	 * 
	 * @return
	 */
	public String getSession_secret() {
		return session_secret;
	}

}
