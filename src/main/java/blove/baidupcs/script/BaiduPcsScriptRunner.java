package blove.baidupcs.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import blove.baidupcs.api.BaiduPcsAuth;
import blove.baidupcs.api.BaiduPcsAuth.AccessToken;
import blove.baidupcs.api.BaiduPcsAuth.UserCodeNotifier;
import blove.baidupcs.api.error.auth.BaiduPcsAuthException;

/**
 * 执行BaiduPcsScript的主程序入口，通过标准输入传入并执行脚本语句。每个语句会有提示符，适合键盘输入。<br>
 * 参数：配置文件路径。
 * 
 * @author blove
 */
public class BaiduPcsScriptRunner {

	public static void main(String[] args) {
		if (args.length < 1) {
			exitWithError("Illegal arguements!");
		}

		String propsFilePath = args[0];
		AccessToken accessToken = null;
		try {
			accessToken = BaiduPcsAuth.validateTokenInPropsFile(Paths.get(propsFilePath), Charset.defaultCharset(),
					new UserCodeNotifier() {
						@Override
						public void notify(String userCode, String verificationUrl, String qrCodeUrl) {
							System.out.println("User Code: " + userCode);
							System.out.println("Verification URL: " + verificationUrl);
							System.out.println("QR Code URL: " + qrCodeUrl);
						}
					});
		} catch (BaiduPcsAuthException e) {
			exitWithError(e.getErrorCode() + ": " + e.getErrorDescription());
		} catch (Exception e) {
			exitWithError("Error: " + e.getMessage());
		}

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName(BaiduPcsScriptFactory.NAME_1);
		if (engine == null) {
			exitWithError("Cannot found script engine!");
		}

		try {
			engine.eval("open " + accessToken.getToken() + " " + accessToken.getAppName());
		} catch (ScriptException e) {
			exitWithError("Not open: " + e.getMessage());
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			printPrompt(engine);
			while ((line = reader.readLine()) != null) {
				try {
					engine.eval(line);
				} catch (ScriptException e) {
					System.err.println("Error: " + e.getMessage());
				}
				printPrompt(engine);
			}
		} catch (IOException e) {
			exitWithError("Error reading command. " + e.getMessage());
		}
	}

	private static void printPrompt(ScriptEngine engine) {
		Object currDir = engine.getContext().getAttribute(BaiduPcsScript.CURR_DIR);
		if (currDir instanceof String)
			System.out.print(currDir);
		System.out.print("> ");
	}

	private static void exitWithError(String message) {
		System.err.println(message);
		System.exit(1);
	}
}
