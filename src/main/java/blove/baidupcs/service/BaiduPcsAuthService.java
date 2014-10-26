package blove.baidupcs.service;

import retrofit.http.GET;
import retrofit.http.Query;
import blove.baidupcs.service.response.auth.DeviceCodeResponse;
import blove.baidupcs.service.response.auth.TokenResponse;

public interface BaiduPcsAuthService {
	/**
	 * 构建RestAdapter.Builder需要使用的server配置。
	 */
	String SERVER = "https://openapi.baidu.com/oauth/2.0";

	/**
	 * deviceCode方法的responseType参数值。
	 */
	String RESPONSE_TYPE_DEVICE_CODE = "device_code";
	/**
	 * deviceCode和refreshToken方法的scope参数值，用于请求获取用户在个人云存储中存放的数据的权限。
	 */
	String SCOPE_NETDISK = "netdisk";

	/**
	 * deviceToken方法的grantType参数值。
	 */
	String GRANT_TYPE_DEVICE_TOKEN = "device_token";

	/**
	 * 获取User Code和Device Code，用于引导用户去百度填写User Code并授权。
	 * 
	 * @param clientId
	 *            必须参数，注册应用时获得的API Key。
	 * @param responseType
	 *            必须参数，此值固定为“device_code”。
	 * @param scope
	 *            非必须参数，以空格分隔的权限列表，若不传递此参数，代表请求用户的默认权限。
	 * @return 响应
	 */
	@GET("/device/code")
	DeviceCodeResponse deviceCode(@Query("client_id") String clientId,
			@Query("response_type") String responseType,
			@Query("scope") String scope);

	/**
	 * 向用户展现了授权URL和User Code之后，通过Device Code来获取Access
	 * Token。这个操作可以由用户完成授权后在设备上触发，也可以由设备向OAuth服务器进行轮询尝试。
	 * 
	 * @param grantType
	 *            必须参数，此值固定为“device_token”。
	 * @param code
	 *            必须参数，通过上面第一步所获得的Device Code。
	 * @param clientId
	 *            必须参数，应用的API Key。
	 * @param clientSecret
	 *            必须参数，应用的Secret Key。
	 * @return 响应
	 */
	@GET("/token")
	TokenResponse deviceToken(@Query("grant_type") String grantType,
			@Query("code") String code, @Query("client_id") String clientId,
			@Query("client_secret") String clientSecret);

	/**
	 * 使用Refresh Token刷新以获得新的Access Token。
	 * 
	 * @param grantType
	 *            必须参数，固定为“refresh_token”。
	 * @param refresh_token
	 *            必须参数，用于刷新Access Token用的Refresh Token。
	 * @param clientId
	 *            必须参数，应用的API Key。
	 * @param clientSecret
	 *            必须参数，应用的Secret Key。
	 * @param scope
	 *            非必须参数。以空格分隔的权限列表，若不传递此参数，代表请求的数据访问操作权限与上次获取Access
	 *            Token时一致。通过Refresh Token刷新Access
	 *            Token时所要求的scope权限范围必须小于等于上次获取Access Token时授予的权限范围。
	 * @return 响应
	 */
	@GET("/token")
	TokenResponse refreshToken(@Query("grant_type") String grantType,
			@Query("refresh_token") String refresh_token,
			@Query("client_id") String clientId,
			@Query("client_secret") String clientSecret,
			@Query("scope") String scope);
}
