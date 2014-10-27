package blove.baidupcs.api;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import blove.baidupcs.api.error.auth.AuthErrorParseHandler;
import blove.baidupcs.api.error.auth.BaiduPcsAuthException;
import blove.baidupcs.api.error.auth.InvalidPropsFileException;
import blove.baidupcs.service.BaiduPcsAuthService;
import blove.baidupcs.service.response.auth.DeviceCodeResponse;
import blove.baidupcs.service.response.auth.TokenResponse;

/**
 * 封装的百度个人云存储空间权限获取接口。
 * 
 * @author blove
 */
public class BaiduPcsAuth {
	private final String apiKey;
	private final String secretKey;
	@SuppressWarnings("unused")
	private final LogLevel logLevel;

	private BaiduPcsAuthService service;

	private static final String PROP_KEY_API_KEY = "api_key";
	private static final String PROP_KEY_SECRET_KEY = "secret_key";
	private static final String PROP_KEY_REFRESH_TOKEN = "refresh_token";
	private static final String PROP_KEY_ACCESS_TOKEN = "access_token";
	private static final String PROP_KEY_EXPIRE_TIME = "expire_time";
	private static final String PROP_KEY_APP_NAME = "app_name";

	/**
	 * 创建一个实例，不输出日志。
	 * 
	 * @param apiKey
	 *             应用的API Key
	 * @param secretKey
	 *             应用的Secret Key
	 */
	public BaiduPcsAuth(String apiKey, String secretKey) {
		this(apiKey, secretKey, LogLevel.NONE);
	}

	/**
	 * 创建一个实例。
	 * 
	 * @param apiKey
	 *             应用的API Key
	 * @param secretKey
	 *             应用的Secret Key
	 * @param logLevel
	 *             日志等级
	 */
	public BaiduPcsAuth(String apiKey, String secretKey, LogLevel logLevel) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.logLevel = logLevel;

		ErrorHandler errorHandler = new AuthErrorParseHandler();
		service = new RestAdapter.Builder().setLogLevel(logLevel).setEndpoint(BaiduPcsAuthService.SERVER)
				.setErrorHandler(errorHandler).build().create(BaiduPcsAuthService.class);
	}

	/**
	 * 获取新的access token。此方法调用之后，会用另一个线程调用notifier通知user
	 * code及相关信息，然后等待直到用户授权完成或取消授权，或超时。
	 * 
	 * @param notifier
	 *             通知user code的接口，此接口应引导用户到网页上进行授权。
	 * @return 响应
	 * @throws BaiduPcsAuthException
	 * @throws InterruptedException
	 *              当前线程被中断
	 */
	public TokenResponse newToken(final UserCodeNotifier notifier) throws BaiduPcsAuthException, InterruptedException {
		try {
			final DeviceCodeResponse deviceCodeRes = service.deviceCode(apiKey,
					BaiduPcsAuthService.RESPONSE_TYPE_DEVICE_CODE, BaiduPcsAuthService.SCOPE_NETDISK);

			Thread notifyThread = new Thread() {
				@Override
				public void run() {
					String verificationUrl = deviceCodeRes.getVerification_url() + "?code="
							+ deviceCodeRes.getUser_code();
					notifier.notify(deviceCodeRes.getUser_code(), verificationUrl, deviceCodeRes.getQrcode_url());
				}
			};
			notifyThread.setDaemon(true);
			notifyThread.start();

			TokenResponse tokenResponse = null;
			int interval = deviceCodeRes.getInterval();
			while (tokenResponse == null) {
				TimeUnit.SECONDS.sleep(interval);
				try {
					tokenResponse = service.deviceToken(BaiduPcsAuthService.GRANT_TYPE_DEVICE_TOKEN,
							deviceCodeRes.getDevice_code(), apiKey, secretKey);
				} catch (BaiduPcsAuthException e) {
					switch (e.getErrorCode()) {
					case AUTHORIZATION_PENDING:
						// 正在授权，再等会儿
						break;
					case SLOW_DOWN:
						// 太快了，慢点吧
						interval++;
						break;
					default:
						// 出错了
						throw e;
					}
				}
			}
			return tokenResponse;
		} catch (BaiduPcsAuthException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用Refresh Token刷新以获得新的Access Token。
	 * 
	 * @param refreshToken
	 *             必须参数，用于刷新Access Token用的Refresh Token。
	 * @return 响应
	 * @throws BaiduPcsAuthException
	 */
	public TokenResponse refreshToken(String refreshToken) throws BaiduPcsAuthException {
		try {
			return service.refreshToken(BaiduPcsAuthService.GRANT_TYPE_REFRESH_TOKEN, refreshToken, apiKey,
					secretKey, BaiduPcsAuthService.SCOPE_NETDISK);
		} catch (BaiduPcsAuthException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据配置文件的信息，确保Access token可用，即获取或刷新access token。<br>
	 * 配置：[api_key]API key；[secret_key]Secret key；[refresh_token]Refresh token。<br>
	 * 写入配置：[refresh_token]；[access_token]Access token；[expire_time]Expire
	 * time, in milliseconds.
	 * 
	 * @param propsFilePath
	 *             配置文件路径
	 * @param charset
	 *             配置文件使用的字符集
	 * @param notifier
	 *             通知user code的接口，此接口应引导用户到网页上进行授权。
	 * @return Access token和应用名
	 * @throws IOException
	 *              读写配置文件出现IO异常
	 * @throws BaiduPcsAuthException
	 * @throws InterruptedException
	 *              当前线程被中断
	 * @throws InvalidPropsFileException
	 *              配置文件不合法
	 */
	public static AccessToken validateTokenInPropsFile(Path propsFilePath, Charset charset, UserCodeNotifier notifier)
			throws IOException, BaiduPcsAuthException, InterruptedException, InvalidPropsFileException {
		Properties props = new Properties();
		try (Reader propsReader = Files.newBufferedReader(propsFilePath, charset)) {
			props.load(propsReader);
		}
		String apiKey = props.getProperty(PROP_KEY_API_KEY);
		String secretKey = props.getProperty(PROP_KEY_SECRET_KEY);
		String refreshToken = props.getProperty(PROP_KEY_REFRESH_TOKEN);
		String accessToken = props.getProperty(PROP_KEY_ACCESS_TOKEN);
		String expireTime = props.getProperty(PROP_KEY_EXPIRE_TIME);
		Date expireTimeDate = null;
		if (expireTime != null && !expireTime.isEmpty()) {
			try {
				expireTimeDate = new Date(Long.parseLong(expireTime));
			} catch (NumberFormatException e) {
				throw new InvalidPropsFileException(PROP_KEY_EXPIRE_TIME + " is not a valid time.");
			}
		}
		String appName = props.getProperty(PROP_KEY_APP_NAME);

		checkPropEmpty(PROP_KEY_APP_NAME, appName);
		checkPropEmpty(PROP_KEY_API_KEY, apiKey);
		checkPropEmpty(PROP_KEY_SECRET_KEY, secretKey);

		Date now = new Date();

		if (accessToken != null && !accessToken.isEmpty() && expireTimeDate != null && now.before(expireTimeDate))
			return new AccessToken(accessToken, appName);

		BaiduPcsAuth auth = new BaiduPcsAuth(apiKey, secretKey);
		TokenResponse token;
		if (refreshToken != null && !refreshToken.isEmpty()) {
			token = auth.refreshToken(refreshToken);
		} else {
			token = auth.newToken(notifier);
		}

		String newAccessToken = token.getAccess_token();
		long newExpireTime = now.getTime() + TimeUnit.MILLISECONDS.convert(token.getExpires_in(), TimeUnit.SECONDS);
		String newRefreshToken = token.getRefresh_token();
		props.setProperty(PROP_KEY_ACCESS_TOKEN, newAccessToken);
		props.setProperty(PROP_KEY_EXPIRE_TIME, Long.toString(newExpireTime));
		props.setProperty(PROP_KEY_REFRESH_TOKEN, newRefreshToken);
		try (Writer propsWriter = Files.newBufferedWriter(propsFilePath, charset)) {
			props.store(propsWriter, "Baidupcs authorization infomation.");
		}

		return new AccessToken(newAccessToken, appName);
	}

	private static void checkPropEmpty(String key, String value) throws InvalidPropsFileException {
		if (value == null || value.isEmpty())
			throw new InvalidPropsFileException(key + " should not be empty.");
	}

	/**
	 * 用来回调通知需要用户授权使用的信息。
	 * 
	 * @author blove
	 */
	public interface UserCodeNotifier {
		/**
		 * 通知信息。
		 * 
		 * @param userCode
		 *             User Code，设备需要展示给用户。
		 * @param verificationUrl
		 *             用户填写User Code并进行授权的url，设备需要展示给用户。
		 * @param qrCodeUrl
		 *             用于二维码登陆的Qr Code图片url，用户用智能终端扫描该二维码之后，可直接进入登陆授权页面。
		 */
		void notify(String userCode, String verificationUrl, String qrCodeUrl);
	}

	/**
	 * Access token和应用名。
	 * 
	 * @author blove
	 */
	public static class AccessToken {
		private String token;
		private String appName;

		AccessToken(String token, String appName) {
			this.token = token;
			this.appName = appName;
		}

		/**
		 * 返回Access token。
		 * 
		 * @return
		 */
		public String getToken() {
			return token;
		}

		/**
		 * 
		 * @return
		 */
		public String getAppName() {
			return appName;
		}
	}

}
