package blove.baidupcs.script;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * BaiduPcsScript脚本引擎工厂。
 * 
 * @author blove
 */
public class BaiduPcsScriptFactory implements ScriptEngineFactory {
	/**
	 * 脚本引擎名称（1）。
	 */
	public static final String NAME_1 = "baidupcsscript";
	/**
	 * 脚本引擎名称（2）。
	 */
	public static final String NAME_2 = "baidupcs";
	/**
	 * 脚本引擎名称（3）。
	 */
	public static final String NAME_3 = "bps";

	@Override
	public String getEngineName() {
		return "Baidu PCS Script Engine";
	}

	@Override
	public String getEngineVersion() {
		return "0.1";
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList("baidupcs", "bps");
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList();
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList(NAME_1, NAME_2, NAME_3);
	}

	@Override
	public String getLanguageName() {
		return "baidupcsscript";
	}

	@Override
	public String getLanguageVersion() {
		return "0.1";
	}

	@Override
	public Object getParameter(String key) {
		switch (key) {
		case ScriptEngine.ENGINE:
			return getEngineName();
		case ScriptEngine.ENGINE_VERSION:
			return getEngineVersion();
		case ScriptEngine.LANGUAGE:
			return getLanguageName();
		case ScriptEngine.LANGUAGE_VERSION:
			return getLanguageVersion();
		case ScriptEngine.NAME:
			return getNames();
		case "THREADING":
			return "MULTITHREADED";
		default:
			return null;
		}
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		throw new UnsupportedOperationException("Method call is not supported.");
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print " + toDisplay;
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder program = new StringBuilder();
		for (int i = 0; i < statements.length; i++) {
			program.append(statements[i]);
			if (i < statements.length - 1)
				program.append("\n");
		}
		return program.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new BaiduPcsScript(this);
	}

}
