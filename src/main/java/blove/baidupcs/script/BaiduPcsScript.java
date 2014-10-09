package blove.baidupcs.script;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import retrofit.RestAdapter.LogLevel;
import retrofit.mime.TypedInput;
import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsException;
import blove.baidupcs.api.error.BaiduPcsFileExistsException;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.error.BaiduPcsMD5NotExistsException;
import blove.baidupcs.api.error.ErrorResponse;
import blove.baidupcs.api.request.OnDup;
import blove.baidupcs.api.request.RapidUploadRecogInfo;
import blove.baidupcs.api.response.CloudDownloadMeta;
import blove.baidupcs.api.response.CloudDownloadProgress;
import blove.baidupcs.api.response.FileMeta;
import blove.baidupcs.api.response.Quota;
import blove.baidupcs.api.response.CloudDownloadBasic.Status;
import blove.baidupcs.util.ProgressInputStream;
import blove.baidupcs.util.ProgressObserver;

/**
 * 百度个人云存储的脚本语言形式实现。实现{@link ScriptEngine}接口。
 * 
 * @author blove
 */
public class BaiduPcsScript extends AbstractScriptEngine {
	/**
	 * Attribute项，当前BaiduPcs实例。
	 */
	public static final String BAIDUPCS = "blove.baidupcs.baidupcs";

	/**
	 * 当前目录。类型为String。
	 */
	public static final String CURR_DIR = "blove.baidupcs.curr_dir";

	/**
	 * 当前行数。执行一个命令时实时设置，供命令方法使用。类型为Integer。
	 */
	private static final String LINE_NUMBER = "blove.baidupcs.line_number";

	private static final String INNER_DIR = "/.tmp";
	private static final String CDOWNGET_TMPDIR = INNER_DIR + "/cdownget";

	private final BaiduPcsScriptFactory factory;

	/**
	 * 新建一个实例。
	 * 
	 * @param factory
	 *             工厂
	 */
	protected BaiduPcsScript(BaiduPcsScriptFactory factory) {
		this.factory = factory;
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		return eval(new StringReader(script), context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		try {
			int lineNumber = 1;
			try {
				BufferedReader br = new BufferedReader(reader);
				String line;
				while ((line = br.readLine()) != null) {
					// 读取到了一个命令

					// 提取命令名和参数
					String[] nameAndArgs = line.trim().split("\\s+", 2);
					String name = nameAndArgs[0];
					String argsStr = nameAndArgs.length > 1 ? nameAndArgs[1] : null;

					// 根据注解找到合适的方法
					Method rightMethod = null;
					for (Method method : BaiduPcsScript.class.getDeclaredMethods()) {
						Command commandAnno = method.getAnnotation(Command.class);
						if (commandAnno != null) {
							if (name.equals(method.getName())
									|| Arrays.asList(commandAnno.aliases()).contains(name)) {
								rightMethod = method;
								break;
							}
						}
					}

					if (rightMethod == null) {
						// 没找到命令方法
						throw new ScriptException("Unrecognized command: " + line, null, lineNumber);
					}

					// 将命令参数分解成方法参数
					int paramCount = rightMethod.getParameterTypes().length;
					Object[] params = new Object[paramCount];
					params[0] = context;
					if (paramCount > 1) {
						String[] args = argsStr == null ? new String[] {} : argsStr.split("\\s+", paramCount - 1);
						System.arraycopy(args, 0, params, 1, args.length);
					}

					// 设置context，调用命令方法
					context.setAttribute(LINE_NUMBER, lineNumber, ScriptContext.ENGINE_SCOPE);
					rightMethod.invoke(this, params);

					lineNumber++;
				}
			} catch (IOException e) {
				throw new ScriptException("Failed to read script.", null, lineNumber);
			} catch (InvocationTargetException e) {
				handleInvocationTargetException(e, lineNumber);
			} catch (Exception e) {
				throw new ScriptException(e);
			}
		} catch (ScriptException e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty())
				getErrorOutput(context).println(e.getMessage());
			else
				throw e;
		}
		return null;
	}

	private void handleInvocationTargetException(InvocationTargetException e, int lineNumber) throws ScriptException {
		Throwable cause = e.getCause();
		if (cause != null) {
			if (cause instanceof ScriptException)
				throw (ScriptException) cause;
			// else if (cause instanceof IllegalArgumentException)
			// throw new ScriptException("Bad params: " +
			// e.getMessage(),
			// null, lineNumber);
			else if (cause instanceof BaiduPcsException) {
				String message = null;
				BaiduPcsException be = (BaiduPcsException) cause;
				ErrorResponse errorRes = be.getErrorResponse();
				if (errorRes != null)
					message = errorRes.getError_msg();
				else if (be.getHttpReason() != null && !be.getHttpReason().isEmpty())
					message = be.getHttpReason();

				if (message != null)
					throw new ScriptException(message, null, lineNumber);
				else
					throw new ScriptException(be);
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else
				throw new ScriptException((Exception) cause);
		} else {
			throw new ScriptException(e);
		}
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	/**
	 * 脚本命令方法的注解。方法第一个参数必须是ScriptContext，后面的参数必须是若干个String，对应命令参数。
	 * 
	 * @author blove
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Command {
		/**
		 * 命令名称。方法名本身也作为一个命令名称。
		 * 
		 * @return 该命令的若干个名称
		 */
		String[] aliases() default {};
	}

	@Command
	private void help(ScriptContext context) {
		PrintWriter out = getOutput(context);
		List<String> cmdNames = new LinkedList<>();
		for (Method method : BaiduPcsScript.class.getDeclaredMethods()) {
			Command commandAnno = method.getAnnotation(Command.class);
			if (commandAnno != null) {
				StringBuilder name = new StringBuilder(method.getName());
				for (String alias : commandAnno.aliases())
					name.append("/").append(alias);
				cmdNames.add(name.toString());
			}
		}
		Collections.sort(cmdNames);
		for (String name : cmdNames)
			out.println(name);
	}

	@Command
	private void open(ScriptContext context, String accessToken, String appName) throws BaiduPcsException,
			IOException, ScriptException {
		BaiduPcs pcs = new BaiduPcs(accessToken, appName, LogLevel.BASIC);
		pcs.list("/");// 验证是否有权限
		context.setAttribute(CURR_DIR, "/", ScriptContext.ENGINE_SCOPE);// 重置目录
		context.setAttribute(BAIDUPCS, pcs, ScriptContext.ENGINE_SCOPE);
		getOutput(context).println("Open.");
	}

	@Command
	private void cd(ScriptContext context, String path) throws ScriptException, BaiduPcsException, IOException {
		BaiduPcs pcs = checkBaiduPcs(context);
		if (path == null || path.isEmpty())
			return;
		String absolutePath = getAbsolutePath(context, path);
		if (!pcs.meta(absolutePath).isDir())
			// 验证是否是目录
			getErrorOutput(context).print("Not dir.");
		context.setAttribute(CURR_DIR, absolutePath, ScriptContext.ENGINE_SCOPE);
	}

	@Command
	private void cdup(ScriptContext context) throws ScriptException, BaiduPcsException, IOException {
		checkBaiduPcs(context);
		String crtPath = getCurrDir(context);
		int slashIndex = crtPath.lastIndexOf("/");
		cd(context, crtPath.substring(0, slashIndex + 1));
	}

	@Command
	private void pwd(ScriptContext context) throws ScriptException {
		checkBaiduPcs(context);
		String crtPath = getCurrDir(context);
		getOutput(context).println(crtPath);
	}

	@Command
	private void lpwd(ScriptContext context) {
		getOutput(context).println(Paths.get("").toAbsolutePath());
	}

	@Command
	private void quota(ScriptContext context) throws ScriptException, BaiduPcsException, IOException {
		BaiduPcs pcs = checkBaiduPcs(context);
		Quota quota = pcs.quota();
		boolean h = true;// 暂不支持选项
		PrintWriter out = getOutput(context);
		out.println("Total:\t" + (h ? sizeH(quota.getQuota()) : quota.getQuota()));
		out.println("Used:\t" + (h ? sizeH(quota.getUsed()) : quota.getUsed()));
		long free = quota.getQuota() - quota.getUsed();
		out.println("Free:\t" + (h ? sizeH(free) : free));
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Command(aliases = { "ls" })
	private void list(ScriptContext context, String path) throws ScriptException, BaiduPcsException, IOException {
		BaiduPcs pcs = checkBaiduPcs(context);
		String absolutePath = getAbsolutePath(context, path);

		List<? extends FileMeta> files = pcs.list(absolutePath);
		PrintWriter out = getOutput(context);
		for (FileMeta file : files) {
			String type = file.isDir() ? "D" : "F";
			String time = DATE_FORMAT.format(new Date(file.getMtime() * 1000));
			String size = file.isDir() ? "-" : sizeH(file.getSize());
			String name = file.getFileName();
			out.format("%-3s%-20s%10s %s\n", type, time, size, name);
		}
	}

	@Command(aliases = { "echo" })
	private void print(ScriptContext context, String text) {
		PrintWriter out = getOutput(context);
		if (text != null)
			out.println(text);
		else
			out.println();
	}

	/**
	 * 上传文件/递归上传目录。不覆盖已存在文件/目录。
	 * 
	 * @param context
	 * @param localFile
	 *             本地文件/目录路径
	 * @param pcsPath
	 *             pcs中的文件/目录或上传到的目录路径。默认为当前目录下同名文件/目录。
	 * @throws ScriptException
	 * @throws IOException
	 */
	@Command(aliases = { "up" })
	private void upload(final ScriptContext context, final String localFile, String pcsPath) throws ScriptException,
			IOException {
		final PrintWriter out = getOutput(context);
		PrintWriter errorOut = getErrorOutput(context);

		final BaiduPcs pcs = checkBaiduPcs(context);
		final Path localOriPath = Paths.get(localFile);
		String absolutePcsPath = getAbsolutePath(context, pcsPath);

		// 如果absolutePcsPath是已存在的目录，则上传到此目录下。否则认为是上传为此名的文件/目录。
		absolutePcsPath = genePcsFilePath(context, absolutePcsPath,
				localOriPath.equals(localOriPath.getRoot()) ? "ROOT" : localOriPath.getFileName().toString());
		if (absolutePcsPath == null) {
			out.println("Skip exists file: " + absolutePcsPath);
			return;
		}

		final String pcsOriPath = absolutePcsPath;

		if (Files.isDirectory(localOriPath)) {
			Files.walkFileTree(localOriPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
					new SimpleFileVisitor<Path>() {

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {
							String pcsCurrPath = genePcsPath(localOriPath, dir, pcsOriPath);
							pcs.mkdir(pcsCurrPath);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							String pcsCurrPath = genePcsPath(localOriPath, file, pcsOriPath);
							try {
								uploadFile(context, file, pcsCurrPath);
							} catch (ScriptException e) {
								throw new IOException(e);
							}
							return FileVisitResult.CONTINUE;
						}

					});
		} else if (Files.isRegularFile(localOriPath)) {
			uploadFile(context, localOriPath, absolutePcsPath);
		} else {
			errorOut.println("File type is unsupported.");
		}
	}

	/**
	 * 根据localOriPath->localCurrPath的相对路径得出以pcsOriPath为原路径的“pcsCurrPath”。
	 * 
	 * @param localOriPath
	 * @param localCurrPath
	 * @param pcsOriPath
	 * @return
	 */
	private String genePcsPath(Path localOriPath, Path localCurrPath, String pcsOriPath) {
		StringBuilder pcsCurrPath = new StringBuilder(pcsOriPath);
		for (Path pathItem : localOriPath.relativize(localCurrPath)) {
			if (pcsCurrPath.lastIndexOf("/") != pcsCurrPath.length() - 1)
				pcsCurrPath.append("/");
			pcsCurrPath.append(pathItem);
		}
		return pcsCurrPath.toString();
	}

	private void uploadFile(final ScriptContext context, Path localPath, final String absolutePcsPath)
			throws ScriptException, IOException {
		final PrintWriter out = getOutput(context);

		// 先尝试秒传，失败再以普通方式上传
		out.print("HASHING \t" + absolutePcsPath);
		out.flush();
		if (tryRapidUploadFile(context, localPath, absolutePcsPath)) {
			// 秒传成功
			out.println("\rRAPID \t" + absolutePcsPath);
			return;
		}

		BaiduPcs pcs = checkBaiduPcs(context);

		// 设一个允许显示进度的标志。避免在监视器多线程通知的情况下出问题，比如上传完了进度还没显示完。
		final String RATE_PRINT_ALLOWED = "blove.baidupcs.rate_print_allowed." + Thread.currentThread().getId();
		context.setAttribute(RATE_PRINT_ALLOWED, 1, ScriptContext.ENGINE_SCOPE);

		try (InputStream in = new BufferedInputStream(Files.newInputStream(localPath))) {
			long length = Files.size(localPath);
			ProgressInputStream pin = new ProgressInputStream(in, length);
			pin.addObserver(new ProgressObserver() {

				@Override
				public void update(ProgressInputStream stream, double rate) {
					if (context.getAttribute(RATE_PRINT_ALLOWED) != null) {
						out.print("\r" + (int) (rate * 100) + "%   \t" + absolutePcsPath);
						out.flush();
					}
				}
			});

			pcs.upload(absolutePcsPath, pin, length, OnDup.EXCEPTION);
		}

		context.removeAttribute(RATE_PRINT_ALLOWED, ScriptContext.ENGINE_SCOPE);
		out.println();
	}

	/**
	 * 尝试秒传文件。
	 * 
	 * @param context
	 * @param localPath
	 *             文件本地路径
	 * @param absolutePcsPath
	 *             上传为文件路径
	 * @return 成功返回true；失败返回false。
	 * @throws IOException
	 * @throws ScriptException
	 */
	private boolean tryRapidUploadFile(final ScriptContext context, Path localPath, final String absolutePcsPath)
			throws IOException, ScriptException {
		if (Files.size(localPath) <= 1024 * 256)
			// 256K及以下的文件不使用快速上传（REST API的规定）
			return false;

		BaiduPcs pcs = checkBaiduPcs(context);

		RapidUploadRecogInfo param;
		try (InputStream in = new BufferedInputStream(Files.newInputStream(localPath))) {
			param = RapidUploadRecogInfo.fromInputStream(in);
		}
		if (param == null)
			return false;

		try {
			pcs.rapidUpload(absolutePcsPath, param, OnDup.EXCEPTION);
		} catch (BaiduPcsMD5NotExistsException e) {
			return false;
		}

		return true;
	}

	@Command(aliases = { "down" })
	private void download(ScriptContext context, String pcsPath, String localFile) throws ScriptException,
			BaiduPcsException, IOException {
		PrintWriter out = getOutput(context);

		BaiduPcs pcs = checkBaiduPcs(context);
		Path localPath = Paths.get(localFile == null ? "" : localFile);
		String pcsOriPath = getAbsolutePath(context, pcsPath);

		// 如果localPath是已存在的目录，则下载到此目录下。否则认为是下载为此名的文件/目录。
		String pcsFileName = pcsOriPath.equals("/") ? "ROOT" : pcsOriPath.substring(pcsOriPath.lastIndexOf("/") + 1);
		localPath = geneLocalFilePath(context, localPath, pcsFileName);
		if (localPath == null) {
			out.println("Skip exists file: " + localPath);
			return;
		}

		FileMeta meta = pcs.meta(pcsOriPath);
		if (meta.isDir())
			downloadDir(context, pcsOriPath, localPath);
		else
			downloadFile(context, pcsOriPath, localPath);
	}

	private void downloadDir(ScriptContext context, String pcsAbsolutePath, Path localPath) throws BaiduPcsException,
			IOException, ScriptException {
		Files.createDirectory(localPath);

		BaiduPcs pcs = checkBaiduPcs(context);
		for (FileMeta file : pcs.list(pcsAbsolutePath)) {
			String pcsFilePath = file.getPathInApp();
			Path localFilePath = localPath.resolve(file.getFileName());
			if (file.isDir())
				downloadDir(context, pcsFilePath, localFilePath);
			else
				downloadFile(context, pcsFilePath, localFilePath);
		}
	}

	private void downloadFile(final ScriptContext context, String pcsAbsolutePath, final Path localPath)
			throws ScriptException, BaiduPcsException, IOException {
		final PrintWriter out = getOutput(context);

		BaiduPcs pcs = checkBaiduPcs(context);
		TypedInput typedInput = pcs.download(pcsAbsolutePath);

		// 设一个允许显示进度的标志。避免在监视器多线程通知的情况下出问题，比如上传完了进度还没显示完。
		final String RATE_PRINT_ALLOWED = "blove.baidupcs.rate_print_allowed." + Thread.currentThread().getId();
		context.setAttribute(RATE_PRINT_ALLOWED, 1, ScriptContext.ENGINE_SCOPE);

		try (InputStream in = typedInput.in()) {
			ProgressInputStream pin = new ProgressInputStream(in, typedInput.length());
			pin.addObserver(new ProgressObserver() {

				@Override
				public void update(ProgressInputStream stream, double rate) {
					if (context.getAttribute(RATE_PRINT_ALLOWED) != null) {
						out.print("\r" + (int) (rate * 100) + "% " + localPath);
						out.flush();
					}
				}
			});

			Files.copy(pin, localPath);
		}

		context.removeAttribute(RATE_PRINT_ALLOWED, ScriptContext.ENGINE_SCOPE);
		out.println();
	}

	@Command
	private void rm(ScriptContext context, String path) throws BaiduPcsException, IOException, ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		String absolutePath = getAbsolutePath(context, path);
		baiduPcs.delete(absolutePath);
	}

	@Command
	private void search(ScriptContext context, String keyword, String dir) throws BaiduPcsException, IOException,
			ScriptException {
		innerSearch(context, keyword, dir, false);
	}

	@Command
	private void searchr(ScriptContext context, String keyword, String dir) throws BaiduPcsException, IOException,
			ScriptException {
		innerSearch(context, keyword, dir, true);
	}

	private void innerSearch(ScriptContext context, String keyword, String dir, boolean recursively)
			throws BaiduPcsException, IOException, ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		String absolutePath = getAbsolutePath(context, dir);
		List<? extends FileMeta> files = baiduPcs.search(absolutePath, keyword, recursively);

		PrintWriter out = getOutput(context);
		for (FileMeta file : files) {
			out.println(file.getPathInApp());
		}
	}

	/**
	 * 输出文本文件内容。
	 * 
	 * @param context
	 * @param file
	 *             文件路径
	 * @param charset
	 *             编码。默认为当前系统默认编码。
	 * @throws BaiduPcsException
	 * @throws IOException
	 * @throws ScriptException
	 */
	@Command
	private void cat(ScriptContext context, String file, String charset) throws BaiduPcsException, IOException,
			ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		PrintWriter out = getOutput(context);
		String absolutePath = getAbsolutePath(context, file);

		try (InputStream in = baiduPcs.download(absolutePath).in()) {
			BufferedReader reader;
			if (charset != null)
				reader = new BufferedReader(new InputStreamReader(in, charset));
			else
				reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null)
				out.println(line);
		}
	}

	@Command(aliases = { "cdownl" })
	private void cdownlist(ScriptContext context) throws BaiduPcsException, IOException, ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		PrintWriter out = getOutput(context);
		Map<String, CloudDownloadMeta> metaMap = baiduPcs.cloudDownloadMeta();
		if (metaMap.isEmpty()) {
			out.println("NO TASK.");
			return;
		}
		Map<String, CloudDownloadProgress> progressMap = baiduPcs.cloudDownloadProgress(new ArrayList<>(metaMap
				.keySet()));
		for (CloudDownloadMeta meta : metaMap.values()) {
			CloudDownloadProgress progress = progressMap.get(meta.getTaskID());

			out.format(
					"%s  %s  %d(%s)/%d(%s)  START AT: %s\n",
					meta.getTaskID(),
					meta.getStatus(),
					progress.getFinishedSize(),
					sizeH(progress.getFinishedSize()),
					progress.getFileSize(),
					sizeH(progress.getFileSize()),
					progress.getStartTime() == 0 ? "UNKNOWN" : DATE_FORMAT.format(new Date(progress
							.getFinishTime() * 1000)));
			out.println("SAVE AS: " + meta.getSavePathInApp());
			out.println("SOURCE: " + meta.getSourceUrl());
			out.println();
		}
	}

	@Command(aliases = { "cdownw" })
	private void cdownandwait(ScriptContext context, String url, String dir) throws BaiduPcsException, IOException,
			ScriptException, InterruptedException {
		CloudDownloadMeta meta = innerCdownandwait(context, url, dir);
		PrintWriter errorOut = getErrorOutput(context);
		Status status = meta.getStatus();
		if (status != Status.SUCCESS) {
			errorOut.println("Failed: " + status);
			return;
		}
	}

	private CloudDownloadMeta innerCdownandwait(ScriptContext context, String url, String dir)
			throws BaiduPcsException, IOException, InterruptedException, ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		PrintWriter out = getOutput(context);

		String taskID = baiduPcs.cloudDownloadStart(CDOWNGET_TMPDIR, url);
		boolean finished = false;
		CloudDownloadMeta retMeta = null;
		while (!finished) {
			CloudDownloadProgress progress = baiduPcs.cloudDownloadProgress(taskID);
			switch (progress.getStatus()) {
			case DOWNLOADING:
				out.println("\r" + progress.getFinishedSize() + "(" + sizeH(progress.getFinishedSize()) + ")/"
						+ progress.getFileSize() + "(" + sizeH(progress.getFileSize()) + ")");
				TimeUnit.SECONDS.sleep(3);
				break;
			case SUCCESS:
				out.println();
				finished = true;
				retMeta = baiduPcs.cloudDownloadMeta(taskID);
				break;
			default:
				out.println();
				finished = true;
				retMeta = baiduPcs.cloudDownloadMeta(taskID);
			}
		}
		return retMeta;
	}

	@Command(aliases = { "cdowns" })
	private void cdownstart(ScriptContext context, String url, String dir) throws ScriptException, BaiduPcsException,
			IOException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		String absolutePath = getAbsolutePath(context, dir);

		baiduPcs.cloudDownloadStart(absolutePath, url);
	}

	@Command(aliases = { "cdownc" })
	private void cdowncancel(ScriptContext context, String taskID) throws BaiduPcsException, IOException,
			ScriptException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		baiduPcs.cloudDownloadCancel(taskID);
	}

	@Command(aliases = { "cdowng" })
	private void cdownget(ScriptContext context, String url, String localPath) throws BaiduPcsException, IOException,
			ScriptException, InterruptedException {
		BaiduPcs baiduPcs = checkBaiduPcs(context);
		PrintWriter out = getOutput(context);
		PrintWriter errorOut = getErrorOutput(context);

		try {
			baiduPcs.mkdir(INNER_DIR);
		} catch (BaiduPcsFileExistsException e) {
		}
		try {
			baiduPcs.mkdir(CDOWNGET_TMPDIR);
		} catch (BaiduPcsFileExistsException e) {
		}

		out.println("Cloud downloading...");
		CloudDownloadMeta cdMeta = innerCdownandwait(context, url, CDOWNGET_TMPDIR);
		Status status = cdMeta.getStatus();
		if (status != Status.SUCCESS) {
			errorOut.println("Cloud download failed: " + status);
			return;
		}

		String filePath = cdMeta.getSavePathInApp();
		out.println("Local downloading...");
		download(context, filePath, localPath);

		out.println("Delete temp file...");
		baiduPcs.delete(filePath);

		out.println("Done.");
	}

	private BaiduPcs checkBaiduPcs(ScriptContext context) throws ScriptException {
		Object valueObject = context.getAttribute(BAIDUPCS);
		if (valueObject != null && valueObject instanceof BaiduPcs)
			return (BaiduPcs) valueObject;
		else
			throw new ScriptException("Not open.", null, ((Integer) context.getAttribute(LINE_NUMBER)).intValue());
	}

	private String getCurrDir(ScriptContext context) {
		Object path = context.getAttribute(CURR_DIR);
		if (path == null || !(path instanceof String))
			return "/";
		else
			return (String) path;
	}

	/**
	 * 根据给定路径返回绝对路径。如果给定的是绝对路径则直接返回；如果给定的是null或空串，则返回当前目录路径；如果给定的是相对路径，
	 * 则返回相对于当前目录的绝对路径。
	 * 
	 * @param context
	 * @param givenPath
	 *             给定路径
	 * @return 绝对路径。非根目录不以"/"结尾。
	 */
	private String getAbsolutePath(ScriptContext context, String givenPath) {
		String currDir = getCurrDir(context);
		String retDir;
		if (givenPath == null || givenPath.isEmpty()) {
			retDir = currDir;
		} else if (givenPath.startsWith("/")) {
			retDir = givenPath;
		} else {
			if (!currDir.endsWith("/"))
				currDir = currDir + "/";
			retDir = currDir + givenPath;
		}
		while (retDir.endsWith("/"))
			retDir = retDir.substring(0, retDir.length() - 1);
		return retDir;
	}

	private static DecimalFormat sizeHFormat = new DecimalFormat("#.0");

	/**
	 * 将字节大小转换为合适的单位表示。
	 * 
	 * @param size
	 *             字节数
	 * @return 表示
	 */
	private String sizeH(long size) {
		String[] units = { "", "K", "M", "G", "T" };
		double sizeH = size;
		int unitIndex;
		for (unitIndex = 0; unitIndex < units.length; unitIndex++) {
			sizeH = size / Math.pow(1024, unitIndex);
			if (sizeH < 1024) {
				break;
			}
		}
		return (unitIndex == 0 ? size : sizeHFormat.format(sizeH)) + units[unitIndex];
	}

	/**
	 * 生成一个在pcs上的文件路径。当需要将文件上传/离线下载/复制到pcs中，可以调用此方法，将用户给定的路径转换为将要生成的文件路径。<br>
	 * 规则是： <li>当给定absolutePcsPath不存在时，直接返回之； <li>
	 * 当给定absolutePcsPath存在，且是目录时，则将给定文件名fileName拼接在此路径下并返回； <li>
	 * 当给定absolutePcsPath存在，且是文件时，则返回null。
	 * 
	 * @param context
	 * @param absolutePcsPath
	 * @param fileName
	 * @return
	 * @throws BaiduPcsException
	 * @throws IOException
	 * @throws ScriptException
	 */
	private String genePcsFilePath(ScriptContext context, String absolutePcsPath, String fileName)
			throws BaiduPcsException, IOException, ScriptException {
		BaiduPcs pcs = checkBaiduPcs(context);
		PrintWriter out = getOutput(context);

		// 如果absolutePcsPath是已存在的目录，则上传到此目录下。否则认为是上传为此名的文件/目录。
		try {
			FileMeta meta = pcs.meta(absolutePcsPath);
			if (meta.isDir()) {
				// 存在，是目录
				absolutePcsPath += "/" + fileName;
				try {
					pcs.meta(absolutePcsPath);
					// 存在，忽略
					out.println("Skip exists file: " + absolutePcsPath);
					return null;
				} catch (BaiduPcsFileNotExistsException e) {
					// 不存在，就这样了
				}
			} else {
				// 存在，是文件，不覆盖，忽略
				out.println("Skip exists file: " + absolutePcsPath);
				return null;
			}
		} catch (BaiduPcsFileNotExistsException e) {
			// 不存在，就这样了
		}
		return absolutePcsPath;
	}

	/**
	 * 参考{@link #genePcsFilePath(ScriptContext, String, String)}
	 * 的说明，与之不同的是此方法给定并生成的均是本地路径。
	 */
	private Path geneLocalFilePath(ScriptContext context, Path localPath, String fileName) {
		if (Files.isDirectory(localPath)) {
			// 存在，是目录
			localPath = localPath.resolve(fileName);
		} else if (Files.exists(localPath)) {
			// 存在，是文件，不覆盖，忽略
			return null;
		} else {
			// 不存在，就这样了
		}
		return localPath;
	}

	/**
	 * 获取PrintWriter类型的标准输出流。
	 * 
	 * @param context
	 *             ScriptContext
	 * @return PrintWriter标准输出
	 */
	private PrintWriter getOutput(ScriptContext context) {
		Writer writer = context.getWriter();
		if (writer instanceof PrintWriter)
			return (PrintWriter) writer;
		else
			return new PrintWriter(writer, true);
	}

	/**
	 * 获取PrintWriter类型的标准错误输出流。
	 * 
	 * @param context
	 *             ScriptContext
	 * @return PrintWriter标准输出
	 */
	private PrintWriter getErrorOutput(ScriptContext context) {
		Writer writer = context.getErrorWriter();
		if (writer instanceof PrintWriter)
			return (PrintWriter) writer;
		else
			return new PrintWriter(writer, true);
	}

}
