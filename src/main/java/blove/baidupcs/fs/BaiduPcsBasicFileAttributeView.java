package blove.baidupcs.fs;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.response.FileMetaWithExtra1;

public class BaiduPcsBasicFileAttributeView implements BasicFileAttributeView {
	public static final String VIEW_NAME = "basic";

	public static final String DYNAMIC_ATTR_LAST_MODIFIED_TIME = "lastModifiedTime";
	public static final String DYNAMIC_ATTR_LAST_ACCESS_TIME = "lastAccessTime";
	public static final String DYNAMIC_ATTR_CREATION_TIME = "creationTime";
	public static final String DYNAMIC_ATTR_SIZE = "size";
	public static final String DYNAMIC_ATTR_IS_REGULAR_FILE = "isRegularFile";
	public static final String DYNAMIC_ATTR_IS_DIRECTORY = "isDirectory";
	public static final String DYNAMIC_ATTR_IS_SYMBOLIC_LINK = "isSymbolicLink";
	public static final String DYNAMIC_ATTR_IS_OTHER = "isOther";
	public static final String DYNAMIC_ATTR_FILE_KEY = "fileKey";

	private final BaiduPcs service;
	private final BaiduPcsPath path;
	private final String pathServiceStr;

	BaiduPcsBasicFileAttributeView(BaiduPcsPath path) {
		this.path = path;
		this.pathServiceStr = path.toServiceString();
		this.service = path.getFileSystem().getFileStore().getService();
	}

	@Override
	public String name() {
		return "basic";
	}

	@Override
	public BasicFileAttributes readAttributes() throws IOException {
		try {
			FileMetaWithExtra1 meta = service.meta(pathServiceStr);
			return new BaiduPcsBasicFileAttributes(meta.getCtime() * 1000, meta.getMtime() * 1000, meta.isDir(),
					meta.getSize(), meta.getFsID());
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(path.toString());
		}
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
		// Not support, do nothing.
	}

	public Map<String, Object> readAttributes(String[] attrNameArray) throws IOException {
		if (attrNameArray.length == 0)
			return Collections.emptyMap();

		BasicFileAttributes attrs = readAttributes();
		Map<String, Object> res = new HashMap<>();
		for (String attrName : attrNameArray) {
			Object value;
			switch (attrName) {
			case DYNAMIC_ATTR_LAST_MODIFIED_TIME:
				value = attrs.lastModifiedTime();
				break;
			case DYNAMIC_ATTR_LAST_ACCESS_TIME:
				value = attrs.lastAccessTime();
				break;
			case DYNAMIC_ATTR_CREATION_TIME:
				value = attrs.creationTime();
				break;
			case DYNAMIC_ATTR_SIZE:
				value = attrs.size();
				break;
			case DYNAMIC_ATTR_IS_REGULAR_FILE:
				value = attrs.isRegularFile();
				break;
			case DYNAMIC_ATTR_IS_DIRECTORY:
				value = attrs.isDirectory();
				break;
			case DYNAMIC_ATTR_IS_SYMBOLIC_LINK:
				value = attrs.isSymbolicLink();
				break;
			case DYNAMIC_ATTR_IS_OTHER:
				value = attrs.isOther();
				break;
			case DYNAMIC_ATTR_FILE_KEY:
				value = attrs.fileKey();
				break;
			default:
				throw new IllegalArgumentException("Unrecognized attr name: " + attrName);
			}
			res.put(attrName, value);
		}
		return res;
	}

	public void setAttribute(String attribute, Object value) throws IOException {
		// Not support, do nothing.
	}

}
