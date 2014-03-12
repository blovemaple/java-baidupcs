package blove.baidupcs.api;

import static blove.baidupcs.service.BaiduPcsCService.*;
import static blove.baidupcs.service.BaiduPcsDService.*;
import static blove.baidupcs.service.BaiduPcsService.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import blove.baidupcs.api.error.BaiduPcsException;
import blove.baidupcs.api.error.ErrorParseHandler;
import blove.baidupcs.api.request.CloudDownloadQueryCondition;
import blove.baidupcs.api.request.ListStreamType;
import blove.baidupcs.api.request.OnDup;
import blove.baidupcs.api.request.Order;
import blove.baidupcs.api.request.OrderBy;
import blove.baidupcs.api.request.RapidUploadRecogInfo;
import blove.baidupcs.api.request.StreamingType;
import blove.baidupcs.api.response.CloudDownloadMeta;
import blove.baidupcs.api.response.CloudDownloadProgress;
import blove.baidupcs.api.response.FileMetaWithExtra1;
import blove.baidupcs.api.response.FileMetaWithExtra2;
import blove.baidupcs.api.response.Quota;
import blove.baidupcs.service.BaiduPcsCService;
import blove.baidupcs.service.BaiduPcsDService;
import blove.baidupcs.service.BaiduPcsService;
import blove.baidupcs.service.request.files.CreateSuperFileParam;
import blove.baidupcs.service.request.files.MoveBatchOrCopyBatchParam;
import blove.baidupcs.service.request.files.PathListParam;
import blove.baidupcs.service.request.files.RestoreBatchParam;
import blove.baidupcs.service.request.files.MoveBatchOrCopyBatchParam.FromTo;
import blove.baidupcs.service.request.files.PathListParam.Path;
import blove.baidupcs.service.request.files.RestoreBatchParam.RestoreFileInfo;
import blove.baidupcs.service.response.files.CloudDownloadAddTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadListTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse;
import blove.baidupcs.service.response.files.CreateFileResponse;
import blove.baidupcs.service.response.files.DiffResponse;
import blove.baidupcs.service.response.files.ListOrSearchResponse;
import blove.baidupcs.service.response.files.CloudDownloadListTaskResponse.ListTaskInfo;

/**
 * 封装的百度个人云存储空间，提供比{@link BaiduPcsService}、${@link BaiduPcsCService}、$
 * {@link BaiduPcsDService}更方便的接口。
 * 
 * @author blove
 */
public class BaiduPcs {
	private final String accessToken;
	private final String pathPrefix;

	private BaiduPcsService pcsService;
	private BaiduPcsCService pcsCService;
	private BaiduPcsDService pcsDService;

	private final LogLevel logLevel;

	/**
	 * 新建一个实例，不输出日志。
	 * 
	 * @param accessToken
	 *            百度的开发者准入标识。
	 * @param appName
	 *            应用名称。用于根目录的路径中。
	 */
	public BaiduPcs(String accessToken, String appName) {
		this(accessToken, appName, LogLevel.NONE);
	}

	/**
	 * 新建一个实例。
	 * 
	 * @param accessToken
	 *            百度的开发者准入标识。
	 * @param appName
	 *            应用名称。用于根目录的路径中。
	 * @param logLevel
	 *            日志等级。
	 */
	public BaiduPcs(String accessToken, String appName, LogLevel logLevel) {
		this.accessToken = accessToken;
		this.pathPrefix = "/apps/" + appName;

		ErrorHandler errorHandler = new ErrorParseHandler();
		pcsService = new RestAdapter.Builder().setLogLevel(logLevel)
				.setEndpoint(BaiduPcsService.SERVER)
				.setErrorHandler(errorHandler).build()
				.create(BaiduPcsService.class);
		pcsCService = new RestAdapter.Builder().setLogLevel(logLevel)
				.setEndpoint(BaiduPcsService.SERVER)
				.setErrorHandler(errorHandler).build()
				.create(BaiduPcsCService.class);
		pcsDService = new RestAdapter.Builder().setLogLevel(logLevel)
				.setEndpoint(BaiduPcsDService.SERVER)
				.setErrorHandler(errorHandler).build()
				.create(BaiduPcsDService.class);

		this.logLevel = logLevel;
	}

	/**
	 * 获取当前用户空间配额信息。
	 * 
	 * @return Quota
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Quota quota() throws BaiduPcsException, IOException {
		try {
			return Quota.fromResponse(pcsService.quotaInfo(METHOD_INFO,
					accessToken));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 上传单个文件。<br>
	 * 百度PCS服务目前支持最大2G的单个文件上传。<br>
	 * 如需支持超大文件（>2G）的断点续传，请参考下面的“分片文件上传”方法。
	 * 
	 * @param path
	 *            上传后的文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param bytes
	 *            文件内容
	 * @param ondup
	 *            文件已存在的处理方式。默认为抛出异常。
	 * @return Creation
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra2 upload(String path, byte[] bytes, OnDup ondup)
			throws BaiduPcsException, IOException {
		return upload(path, new ByteArrayInputStream(bytes), bytes.length,
				ondup);
	}

	/**
	 * 上传单个文件。<br>
	 * 百度PCS服务目前支持最大2G的单个文件上传。<br>
	 * 如需支持超大文件（>2G）的断点续传，请参考下面的“分片文件上传”方法。
	 * 
	 * @param path
	 *            上传后的文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param in
	 *            文件内容输入流
	 * @param size
	 *            文件内容的长度
	 * @param ondup
	 *            文件已存在的处理方式。默认为抛出异常。
	 * @return Creation
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra2 upload(String path, final InputStream in,
			final long size, OnDup ondup) throws BaiduPcsException, IOException {
		try {
			if (ondup == null)
				ondup = OnDup.EXCEPTION;

			TypedOutput out = new TypedOutput() {

				@Override
				public void writeTo(OutputStream out) throws IOException {
					byte[] buf = new byte[1024 * 8];
					int n;
					while ((n = in.read(buf)) >= 0) {
						out.write(buf, 0, n);
					}
				}

				@Override
				public String mimeType() {
					return "application/octet-stream";
				}

				@Override
				public long length() {
					return size;
				}

				@Override
				public String fileName() {
					return "file";
				}
			};
			return FileMetaWithExtra2.fromResponse(pcsCService.upload(
					METHOD_UPLOAD, accessToken, realPath(path), out,
					ondup.getRestParam()));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 上传文件分块。<br>
	 * 百度PCS服务支持每次直接上传最大2G的单个文件。<br>
	 * 如需支持上传超大文件（>2G），则可以通过组合调用分片文件上传的uploadBlock方法和createSuperFile方法实现：<br>
	 * 首先，将超大文件分割为2G以内的单文件，并调用upload将分片文件依次上传；<br>
	 * 其次，调用createSuperFile，完成分片文件的重组。<br>
	 * 除此之外，如果应用中需要支持断点续传的功能，也可以通过分片上传文件并调用createSuperFile接口的方式实现。
	 * 
	 * @param bytes
	 *            块内容
	 * @return 文件块的MD5
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public String uploadBlock(byte[] bytes) throws BaiduPcsException,
			IOException {
		return uploadBlock(new ByteArrayInputStream(bytes), bytes.length);
	}

	/**
	 * 上传文件分块。<br>
	 * 百度PCS服务支持每次直接上传最大2G的单个文件。<br>
	 * 如需支持上传超大文件（>2G），则可以通过组合调用分片文件上传的uploadBlock方法和createSuperFile方法实现：<br>
	 * 首先，将超大文件分割为2G以内的单文件，并调用upload将分片文件依次上传；<br>
	 * 其次，调用createSuperFile，完成分片文件的重组。<br>
	 * 除此之外，如果应用中需要支持断点续传的功能，也可以通过分片上传文件并调用createSuperFile接口的方式实现。
	 * 
	 * @param in
	 *            块内容输入流
	 * @param size
	 *            块内容的长度
	 * @return 文件块的MD5
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public String uploadBlock(final InputStream in, final long size)
			throws BaiduPcsException, IOException {
		try {
			TypedOutput out = new TypedOutput() {

				@Override
				public void writeTo(OutputStream out) throws IOException {
					byte[] buf = new byte[1024 * 8];
					int n;
					while ((n = in.read(buf)) >= 0) {
						out.write(buf, 0, n);
					}
				}

				@Override
				public String mimeType() {
					return "application/octet-stream";
				}

				@Override
				public long length() {
					return size;
				}

				@Override
				public String fileName() {
					return "file";
				}
			};
			return pcsCService.uploadBlock(METHOD_UPLOAD, accessToken,
					UPLOADBLOCK_TYPE_TMPFILE, out).getMd5();
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 与分片文件上传的uploadBlock方法配合使用，可实现超大文件（>2G）上传，同时也可用于断点续传的场景。
	 * 
	 * @param path
	 *            上传后的文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param blockList
	 *            所有分块的MD5列表
	 * @param ondup
	 *            文件已存在的处理方式。如果为null，则默认为抛出异常。
	 * @return Creation
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra2 createSuperFile(String path,
			List<String> blockList, OnDup ondup) throws BaiduPcsException,
			IOException {
		try {
			CreateSuperFileParam param = new CreateSuperFileParam(blockList);
			if (ondup == null)
				ondup = OnDup.EXCEPTION;
			return FileMetaWithExtra2.fromResponse(pcsService.createSuperFile(
					METHOD_CREATESUPERFILE, accessToken, realPath(path), param,
					ondup.getRestParam()));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 下载单个文件。
	 * 
	 * @param path
	 *            下载文件路径。此路径是以应用文件夹为根目录的路径。
	 * @return TypedInput
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public TypedInput download(String path) throws BaiduPcsException,
			IOException {
		return download(path, -1, -1);
	}

	/**
	 * 下载单个文件的指定部分。
	 * 
	 * @param path
	 *            下载文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param firstBytePos
	 *            下载部分第一个字节的位置，索引从0开始。如为负数则下载整个文件（此时lastBytePos无效）。
	 * @param lastBytePos
	 *            下载部分最后一个字节的位置，索引从0开始。如为负数或超过文件末尾，则默认为文件末尾位置。
	 * @return TypedInput
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public TypedInput download(String path, long firstBytePos, long lastBytePos)
			throws BaiduPcsException, IOException {
		try {
			String range = null;
			if (firstBytePos >= 0) {
				if (lastBytePos >= 0 && lastBytePos < firstBytePos)
					throw new IllegalArgumentException(
							"lastBytePos cannot be smaller than firstBytePos ("
									+ lastBytePos + ">" + firstBytePos + ")");
				range = "bytes=" + firstBytePos + "-"
						+ (lastBytePos >= 0 ? lastBytePos : "");
				if (logLevel == LogLevel.BASIC)
					System.out.println("Range:" + range);
			}
			return pcsDService.download(METHOD_DOWNLOAD, accessToken,
					realPath(path), range).getBody();
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建一个目录。
	 * 
	 * @param path
	 *            需要创建的目录路径。此路径是以应用文件夹为根目录的路径。
	 * @return Creation
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra2 mkdir(String path) throws BaiduPcsException,
			IOException {
		try {
			return FileMetaWithExtra2.fromResponse(pcsService.mkdir(
					METHOD_MKDIR, accessToken, realPath(path)));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取单个文件或目录的元信息。
	 * 
	 * @param path
	 *            需要获取属性的文件或目录路径。此路径是以应用文件夹为根目录的路径。
	 * @return Meta
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra1 meta(String path) throws BaiduPcsException,
			IOException {
		try {
			return FileMetaWithExtra1.fromSingleResponse(pcsService.meta(
					METHOD_META, accessToken, realPath(path)));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 批量获取文件或目录的元信息。
	 * 
	 * @param paths
	 *            需要获取属性的文件或目录路径列表。此路径是以应用文件夹为根目录的路径。
	 * @return Meta列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra1> meta(List<String> paths)
			throws BaiduPcsException, IOException {
		try {
			if (paths.isEmpty())
				return Collections.emptyList();
			return FileMetaWithExtra1.fromBatchResponse(pcsService.metaBatch(
					METHOD_META, accessToken, pathListParam(paths)));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取指定目录下的所有文件或目录列表。按照文件类型降序排列。<br>
	 * 如果path是普通文件，会返回空列表。
	 * 
	 * @param path
	 *            需要list的目录路径。此路径是以应用文件夹为根目录的路径。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> list(String path) throws BaiduPcsException,
			IOException {
		return list(path, null, null, -1, -1);
	}

	/**
	 * 获取指定目录下的所有文件或目录列表。
	 * 
	 * @param path
	 *            需要list的目录路径。此路径是以应用文件夹为根目录的路径。
	 * @param by
	 *            排序字段。如果为null，则默认为按照文件类型排序。
	 * @param order
	 *            排序顺序。如果为null，则默认为降序排序。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> list(String path, OrderBy by, Order order)
			throws BaiduPcsException, IOException {
		return list(path, by, order, -1, -1);
	}

	/**
	 * 获取指定目录下的文件或目录列表。
	 * 
	 * @param path
	 *            需要list的目录路径。此路径是以应用文件夹为根目录的路径。
	 * @param by
	 *            排序字段。如果为null，则默认为按照文件类型排序。
	 * @param order
	 *            排序顺序。如果为null，则默认为降序排序。
	 * @param startIndex
	 *            返回条目的起始索引，包含。如果为负数或endIndex为负数，则默认返回所有条目。
	 * @param endIndex
	 *            返回条目的结束索引，不包含。如果为负数或startIndex为负数，则默认返回所有条目。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> list(String path, OrderBy by, Order order,
			int startIndex, int endIndex) throws BaiduPcsException, IOException {
		try {
			if (by == null)
				by = OrderBy.DEFAULT;
			if (order == null)
				order = Order.DEFAULT;
			return FileMetaWithExtra2
					.fromResponse(pcsService.list(METHOD_LIST, accessToken,
							realPath(path), by.getRestParam(),
							order.getRestParam(),
							(startIndex >= 0 && endIndex >= 0) ? (startIndex
									+ "-" + endIndex) : null));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 移动单个文件/目录。
	 * 
	 * @param from
	 *            源文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param to
	 *            目标文件路径。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void move(String from, String to) throws BaiduPcsException,
			IOException {
		try {
			pcsService.move(METHOD_MOVE, accessToken, realPath(from),
					realPath(to));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 批量移动文件/目录。<br>
	 * 注意：非原子操作，失败时可能已经移动一部分文件/目录。
	 * 
	 * @param fromTos
	 *            源文件路径和目标文件路径对应的列表。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void move(List<FromTo> fromTos) throws BaiduPcsException,
			IOException {
		try {
			if (fromTos.isEmpty())
				return;
			pcsService.moveBatch(METHOD_MOVE, accessToken,
					moveBatchOrCopyBatchParam(fromTos));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 拷贝单个文件/目录。
	 * 
	 * @param from
	 *            源文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param to
	 *            目标文件路径。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void copy(String from, String to) throws BaiduPcsException,
			IOException {
		try {
			pcsService.copy(METHOD_COPY, accessToken, realPath(from),
					realPath(to));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 批量拷贝文件/目录。<br>
	 * 注意：非原子操作，失败时可能已经拷贝一部分文件/目录。
	 * 
	 * @param fromTos
	 *            源文件路径和目标文件路径对应的列表。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void copy(List<FromTo> fromTos) throws BaiduPcsException,
			IOException {
		try {
			if (fromTos.isEmpty())
				return;
			pcsService.copyBatch(METHOD_COPY, accessToken,
					moveBatchOrCopyBatchParam(fromTos));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 删除单个文件/目录。<br>
	 * 注意：<br>
	 * <li>文件/目录删除后默认临时存放在回收站内，删除文件或目录的临时存放不占用用户的空间配额；<br> <li>
	 * 存放有效期为10天，10天内可还原回原路径下，10天后则永久删除。
	 * 
	 * @param path
	 *            需要删除的文件或者目录路径。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void delete(String path) throws BaiduPcsException, IOException {
		try {
			pcsService.delete(METHOD_DELETE, accessToken, realPath(path));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 批量删除文件/目录。<br>
	 * 注意：非原子操作，失败时可能已经删除一部分文件/目录。
	 * 
	 * @param paths
	 *            需要删除的文件或者目录路径列表。此路径是以应用文件夹为根目录的路径。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void delete(List<String> paths) throws BaiduPcsException,
			IOException {
		try {
			if (paths.isEmpty())
				return;
			pcsService.deleteBatch(METHOD_DELETE, accessToken,
					pathListParam(paths));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 按文件名搜索文件（不支持查找目录）。
	 * 
	 * @param path
	 *            需要检索的目录路径。此路径是以应用文件夹为根目录的路径。
	 * @param word
	 *            关键词。检索的文件或目录名称包含关键词字符串。
	 * @param recursively
	 *            是否递归查询子目录。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> search(String path, String word,
			boolean recursively) throws BaiduPcsException, IOException {
		try {
			return FileMetaWithExtra2.fromResponse(pcsService.search(
					METHOD_SEARCH, accessToken, realPath(path), word,
					recursively ? BaiduPcsService.SEARCH_RE_1
							: BaiduPcsService.SEARCH_RE_0));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 以最高质量获取指定图片文件的缩略图。<br>
	 * 注意，有以下限制条件： <li>原图大小(0, 10M]； <li>原图类型: jpg、jpeg、bmp、gif、png； <li>
	 * 目标图类型:和原图的类型有关；例如：原图是gif图片，则缩略后也为gif图片。
	 * 
	 * @param path
	 *            源图片的路径。此路径是以应用文件夹为根目录的路径。
	 * @param height
	 *            指定缩略图的高度，取值范围为(0,1600]。
	 * @param width
	 *            指定缩略图的宽度，取值范围为(0,1600]。
	 * @return TypedInput
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public TypedInput generateThumbnail(String path, int height, int width)
			throws BaiduPcsException, IOException {
		return generateThumbnail(path, height, width, -1);
	}

	/**
	 * 获取指定图片文件的缩略图。<br>
	 * 注意，有以下限制条件： <li>原图大小(0, 10M]； <li>原图类型: jpg、jpeg、bmp、gif、png； <li>
	 * 目标图类型:和原图的类型有关；例如：原图是gif图片，则缩略后也为gif图片。
	 * 
	 * @param path
	 *            源图片的路径。此路径是以应用文件夹为根目录的路径。
	 * @param height
	 *            指定缩略图的高度，取值范围为(0,1600]。
	 * @param width
	 *            指定缩略图的宽度，取值范围为(0,1600]。
	 * @param quality
	 *            缩略图的质量，取值范围(0,100]，如指定-1则为100。
	 * @return TypedInput
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public TypedInput generateThumbnail(String path, int height, int width,
			int quality) throws BaiduPcsException, IOException {
		try {
			Integer realQuality;
			if (quality > 0 && quality <= 100)
				realQuality = quality;
			else if (quality == -1)
				realQuality = null;
			else
				throw new IllegalArgumentException("quality is out of range: "
						+ quality);
			return pcsService.generateThumbnail(METHOD_GENERATE_THUMBNAIL,
					accessToken, realPath(path), realQuality, height, width)
					.getBody();
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 文件增量更新操作查询接口。本接口有数秒延迟，但保证返回结果为最终一致。
	 * 
	 * @param cursor
	 *            用于标记更新断点。<br>
	 *            <li>首次调用cursor=null； <li>非首次调用，使用最后一次调用diff接口的返回结果中的cursor。
	 * @return DiffResponse
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public DiffResponse diff(String cursor) throws BaiduPcsException,
			IOException {
		try {
			return pcsService.diff(accessToken, METHOD_DIFF,
					cursor == null ? "null" : cursor);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对视频文件进行转码，实现实时观看视频功能。<br>
	 * 目前这个接口支持的源文件格式参考
	 * {@link BaiduPcsService#streaming(String, String, String, String)}方法的说明。
	 * 
	 * @param path
	 *            需要下载的视频文件路径。此路径是以应用文件夹为根目录的路径。
	 * @param type
	 *            转码后的格式。
	 * @return TypedInput
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public TypedInput streaming(String path, StreamingType type)
			throws BaiduPcsException, IOException {
		try {
			return pcsService.streaming(METHOD_STREAMING, accessToken,
					realPath(path), type.getRestParam()).getBody();
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 以视频、音频、图片及文档四种类型的视图获取所创建应用程序下的文件列表。最多返回1000条。
	 * 
	 * @param type
	 *            类型
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> listStream(ListStreamType type)
			throws BaiduPcsException, IOException {
		return listStream(type, -1, -1, null);
	}

	/**
	 * 以视频、音频、图片及文档四种类型的视图获取所创建应用程序下的文件列表。
	 * 
	 * @param type
	 *            类型
	 * @param start
	 *            返回条目控制起始值。若指定-1则为0。
	 * @param limit
	 *            返回条目控制长度。若指定-1则为1000。
	 * @param filterPath
	 *            需要过滤的前缀路径。此路径是以应用文件夹为根目录的路径。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> listStream(ListStreamType type, int start,
			int limit, String filterPath) throws BaiduPcsException, IOException {
		try {
			return FileMetaWithExtra2.fromResponse(pcsService.listStream(
					METHOD_LIST_STREAM, accessToken, type.getRestParam(),
					start == -1 ? null : String.valueOf(start),
					limit == -1 ? null : String.valueOf(limit),
					realPath(filterPath)));
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 秒传一个文件。<br>
	 * 注意： 被秒传文件必须大于256KB（即 256*1024 B）。<br>
	 * (非强一致接口，上传后请等待1秒后再读取)
	 * 
	 * @param path
	 *            上传文件的路径名。此路径是以应用文件夹为根目录的路径。
	 * @param recogInfo
	 *            文件识别信息。参数须全部填写。
	 * @param onDup
	 *            文件已存在的处理方式。如果为null，则默认为抛出异常。
	 * @return 文件信息
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public FileMetaWithExtra2 rapidUpload(String path,
			RapidUploadRecogInfo recogInfo, OnDup onDup)
			throws BaiduPcsException, IOException {
		try {
			if (onDup == null)
				onDup = OnDup.EXCEPTION;
			CreateFileResponse response = pcsService.rapidUpload(
					METHOD_RAPID_UPLOAD, accessToken, realPath(path),
					recogInfo.getContentLength(), recogInfo.getContentMD5(),
					recogInfo.getSliceMD5(), recogInfo.getContentCRC32(),
					onDup.getRestParam());
			return FileMetaWithExtra2.fromResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 添加离线下载任务。不限速，超时时间为3600秒。
	 * 
	 * @param savePath
	 *            下载后的文件保存路径（所在目录）。此路径是以应用文件夹为根目录的路径。
	 * @param sourceUrl
	 *            源文件的URL。
	 * @return 任务ID
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public String cloudDownloadStart(String savePath, String sourceUrl)
			throws BaiduPcsException, IOException {
		return cloudDownloadStart(savePath, sourceUrl, -1, -1, null);
	}

	/**
	 * 添加离线下载任务。
	 * 
	 * @param savePath
	 *            下载后的文件保存路径。此路径是以应用文件夹为根目录的路径。
	 * @param sourceUrl
	 *            源文件的URL。
	 * @param rateLimit
	 *            下载限速。若指定-1则不限速。
	 * @param timeout
	 *            下载超时时间。若指定-1则为3600秒。
	 * @param callback
	 *            下载完毕后的回调。若为null则不回调。
	 * @return 任务ID
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public String cloudDownloadStart(String savePath, String sourceUrl,
			int rateLimit, int timeout, String callback)
			throws BaiduPcsException, IOException {
		try {
			CloudDownloadAddTaskResponse response = pcsService
					.cloudDownloadAddTask(METHOD_CLOUD_DOWNLOAD_ADD_TASK,
							accessToken, null, realPath(savePath), sourceUrl,
							rateLimit == -1 ? null : rateLimit,
							timeout == -1 ? null : timeout, callback);
			return response.getTask_id();
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据一个任务ID号，查询离线下载任务进度信息。
	 * 
	 * @param taskID
	 *            任务ID
	 * @return 任务进度信息
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public CloudDownloadProgress cloudDownloadProgress(String taskID)
			throws BaiduPcsException, IOException {
		try {
			CloudDownloadQueryTaskResponse response = pcsService
					.cloudDownloadQueryTask(METHOD_CLOUD_DOWNLOAD_QUERY_TASK,
							accessToken, null, taskID,
							CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_PROGRESS_INFO);
			return CloudDownloadProgress.fromSingleQueryResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据多个任务ID号，批量查询离线下载任务进度信息。
	 * 
	 * @param taskIDs
	 *            任务ID列表
	 * @return 任务ID到进度信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Map<String, CloudDownloadProgress> cloudDownloadProgress(
			List<String> taskIDs) throws BaiduPcsException, IOException {
		try {
			if (taskIDs.isEmpty())
				return Collections.emptyMap();

			StringBuilder taskIDsParam = new StringBuilder();
			Iterator<String> itr = taskIDs.iterator();
			while (itr.hasNext()) {
				taskIDsParam.append(itr.next());
				if (itr.hasNext())
					taskIDsParam.append(",");
			}

			CloudDownloadQueryTaskResponse response = pcsService
					.cloudDownloadQueryTask(METHOD_CLOUD_DOWNLOAD_QUERY_TASK,
							accessToken, null, taskIDsParam.toString(),
							CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_PROGRESS_INFO);
			return CloudDownloadProgress.fromBatchQueryResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据一个任务ID号，查询离线下载任务元信息。
	 * 
	 * @param taskID
	 *            任务ID
	 * @return 任务元信息
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public CloudDownloadMeta cloudDownloadMeta(String taskID)
			throws BaiduPcsException, IOException {
		try {
			CloudDownloadQueryTaskResponse response = pcsService
					.cloudDownloadQueryTask(METHOD_CLOUD_DOWNLOAD_QUERY_TASK,
							accessToken, null, taskID,
							CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_TASK_INFO);
			return CloudDownloadMeta.fromSingleQueryResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据多个任务ID号，批量查询离线下载任务元信息。
	 * 
	 * @param taskIDs
	 *            任务ID列表
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Map<String, CloudDownloadMeta> cloudDownloadMeta(List<String> taskIDs)
			throws BaiduPcsException, IOException {
		try {
			if (taskIDs.isEmpty())
				return Collections.emptyMap();

			StringBuilder taskIDsParam = new StringBuilder();
			Iterator<String> itr = taskIDs.iterator();
			while (itr.hasNext()) {
				taskIDsParam.append(itr.next());
				if (itr.hasNext())
					taskIDsParam.append(",");
			}

			CloudDownloadQueryTaskResponse response = pcsService
					.cloudDownloadQueryTask(METHOD_CLOUD_DOWNLOAD_QUERY_TASK,
							accessToken, null, taskIDsParam.toString(),
							CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_TASK_INFO);
			return CloudDownloadMeta.fromBatchQueryResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 查询所有离线下载任务的元信息。返回前10个任务，降序排列。
	 * 
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Map<String, CloudDownloadMeta> cloudDownloadMeta()
			throws BaiduPcsException, IOException {
		return cloudDownloadMeta(null, -1, -1, false);
	}

	/**
	 * 根据指定条件，查询离线下载任务元信息。返回前10个任务，降序排列。
	 * 
	 * @param condition
	 *            查询条件。字段均可选。若指定null则视为不指定任何条件。
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Map<String, CloudDownloadMeta> cloudDownloadMeta(
			CloudDownloadQueryCondition condition) throws BaiduPcsException,
			IOException {
		return cloudDownloadMeta(condition, -1, -1, false);
	}

	/**
	 * 根据指定条件，查询离线下载任务元信息。
	 * 
	 * @param condition
	 *            查询条件。字段均可选。若指定null则视为不指定任何条件。
	 * @param start
	 *            查询任务起始位置。若指定-1则视为0。
	 * @param limit
	 *            返回任务数量。若指定-1则视为10。
	 * @param asc
	 *            返回结果是否升序排列。true为升序，false为降序。
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public Map<String, CloudDownloadMeta> cloudDownloadMeta(
			CloudDownloadQueryCondition condition, int start, int limit,
			boolean asc) throws BaiduPcsException, IOException {
		try {
			CloudDownloadQueryCondition realCondition = condition != null ? condition
					: new CloudDownloadQueryCondition();
			Integer realStart = start == -1 ? null : start;
			Integer realLimit = limit == -1 ? null : limit;
			Integer realAsc = asc ? CLOUD_DOWNLOAD_LIST_TASK_ASC_1
					: CLOUD_DOWNLOAD_LIST_TASK_ASC_0;
			String sourceUrl = realCondition.getSourceUrl();
			String savePath = realPath(realCondition.getSavePath());
			Integer createTime = realCondition.getCreateTime() == -1 ? null
					: realCondition.getCreateTime();
			Integer status = realCondition.getStatus() == null ? null
					: realCondition.getStatus().ordinal();

			CloudDownloadListTaskResponse response = pcsService
					.cloudDownloadListTask(METHOD_CLOUD_DOWNLOAD_LIST_TASK,
							accessToken, null, realStart, realLimit, realAsc,
							sourceUrl, savePath, createTime, status,
							CLOUD_DOWNLOAD_LIST_TASK_NEED_TASK_INFO_1);

			return CloudDownloadMeta.fromListResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据指定条件，查询离线下载任务ID。返回前10个任务，降序排列。
	 * 
	 * @param condition
	 *            查询条件。字段均可选。若指定null则视为不指定任何条件。
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<String> cloudDownloadTaskIDs(
			CloudDownloadQueryCondition condition) throws BaiduPcsException,
			IOException {
		return cloudDownloadTaskIDs(condition, -1, -1, false);
	}

	/**
	 * 根据指定条件，查询离线下载任务ID。返回前10个任务，降序排列。
	 * 
	 * @param condition
	 *            查询条件。字段均可选。若指定null则视为不指定任何条件。
	 * @param start
	 *            查询任务起始位置。若指定-1则视为0。
	 * @param limit
	 *            返回任务数量。若指定-1则视为10。
	 * @param asc
	 *            返回结果是否升序排列。true为升序，false为降序。
	 * @return 任务ID到元信息的映射
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<String> cloudDownloadTaskIDs(
			CloudDownloadQueryCondition condition, int start, int limit,
			boolean asc) throws BaiduPcsException, IOException {
		try {
			CloudDownloadQueryCondition realCondition = condition != null ? condition
					: new CloudDownloadQueryCondition();
			Integer realStart = start == -1 ? null : start;
			Integer realLimit = limit == -1 ? null : limit;
			Integer realAsc = asc ? CLOUD_DOWNLOAD_LIST_TASK_ASC_1
					: CLOUD_DOWNLOAD_LIST_TASK_ASC_0;
			String sourceUrl = realCondition.getSourceUrl();
			String savePath = realPath(realCondition.getSavePath());
			Integer createTime = realCondition.getCreateTime() == -1 ? null
					: realCondition.getCreateTime();
			Integer status = realCondition.getStatus() == null ? null
					: realCondition.getStatus().ordinal();

			CloudDownloadListTaskResponse response = pcsService
					.cloudDownloadListTask(METHOD_CLOUD_DOWNLOAD_LIST_TASK,
							accessToken, null, realStart, realLimit, realAsc,
							sourceUrl, savePath, createTime, status,
							CLOUD_DOWNLOAD_LIST_TASK_NEED_TASK_INFO_1);

			List<String> ids = new ArrayList<>(response.getTask_info().size());
			for (ListTaskInfo info : response.getTask_info())
				ids.add(info.getTask_id());
			return ids;
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 取消指定任务ID的离线下载任务。
	 * 
	 * @param taskID
	 *            任务ID
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void cloudDownloadCancel(String taskID) throws BaiduPcsException,
			IOException {
		try {
			pcsService.cloudDownloadCancelTask(
					METHOD_CLOUD_DOWNLOAD_CANCEL_TASK, accessToken, null,
					taskID);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取回收站中的文件及目录列表。如超过1000个则返回前1000个。
	 * 
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> listRecycle() throws BaiduPcsException,
			IOException {
		return listRecycle(-1, -1);
	}

	/**
	 * 获取回收站中的文件及目录列表。
	 * 
	 * @param start
	 *            返回条目的起始值。若提供-1则视为0。
	 * @param limit
	 *            返回条目的长度。若提供-1则视为1000。
	 * @return FileInfo列表
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public List<FileMetaWithExtra2> listRecycle(int start, int limit)
			throws BaiduPcsException, IOException {
		try {
			ListOrSearchResponse response = pcsService.listRecycle(
					METHOD_LIST_RECYCLE, accessToken, start == -1 ? null
							: start, limit == -1 ? null : limit);
			return FileMetaWithExtra2.fromResponse(response);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 还原单个文件或目录（非强一致接口，调用后请sleep 1秒读取）。
	 * 
	 * @param fsID
	 *            所还原的文件或目录在PCS的临时唯一标识ID。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void restore(String fsID) throws BaiduPcsException, IOException {
		try {
			pcsService.restore(METHOD_RESTORE, accessToken, fsID);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 批量还原文件或目录（非强一致接口，调用后请sleep1秒 ）。<br>
	 * 注意：非原子操作，失败时可能已经还原一部分文件/目录。
	 * 
	 * @param fsIDs
	 *            所还原的文件或目录在PCS的临时唯一标识ID列表。
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void restore(List<String> fsIDs) throws BaiduPcsException,
			IOException {
		try {
			if (fsIDs.isEmpty())
				return;

			List<RestoreFileInfo> fileInfoList = new ArrayList<>(fsIDs.size());
			for (String fsID : fsIDs) {
				RestoreFileInfo fileInfo = new RestoreFileInfo();
				fileInfo.setFs_id(fsID);
				fileInfoList.add(fileInfo);
			}
			RestoreBatchParam param = new RestoreBatchParam();
			param.setList(fileInfoList);

			pcsService.restoreBatch(METHOD_RESTORE, accessToken, param);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 清空回收站
	 * 
	 * @throws BaiduPcsException
	 * @throws IOException
	 *             网络错误
	 */
	public void clearRecycle() throws BaiduPcsException, IOException {
		try {
			pcsService.clearRecycle(METHOD_DELETE, accessToken,
					CLEAR_RECYCLE_TYPE);
		} catch (IOException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private String realPath(String path) {
		if (path == null)
			return null;

		if (!path.startsWith("/"))
			path = "/" + path;
		return pathPrefix + path;
	}

	private PathListParam pathListParam(List<String> pathStrs) {
		List<Path> paths = new ArrayList<>(pathStrs.size());
		for (String pathStr : pathStrs)
			paths.add(new Path(realPath(pathStr)));
		return new PathListParam(paths);
	}

	private MoveBatchOrCopyBatchParam moveBatchOrCopyBatchParam(
			List<FromTo> oriFromTos) {
		List<FromTo> fromTos = new ArrayList<>(oriFromTos.size());
		for (FromTo fromTo : oriFromTos)
			fromTos.add(new FromTo(realPath(fromTo.getFrom()), realPath(fromTo
					.getTo())));
		return new MoveBatchOrCopyBatchParam(fromTos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result
				+ ((pathPrefix == null) ? 0 : pathPrefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BaiduPcs))
			return false;
		BaiduPcs other = (BaiduPcs) obj;
		if (accessToken == null) {
			if (other.accessToken != null)
				return false;
		} else if (!accessToken.equals(other.accessToken))
			return false;
		if (pathPrefix == null) {
			if (other.pathPrefix != null)
				return false;
		} else if (!pathPrefix.equals(other.pathPrefix))
			return false;
		return true;
	}

}
