package blove.baidupcs.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blove.baidupcs.service.response.files.MetaBatchResponse;

import com.google.gson.Gson;

/**
 * 查询获得的文件或目录的元信息。
 * 
 * @author blove
 */
public class FileMetaWithExtra1 extends FileMeta {
	public static FileMetaWithExtra1 fromSingleResponse(MetaBatchResponse res) {
		List<MetaBatchResponse.Meta> resList = res.getList();
		if (resList == null || resList.isEmpty())
			return null;
		if (resList.size() > 1)
			throw new IllegalArgumentException();
		return fromResponseMeta(resList.get(0));
	}

	public static List<FileMetaWithExtra1> fromBatchResponse(
			MetaBatchResponse res) {
		List<MetaBatchResponse.Meta> resList = res.getList();
		if (resList == null || resList.isEmpty())
			return Collections.emptyList();

		List<FileMetaWithExtra1> metas = new ArrayList<>(resList.size());
		for (MetaBatchResponse.Meta resMeta : resList)
			metas.add(fromResponseMeta(resMeta));

		return metas;
	}

	private static FileMetaWithExtra1 fromResponseMeta(
			MetaBatchResponse.Meta resMeta) {
		FileMetaWithExtra1 meta = new FileMetaWithExtra1();
		meta.fsID = resMeta.getFs_id();
		meta.path = resMeta.getPath();
		meta.ctime = resMeta.getCtime();
		meta.mtime = resMeta.getMtime();
		meta.blockList = resMeta.getBlock_list();
		meta.size = resMeta.getSize();
		meta.isDir = resMeta.getIsdir();
		meta.hasSubdir = resMeta.getIfhassubdir();
		return meta;
	}

	private String blockList;
	private int hasSubdir;

	/**
	 * 文件所有分片的md5列表。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getBlockList() {
		if (size == 0 || blockList == null || blockList.length() == 0)
			return Collections.emptyList();
		else {
			return new Gson().fromJson(blockList, List.class);
		}
	}

	/**
	 * 是否含有子目录。
	 * 
	 * @return
	 */
	public boolean hasSubdir() {
		return hasSubdir == 1;
	}

	@Override
	public String toString() {
		return "Meta [\n\tfs_id=" + fsID + "\n\tpath=" + path + "\n\tctime="
				+ ctime + "\n\tmtime=" + mtime + "\n\tblock_list="
				+ getBlockList() + "\n\tsize=" + size + "\n\tisdir=" + isDir
				+ "\n\tifhassubdir=" + hasSubdir + "\n]";
	}

}
