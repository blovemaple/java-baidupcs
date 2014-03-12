package blove.baidupcs.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import retrofit.RestAdapter.LogLevel;
import blove.baidupcs.fs.util.Globs;

public class BaiduPcsFileSystem extends FileSystem {
	private static final String SEPERATOR = "/";

	private final BaiduPcsFileSystemProvider provider;
	private final BaiduPcsFileStore store;
	private final String appName;
	private final String[] dir;

	/**
	 * @param provider
	 * @param accessToken
	 *            开发者标识
	 * @param appName
	 *            应用名称
	 * @param dir
	 *            文件系统根目录路径，此路径以应用目录为根目录。null或空为应用目录。
	 * @param logLevel
	 *            打印的日志级别
	 * 
	 * @throws IOException
	 */
	BaiduPcsFileSystem(BaiduPcsFileSystemProvider provider, String accessToken,
			String appName, List<String> dir, LogLevel logLevel)
			throws IOException {
		this.provider = provider;
		this.appName = appName;
		this.dir = dir == null ? new String[0] : dir.toArray(new String[dir
				.size()]);

		StringBuilder dirStr = new StringBuilder();
		if (dir == null || dir.isEmpty())
			dirStr.append("/");
		else
			for (String dirItem : dir)
				dirStr.append("/").append(dirItem);

		this.store = new BaiduPcsFileStore(accessToken, appName,
				dirStr.toString(), logLevel);
	}

	String getAppName() {
		return appName;
	}

	String[] getDir() {
		return dir;
	}

	@Override
	public BaiduPcsFileSystemProvider provider() {
		return provider;
	}

	/**
	 * 不能关闭文件系统。抛出UnsupportedOperationException。
	 * 
	 * @throws UnsupportedOperationException
	 * @see java.nio.file.FileSystem#close()
	 */
	@Override
	public void close() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return store.isReadOnly();
	}

	@Override
	public String getSeparator() {
		return SEPERATOR;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return Collections.singleton((Path) getRootDirectory());
	}

	private BaiduPcsPath rootDir;

	BaiduPcsPath getRootDirectory() {
		if (rootDir == null)
			rootDir = new BaiduPcsPath(this, "/");
		return rootDir;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		return Collections.singleton((FileStore) store);
	}

	BaiduPcsFileStore getFileStore() {
		return store;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return Collections.singleton(BaiduPcsBasicFileAttributeView.VIEW_NAME);
	}

	@Override
	public BaiduPcsPath getPath(String first, String... more) {
		StringBuilder pathStr = new StringBuilder(first);
		for (String moreItem : more)
			pathStr.append(moreItem);
		return new BaiduPcsPath(this, pathStr.toString());
	}

	private static final String GLOB_SYNTAX = "glob";
	private static final String REGEX_SYNTAX = "regex";

	@Override
	public PathMatcher getPathMatcher(String syntaxAndInput) {
		int pos = syntaxAndInput.indexOf(':');
		if (pos <= 0 || pos == syntaxAndInput.length())
			throw new IllegalArgumentException();
		String syntax = syntaxAndInput.substring(0, pos);
		String input = syntaxAndInput.substring(pos + 1);

		String expr;
		if (syntax.equals(GLOB_SYNTAX)) {
			expr = Globs.toUnixRegexPattern(input);
		} else {
			if (syntax.equals(REGEX_SYNTAX)) {
				expr = input;
			} else {
				throw new UnsupportedOperationException("Syntax '" + syntax
						+ "' not recognized");
			}
		}

		// return matcher
		final Pattern pattern = Pattern.compile(expr);

		return new PathMatcher() {
			@Override
			public boolean matches(Path path) {
				return pattern.matcher(path.toString()).matches();
			}
		};
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException(
				"Get UserPrincipalLookupService is not supported.");
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new UnsupportedOperationException("Watch is not supported.");
	}

}
