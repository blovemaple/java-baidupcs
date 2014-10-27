package blove.baidupcs.script;

import java.nio.charset.Charset;
import java.nio.file.Paths;

import blove.baidupcs.api.BaiduPcsAuth;
import blove.baidupcs.api.BaiduPcsAuth.UserCodeNotifier;
import blove.baidupcs.api.error.auth.BaiduPcsAuthException;

/**
 * Baidu PCS授权程序，根据参数指定的配置文件的信息，确保Access token可用，即获取或刷新access token。<br>
 * 参数：配置文件路径。<br>
 * 配置：[api_key]API key；[secret_key]Secret key；[refresh_token]Refresh token。<br>
 * 写入配置：[refresh_token]；[access_token]Access token；[expire_time]Expire time, in
 * milliseconds.
 * 
 * @author blove
 */
public class BaiduPcsAuthRunner {
	public static void main(String[] args) {
		if (args.length < 1) {
			exitWithError("Illegal arguements!");
		}

		String propsFilePath = args[0];

		try {
			BaiduPcsAuth.validateTokenInPropsFile(Paths.get(propsFilePath), Charset.defaultCharset(),
					new UserCodeNotifier() {
						@Override
						public void notify(String userCode, String verificationUrl, String qrCodeUrl) {
							System.out.println("User Code: " + userCode);
							System.out.println("Verification URL: " + verificationUrl);
							System.out.println("QR Code URL: " + qrCodeUrl);
						}
					});
			System.out.println("Success.");
		} catch (BaiduPcsAuthException e) {
			exitWithError(e.getErrorCode() + ": " + e.getErrorDescription());
		} catch (Exception e) {
			exitWithError("Error: " + e.getMessage());
		}
	}

	private static void exitWithError(String message) {
		System.err.println(message);
		System.exit(1);
	}

}
