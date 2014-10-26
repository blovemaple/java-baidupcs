package blove.baidupcs.api;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import blove.baidupcs.api.error.auth.AuthErrorParseHandler;
import blove.baidupcs.service.BaiduPcsAuthService;

/**
 * 封装的百度个人云存储空间权限获取接口。
 * 
 * @author blove
 */
public class BaiduPcsAuth {
	private final String apiKey;
	private final String secretKey;
	private final LogLevel logLevel;

	private BaiduPcsAuthService service;

	/**
	 * 创建一个实例，不输出日志。
	 * 
	 * @param apiKey
	 *            应用的API Key
	 * @param secretKey
	 *            应用的Secret Key
	 */
	public BaiduPcsAuth(String apiKey, String secretKey) {
		this(apiKey, secretKey, LogLevel.NONE);
	}

	/**
	 * 创建一个实例。
	 * 
	 * @param apiKey
	 *            应用的API Key
	 * @param secretKey
	 *            应用的Secret Key
	 * @param logLevel
	 *            日志等级
	 */
	public BaiduPcsAuth(String apiKey, String secretKey, LogLevel logLevel) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.logLevel = logLevel;

		ErrorHandler errorHandler = new AuthErrorParseHandler();
		service = new RestAdapter.Builder().setLogLevel(logLevel)
				.setEndpoint(BaiduPcsAuthService.SERVER)
				.setErrorHandler(errorHandler).build()
				.create(BaiduPcsAuthService.class);
	}
	
	

}
