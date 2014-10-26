package blove.baidupcs.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 执行BaiduPcsScript的主程序入口，通过标准输入传入并执行脚本语句。每个语句会有提示符，适合键盘输入。
 * 
 * @author blove
 */
public class BaiduPcsScriptRunner {

	public static void main(String[] args) {
		String openCommand = parseOpenCommand(args);

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory
				.getEngineByName(BaiduPcsScriptFactory.NAME_1);
		if (engine == null) {
			exitWithError("Cannot found script engine!");
		}

		if (openCommand != null) {
			try {
				engine.eval(openCommand);
			} catch (ScriptException e) {
				System.err.println("Not open: " + e.getMessage());
			}
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				try {
					engine.eval(line);
				} catch (ScriptException e) {
					System.err.println("Error: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			exitWithError("Error reading command. " + e.getMessage());
		}
	}

	private static String parseOpenCommand(String[] args) {
		if (args.length == 0)
			return null;
		StringBuilder command = new StringBuilder("open");
		for (String arg : args) {
			command.append(' ').append(arg);
		}
		return command.toString();
	}

	private static void exitWithError(String message) {
		System.err.println(message);
		System.exit(1);
	}
}
