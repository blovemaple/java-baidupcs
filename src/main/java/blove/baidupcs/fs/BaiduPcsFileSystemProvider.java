package blove.baidupcs.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import retrofit.RestAdapter.LogLevel;
import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsFileExistsException;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.error.BaiduPcsIllegalFileNameException;
import blove.baidupcs.api.error.BaiduPcsParentDirNotExistsException;
import blove.baidupcs.api.error.NoAuthException;
import blove.baidupcs.api.response.FileMetaWithExtra1;
import blove.baidupcs.api.response.FileMetaWithExtra2;

/**
 * 以在百度个人云存储中的某个目录为根目录的文件系统提供者。<br>
 * URI格式：<br>
 * 文件系统 - baidupcs://ACCESS_TOKEN:APP_NAME/DIR<br>
 * 文件 - baidupcs://ACCESS_TOKEN:APP_NAME/DIR#FILE_PATH<br>
 * ACCESS_TOKEN：百度开发者标识<br>
 * APP_NAME：应用名称<br>
 * DIR：文件系统所在路径，此路径以应用目录为根目录，以斜线为分隔符（可选，默认为应用目录）<br>
 * FILE_PATH：文件路径，以斜线为分隔符<br>
 * env中用"loglevel"作为key，以"none"/"basic"/"headers"/"full"指定打印的日志级别。不指定时默认"none"。<br>
 * 示例：baidupcs://abcdefghijklmn:myapp/fs/dir#/somedir/file
 * 
 * @author blove
 */
public class BaiduPcsFileSystemProvider extends FileSystemProvider {
	private static final String SCHEME = "baidupcs";

	public static final String ENV_KEY_LOGLEVEL = "loglevel";
	public static final String ENV_VALUE_LOGLEVEL_NONE = "none";
	public static final String ENV_VALUE_LOGLEVEL_BASIC = "basic";
	public static final String ENV_VALUE_LOGLEVEL_HEADERS = "headers";
	public static final String ENV_VALUE_LOGLEVEL_FULL = "full";

	@Override
	public String getScheme() {
		return SCHEME;
	}

	private final Map<Integer, BaiduPcsFileSystem> fss = new HashMap<>();

	private static class FSInfo {
		String accessToken;
		String appName;
		List<String> dir;

		FSInfo(String accessToken, String appName, List<String> dir) {
			this.accessToken = accessToken;
			this.appName = appName;
			this.dir = dir;
		}

		static FSInfo fromURI(URI uri) {
			if (!SCHEME.equals(uri.getScheme()))
				throw new IllegalArgumentException("URI scheme should be 'baidu' but not '" + uri.getScheme() + "'");

			String[] authority = uri.getAuthority().split(":");
			if (authority.length < 2)
				throw new IllegalArgumentException("Invalid URI: " + uri);

			String accessToken = authority[0];
			String appName = authority[1];

			String[] path = uri.getPath().split("/");

			List<String> dir = new LinkedList<>();
			for (String dirItem : path) {
				if (!dirItem.isEmpty())
					dir.add(dirItem);
			}

			return new FSInfo(accessToken, appName, dir);
		}

	}

	@Override
	public BaiduPcsFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		FSInfo fsInfo = FSInfo.fromURI(uri);

		int fsHash = fileSystemHash(fsInfo);
		if (fss.containsKey(fsHash))
			throw new FileSystemAlreadyExistsException(uri.toString());

		LogLevel logLevel = LogLevel.NONE;
		if (env != null) {
			String logLevelStr = (String) env.get(ENV_KEY_LOGLEVEL);

			if (logLevelStr != null) {
				switch (logLevelStr) {
				case ENV_VALUE_LOGLEVEL_BASIC:
					logLevel = LogLevel.BASIC;
					break;
				case ENV_VALUE_LOGLEVEL_HEADERS:
					logLevel = LogLevel.HEADERS;
					break;
				case ENV_VALUE_LOGLEVEL_FULL:
					logLevel = LogLevel.FULL;
					break;
				case ENV_VALUE_LOGLEVEL_NONE:
					break;
				}
			}
		}

		return new BaiduPcsFileSystem(this, fsInfo.accessToken, fsInfo.appName, fsInfo.dir, logLevel);
	}

	@Override
	public BaiduPcsFileSystem getFileSystem(URI uri) {
		int fsHash = fileSystemHash(FSInfo.fromURI(uri));
		BaiduPcsFileSystem fs = fss.get(fsHash);
		if (fs == null)
			throw new FileSystemNotFoundException(uri.toString());
		return fs;
	}

	/**
	 * 计算文件系统信息的哈希值。
	 * 
	 * @param fsInfo
	 * @return
	 */
	private int fileSystemHash(FSInfo fsInfo) {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fsInfo.accessToken == null) ? 0 : fsInfo.accessToken.hashCode());
		result = prime * result + ((fsInfo.appName == null) ? 0 : fsInfo.appName.hashCode());
		result = prime * result + ((fsInfo.dir == null) ? 0 : fsInfo.dir.hashCode());
		return result;
	}

	@Override
	public BaiduPcsPath getPath(URI uri) {
		BaiduPcsFileSystem fs;
		try {
			fs = getFileSystem(uri);
		} catch (FileSystemNotFoundException e) {
			try {
				fs = newFileSystem(uri, null);
			} catch (IOException e1) {
				throw new FileSystemNotFoundException(e1.toString());
			}
		}
		String pathStr = uri.getFragment();

		return new BaiduPcsPath(fs, pathStr);
	}

	public URI getURI(BaiduPcsPath baiduPcsPath) {
		// 因URI中含有保密信息access token，所以不能提供
		throw new UnsupportedOperationException("Get URI from path is not supported.");
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		return new BaiduPcsFileChannel(checkPathType(path), options);
	}

	@Override
	public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		try {
			BaiduPcsPath pcsPath = checkPathType(path);

			for (OpenOption option : options)
				if (option != StandardOpenOption.READ)
					throw new UnsupportedOperationException(option + " not allowed.");

			return serviceOf(pcsPath).download(pcsPath.toServiceString()).in();
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		} catch (BaiduPcsIllegalFileNameException e) {
			throw new IllegalArgumentException(e);
		} catch (NoAuthException e) {
			throw new AccessDeniedException(path.toString(), null, "Delete failed");
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		checkPathType(dir);
		return new BaiduPcsDirectoryStream((BaiduPcsPath) dir, filter);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		BaiduPcsPath dirPath = checkPathType(dir);

		try {
			serviceOf(dirPath).mkdir(dirPath.toServiceString());
		} catch (BaiduPcsParentDirNotExistsException e) {
			throw new NoSuchFileException(dirPath.toAbsolutePath().getParent().toString());
		} catch (BaiduPcsFileExistsException e) {
			throw new FileAlreadyExistsException(dirPath.toString());
		} catch (BaiduPcsIllegalFileNameException e) {
			throw new IllegalArgumentException(e);
		} catch (NoAuthException e) {
			throw new AccessDeniedException(dir.toString(), null, "Creation failed");
		}
	}

	@Override
	public void delete(Path path) throws IOException {
		BaiduPcsPath pcsPath = checkPathType(path);
		String pathServiceStr = pcsPath.toServiceString();

		BaiduPcs service = serviceOf(pcsPath);

		List<FileMetaWithExtra2> infoList;
		try {
			infoList = service.list(pathServiceStr);
		} catch (BaiduPcsFileNotExistsException e1) {
			throw new NoSuchFileException(path.toString());
		}
		if (infoList != null && !infoList.isEmpty()) {
			throw new DirectoryNotEmptyException(pcsPath.toString());
		}

		try {
			service.delete(pathServiceStr);
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		} catch (BaiduPcsParentDirNotExistsException e) {
			throw new NoSuchFileException(pcsPath.toAbsolutePath().getParent().toString());
		} catch (BaiduPcsIllegalFileNameException e) {
			throw new IllegalArgumentException(e);
		} catch (NoAuthException e) {
			throw new AccessDeniedException(path.toString(), null, "Delete failed");
		}

	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		BaiduPcsPath sourcePath = checkPathType(source);
		String sourcePathServiceStr = sourcePath.toServiceString();
		BaiduPcsPath targetPath = checkPathType(target);
		String targetPathServiceStr = targetPath.toServiceString();

		BaiduPcs sourceService = serviceOf(sourcePath);
		BaiduPcs targetService = serviceOf(targetPath);

		boolean replaceExisting = false;// 是否允许覆盖target
		for (CopyOption option : options) {
			if (option == StandardCopyOption.COPY_ATTRIBUTES)
				throw new UnsupportedOperationException("COPY_ATTRIBUTES not supported.");
			else if (option == StandardCopyOption.REPLACE_EXISTING)
				replaceExisting = true;
		}

		boolean sourceIsDir;// source是否是目录
		try {
			FileMetaWithExtra1 targetMeta = sourceService.meta(sourcePathServiceStr);
			sourceIsDir = targetMeta.isDir();
		} catch (BaiduPcsFileNotExistsException e1) {
			throw new NoSuchFileException(sourcePath.toString());
		}
		if (sourceIsDir && !sourceService.list(sourcePathServiceStr).isEmpty())
			throw new DirectoryNotEmptyException(sourcePath.toString());

		// 现在source肯定存在，且不是非空目录

		boolean targetExists, targetIsDir;// target是否存在、是否是目录
		try {
			FileMetaWithExtra1 targetMeta = targetService.meta(targetPathServiceStr);
			targetExists = true;
			targetIsDir = targetMeta.isDir();
		} catch (BaiduPcsFileNotExistsException e1) {
			targetExists = false;
			targetIsDir = false;
		}

		if (!replaceExisting && targetExists)
			throw new FileAlreadyExistsException(targetPath.toString());
		if (replaceExisting && targetIsDir && !targetService.list(targetPathServiceStr).isEmpty())
			throw new DirectoryNotEmptyException(targetPath.toString());

		// 现在，如果覆盖，目标肯定不是非空目录（可删）；如果不覆盖，目标肯定不存在

		boolean temped = false;// 是否有target临时文件
		String tempTarget = null;// target临时文件
		try {
			if (replaceExisting && targetExists) {
				// 要求覆盖已存在的target
				// 先不删除target，而是移动到临时文件，失败后可恢复
				while (temped == false) {
					try {
						tempTarget = targetPathServiceStr + Math.abs(random.nextInt());
						targetService.move(targetPathServiceStr, tempTarget);
						temped = true;
					} catch (BaiduPcsFileExistsException e) {
					}
				}
			}
		} catch (BaiduPcsFileNotExistsException e1) {
		}

		try {
			// 尝试将source拷贝到target
			boolean copiedPartly = false;// 记录是否拷贝了一部分，失败恢复时应删除
			try {
				if (sourceService.equals(targetService)) {
					// 在一个存储空间，直接调用API移动（可能移动非空目录）
					sourceService.move(sourcePathServiceStr, targetPathServiceStr);
				} else {
					// 不在一个存储空间
					if (sourceIsDir) {
						// source是目录，直接创建target目录
						targetService.mkdir(targetPathServiceStr);
					} else {
						// source是文件，从source读入，写出到target
						copiedPartly = true;
						try (OutputStream targetOut = newOutputStream(targetPath);
								InputStream sourceIn = newInputStream(sourcePath)) {
							byte[] buf = new byte[1024 * 8];
							int readOnce;
							while ((readOnce = sourceIn.read(buf)) >= 0) {
								targetOut.write(buf, 0, readOnce);
							}
						}
					}
				}
			} catch (IOException | RuntimeException e) {
				// 拷贝失败，恢复target
				if (copiedPartly)
					try {
						targetService.delete(targetPathServiceStr);
					} catch (BaiduPcsFileNotExistsException e1) {
					}
				if (temped)
					targetService.move(tempTarget, targetPathServiceStr);
				throw e;
			}
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(source.toString());
		} catch (BaiduPcsParentDirNotExistsException e) {
			throw new NoSuchFileException(target.toAbsolutePath().getParent().toString());
		} catch (BaiduPcsIllegalFileNameException e) {
			throw new IllegalArgumentException(e);
		} catch (NoAuthException e) {
			throw new AccessDeniedException(source.toString() + " or " + target.toString(), null, "Delete failed");
		}
	}

	private final Random random = new Random();

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		BaiduPcsPath sourcePath = checkPathType(source);
		BaiduPcsPath targetPath = checkPathType(target);
		String sourcePathServiceStr = sourcePath.toServiceString();
		String targetPathServiceStr = targetPath.toServiceString();

		if (getFileStore(sourcePath).equals(targetPath) && sourcePath.equals(targetPath))
			return;

		BaiduPcs sourceService = serviceOf(sourcePath);
		BaiduPcs targetService = serviceOf(targetPath);

		boolean sourceIsDir;// source是否是目录
		try {
			FileMetaWithExtra1 targetMeta = sourceService.meta(sourcePathServiceStr);
			sourceIsDir = targetMeta.isDir();
		} catch (BaiduPcsFileNotExistsException e1) {
			throw new NoSuchFileException(sourcePath.toString());
		}

		// 现在source肯定存在

		boolean replaceExisting = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);// 是否允许覆盖target
		boolean targetExists, targetIsDir;// target是否存在、是否是目录
		try {
			FileMetaWithExtra1 targetMeta = targetService.meta(targetPathServiceStr);
			targetExists = true;
			targetIsDir = targetMeta.isDir();
		} catch (BaiduPcsFileNotExistsException e1) {
			targetExists = false;
			targetIsDir = false;
		}

		if (!replaceExisting && targetExists)
			throw new FileAlreadyExistsException(targetPath.toString());
		if (replaceExisting && targetIsDir && !targetService.list(targetPathServiceStr).isEmpty())
			throw new DirectoryNotEmptyException(targetPath.toString());

		// 现在，如果覆盖，目标肯定不是非空目录（可删）；如果不覆盖，目标肯定不存在

		if (!sourceService.equals(targetService)) {
			// 不在一个存储空间，source不能是非空目录
			if (targetIsDir && !sourceService.list(sourcePathServiceStr).isEmpty())
				throw new DirectoryNotEmptyException(sourcePath.toString());
		}

		// 现在，如果不在一个存储空间，source肯定不是非空目录

		boolean temped = false;// 是否有target临时文件
		String tempTarget = null;// target临时文件
		try {
			if (replaceExisting && targetExists) {
				// 要求覆盖已存在的target
				// 先不删除target，而是移动到临时文件，失败后可恢复
				while (temped == false) {
					try {
						tempTarget = targetPathServiceStr + Math.abs(random.nextInt());
						targetService.move(targetPathServiceStr, tempTarget);
						temped = true;
					} catch (BaiduPcsFileExistsException e) {
					}
				}
			}
		} catch (BaiduPcsFileNotExistsException e1) {
		}

		// 尝试将source拷贝到target
		try {
			boolean copiedPartly = false;// 记录是否拷贝了一部分，失败恢复时应删除
			try {
				if (sourceService.equals(targetService)) {
					// 在一个存储空间，直接调用API移动（可能移动非空目录）
					sourceService.move(sourcePathServiceStr, targetPathServiceStr);
				} else {
					// 不在一个存储空间
					if (sourceIsDir) {
						// source是目录，直接创建target目录
						targetService.mkdir(targetPathServiceStr);
					} else {
						// source是文件，从source读入，写出到target
						copiedPartly = true;
						try (OutputStream targetOut = newOutputStream(targetPath);
								InputStream sourceIn = newInputStream(sourcePath)) {
							byte[] buf = new byte[1024 * 8];
							int readOnce;
							while ((readOnce = sourceIn.read(buf)) >= 0) {
								targetOut.write(buf, 0, readOnce);
							}
						}
					}
				}
			} catch (IOException | RuntimeException e) {
				// 拷贝失败，恢复target
				if (copiedPartly)
					try {
						targetService.delete(targetPathServiceStr);
					} catch (BaiduPcsFileNotExistsException e1) {
					}
				if (temped)
					targetService.move(tempTarget, targetPathServiceStr);
				throw e;
			}

			// 拷贝成功，删除tempTarget
			if (temped)
				targetService.delete(tempTarget);

		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(source.toString());
		} catch (BaiduPcsParentDirNotExistsException e) {
			throw new NoSuchFileException(target.toAbsolutePath().getParent().toString());
		} catch (BaiduPcsIllegalFileNameException e) {
			throw new IllegalArgumentException(e);
		} catch (NoAuthException e) {
			throw new AccessDeniedException(source.toString() + " or " + target.toString(), null, "Delete failed");
		}
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		BaiduPcsPath pcsPath, pcsPath2;
		try {
			pcsPath = checkPathType(path);
			pcsPath2 = checkPathType(path);
		} catch (ProviderMismatchException e) {
			return false;
		}

		if (!serviceOf(pcsPath).equals(serviceOf(pcsPath2)))
			return false;

		if (!pcsPath.toRealPath().equals(pcsPath2.toRealPath()))
			return false;

		return true;
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		Path fileName = path.normalize().getFileName();
		return fileName != null && fileName.startsWith(".");
	}

	@Override
	public BaiduPcsFileStore getFileStore(Path path) throws IOException {
		BaiduPcsPath pcsPath = checkPathType(path);
		return pcsPath.getFileSystem().getFileStore();
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		BaiduPcsPath pcsPath = checkPathType(path);
		try {
			serviceOf(pcsPath).meta(pcsPath.toServiceString());
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		} catch (IOException e) {
			throw new AccessDeniedException(path.toString());
		}
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		if (type.isAssignableFrom(BaiduPcsBasicFileAttributeView.class))
			return type.cast(new BaiduPcsBasicFileAttributeView(checkPathType(path)));

		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		if (type.isAssignableFrom(BaiduPcsBasicFileAttributes.class))
			return type.cast(new BaiduPcsBasicFileAttributeView(checkPathType(path)).readAttributes());

		throw new UnsupportedOperationException("Attributes of type " + type + "is not supported.");
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		BaiduPcsPath pcsPath = checkPathType(path);

		String attrNames;

		String[] viewAndAttrs = attributes.split(":", 2);
		if (viewAndAttrs.length == 1) {
			attrNames = attributes;
		} else {
			String viewName = viewAndAttrs[0];
			if (!BaiduPcsBasicFileAttributeView.VIEW_NAME.equals(viewName))
				throw new UnsupportedOperationException("Unsupported attribute view: " + viewName);
			attrNames = viewAndAttrs[1];
		}

		String[] attrNameArray = attrNames.split(",");
		return new BaiduPcsBasicFileAttributeView(pcsPath).readAttributes(attrNameArray);
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		BaiduPcsPath pcsPath = checkPathType(path);

		String attrName;

		String[] viewAndAttr = attribute.split(":", 2);
		if (viewAndAttr.length == 1) {
			attrName = attribute;
		} else {
			String viewName = viewAndAttr[0];
			if (!BaiduPcsBasicFileAttributeView.VIEW_NAME.equals(viewName))
				throw new UnsupportedOperationException("Unsupported attribute view: " + viewName);
			attrName = viewAndAttr[1];
		}
		new BaiduPcsBasicFileAttributeView(pcsPath).setAttribute(attrName, value);
	}

	private BaiduPcsPath checkPathType(Path path) throws ProviderMismatchException {
		if (path != null && !(path instanceof BaiduPcsPath))
			throw new ProviderMismatchException("Param is not an instance of BaiduPcsPath, but of "
					+ path.getClass().getName());
		return (BaiduPcsPath) path;
	}

	private BaiduPcs serviceOf(BaiduPcsPath path) throws IOException {
		return getFileStore(path).getService();
	}

}
