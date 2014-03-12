package blove.baidupcs.fs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import retrofit.RestAdapter.LogLevel;
import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsException;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.request.OnDup;
import blove.baidupcs.api.response.FileMetaWithExtra1;
import blove.baidupcs.api.response.FileMetaWithExtra2;
import blove.baidupcs.api.response.Quota;
import blove.baidupcs.fs.util.Cache;
import blove.baidupcs.service.request.files.MoveBatchOrCopyBatchParam.FromTo;

public class BaiduPcsFileStore extends FileStore {
	private static final String FILESTORE_TYPE = "baidupcs";
	/**
	 * 属性和list缓存超时时间。
	 * 
	 * @see CachedService
	 */
	private static final int META_CACHE_EXPIRE_TIME = 5000;

	private final BaiduPcs service;
	private final String appName;

	/**
	 * @param accessToken
	 *             开发者标识
	 * @param appName
	 *             应用名称
	 * @param dir
	 *             文件系统根目录路径，此路径以应用目录为根目录。null或空为应用目录。
	 * @param logLevel
	 *             打印的日志级别
	 * @throws IOException
	 */
	BaiduPcsFileStore(String accessToken, String appName, String dir, LogLevel logLevel) throws IOException {
		this.service = new CachedService(accessToken, appName, logLevel);
		this.appName = appName;

		List<String> dirItems = clearDirItems(Arrays.asList(dir.split("/")));

		int unexistIndex = dirItems.size();

		for (int i = dirItems.size(); i > 0; i--) {
			String dirStr = genePathStr(dirItems.subList(0, i));
			try {
				FileMetaWithExtra1 meta = service.meta(dirStr);
				if (meta.isDir())
					break;
				else
					throw new IllegalArgumentException(dir + " is not a dir and cannot create.");
			} catch (BaiduPcsFileNotExistsException e) {
				unexistIndex = i;
			}
		}

		for (int i = unexistIndex; i < dirItems.size(); i++) {
			String dirStr = genePathStr(dirItems.subList(0, i));
			service.mkdir(dirStr);
		}
	}

	/**
	 * 清除dirItems中的空元素。
	 * 
	 * @param dirItems
	 * @return
	 */
	private List<String> clearDirItems(List<String> dirItems) {
		List<String> clearItems = new ArrayList<>();
		for (String item : dirItems)
			if (!item.isEmpty())
				clearItems.add(item);
		return clearItems;
	}

	/**
	 * 根据dirItems获取路径字符串。
	 * 
	 * @param dirItems
	 * @return
	 */
	private String genePathStr(List<String> dirItems) {
		StringBuilder str = new StringBuilder();
		for (String item : dirItems)
			if (!item.isEmpty())
				str.append("/").append(item);
		if (str.length() == 0)
			str.append("/");
		return str.toString();
	}

	/**
	 * 带有缓存的百度云存储服务。当获取元信息时，元信息将被缓存下来，并保留5秒。因实际应用中获取属性的操作往往很频繁，获取元信息时先到此缓存中查找，
	 * 若没有再fetch。
	 * 
	 * @author blove
	 */
	private static class CachedService extends BaiduPcs {
		private final Cache<String, FileMetaWithExtra1> metaCache;

		CachedService(String accessToken, String appName, LogLevel logLevel) {
			super(accessToken, appName, logLevel);
			metaCache = new Cache<>(META_CACHE_EXPIRE_TIME);
		}

		@Override
		public FileMetaWithExtra2 upload(String path, byte[] bytes, OnDup ondup) throws BaiduPcsException,
				IOException {
			FileMetaWithExtra2 ret = super.upload(path, bytes, ondup);
			metaCache.remove(path);
			return ret;
		}

		@Override
		public FileMetaWithExtra2 upload(String path, InputStream in, long size, OnDup ondup)
				throws BaiduPcsException, IOException {
			FileMetaWithExtra2 ret = super.upload(path, in, size, ondup);
			metaCache.remove(path);
			return ret;
		}

		@Override
		public FileMetaWithExtra2 createSuperFile(String path, List<String> blockList, OnDup ondup)
				throws BaiduPcsException, IOException {
			FileMetaWithExtra2 ret = super.createSuperFile(path, blockList, ondup);
			metaCache.remove(path);
			return ret;
		}

		@Override
		public FileMetaWithExtra1 meta(String path) throws BaiduPcsException, IOException {
			FileMetaWithExtra1 meta = metaCache.get(path);
			if (meta == null) {
				meta = super.meta(path);
				metaCache.put(path, meta);
			}
			return meta;
		}

		@Override
		public List<FileMetaWithExtra1> meta(List<String> paths) throws BaiduPcsException, IOException {
			FileMetaWithExtra1[] metas = new FileMetaWithExtra1[paths.size()];
			List<String> fetchs = new LinkedList<>();
			for (int i = 0; i < metas.length; i++) {
				String path = paths.get(i);
				FileMetaWithExtra1 meta = metaCache.get(path);
				if (meta == null)
					fetchs.add(path);
				else
					metas[i] = meta;
			}

			List<FileMetaWithExtra1> fetchedMetas = super.meta(fetchs);
			Iterator<FileMetaWithExtra1> fetchedMetaItr = fetchedMetas.iterator();
			for (int i = 0; i < metas.length; i++) {
				if (metas[i] == null)
					metas[i] = fetchedMetaItr.next();
			}

			return Arrays.asList(metas);
		}

		@Override
		public void move(String from, String to) throws BaiduPcsException, IOException {
			super.move(from, to);
			metaCache.remove(from);
		}

		@Override
		public void move(List<FromTo> fromTos) throws BaiduPcsException, IOException {
			super.move(fromTos);
			for (FromTo fromTo : fromTos)
				metaCache.remove(fromTo.getFrom());
		}

		@Override
		public void delete(String path) throws BaiduPcsException, IOException {
			super.delete(path);
			metaCache.remove(path);
		}

		@Override
		public void delete(List<String> paths) throws BaiduPcsException, IOException {
			super.delete(paths);
			for (String path : paths)
				metaCache.remove(path);
		}

	}

	BaiduPcs getService() {
		return service;
	}

	@Override
	public String name() {
		return appName;
	}

	@Override
	public String type() {
		return FILESTORE_TYPE;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public long getTotalSpace() throws IOException {
		return getService().quota().getQuota();
	}

	@Override
	public long getUsableSpace() throws IOException {
		return getUnallocatedSpace();
	}

	@Override
	public long getUnallocatedSpace() throws IOException {
		Quota quota = getService().quota();
		return quota.getQuota() - quota.getUsed();
	}

	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		return type.isAssignableFrom(BaiduPcsBasicFileAttributeView.class);
	}

	@Override
	public boolean supportsFileAttributeView(String name) {
		return BaiduPcsBasicFileAttributeView.VIEW_NAME.equals(name);
	}

	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
		return null;
	}

	@Override
	public Object getAttribute(String attribute) throws IOException {
		throw new UnsupportedOperationException("No FileStoreAttributeViews is supported.");
	}

}
