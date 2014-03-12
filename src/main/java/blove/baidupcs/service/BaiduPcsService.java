package blove.baidupcs.service;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import blove.baidupcs.service.request.files.CreateSuperFileParam;
import blove.baidupcs.service.request.files.MoveBatchOrCopyBatchParam;
import blove.baidupcs.service.request.files.PathListParam;
import blove.baidupcs.service.request.files.RestoreBatchParam;
import blove.baidupcs.service.response.BasicResponse;
import blove.baidupcs.service.response.files.CloudDownloadAddTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadListTaskResponse;
import blove.baidupcs.service.response.files.CloudDownloadQueryTaskResponse;
import blove.baidupcs.service.response.files.CopyOrMoveResponse;
import blove.baidupcs.service.response.files.CreateFileResponse;
import blove.baidupcs.service.response.files.DiffResponse;
import blove.baidupcs.service.response.files.ListOrSearchResponse;
import blove.baidupcs.service.response.files.ListStreamResponse;
import blove.baidupcs.service.response.files.MetaBatchResponse;
import blove.baidupcs.service.response.files.MkdirResponse;
import blove.baidupcs.service.response.files.QuotaResponse;
import blove.baidupcs.service.response.files.RestoreResponse;

public interface BaiduPcsService {
	/**
	 * 构建RestAdapter.Builder需要使用的server配置。
	 */
	String SERVER = "https://pcs.baidu.com/rest/2.0/pcs";

	/**
	 * info方法的method参数值。
	 */
	String METHOD_INFO = "info";
	/**
	 * createSuperFile方法的method参数值。
	 */
	String METHOD_CREATESUPERFILE = "createsuperfile";
	/**
	 * mkdir方法的method参数值。
	 */
	String METHOD_MKDIR = "mkdir";
	/**
	 * meta和metaBatch方法的method参数值。
	 */
	String METHOD_META = "meta";
	/**
	 * list方法的method参数值。
	 */
	String METHOD_LIST = "list";
	/**
	 * move和moveBatch方法的method参数值。
	 */
	String METHOD_MOVE = "move";
	/**
	 * copy和copyBatch方法的method参数值。
	 */
	String METHOD_COPY = "copy";
	/**
	 * delete、deleteBatch和clearRecycle方法的method参数值。
	 */
	String METHOD_DELETE = "delete";
	/**
	 * search方法的method参数值。
	 */
	String METHOD_SEARCH = "search";
	/**
	 * generateThumbnail方法的method参数值。
	 */
	String METHOD_GENERATE_THUMBNAIL = "generate";
	/**
	 * diff方法的method参数值。
	 */
	String METHOD_DIFF = "diff";
	/**
	 * streaming方法的method参数值。
	 */
	String METHOD_STREAMING = "streaming";
	/**
	 * listStream方法的method参数值。
	 */
	String METHOD_LIST_STREAM = "list";
	/**
	 * rapidUpload方法的method参数值。
	 */
	String METHOD_RAPID_UPLOAD = "rapidupload";
	/**
	 * cloudDownloadAddTask方法的method参数值。
	 */
	String METHOD_CLOUD_DOWNLOAD_ADD_TASK = "add_task";
	/**
	 * cloudDownloadQueryTask方法的method参数值。
	 */
	String METHOD_CLOUD_DOWNLOAD_QUERY_TASK = "query_task";
	/**
	 * cloudDownloadListTask方法的method参数值。
	 */
	String METHOD_CLOUD_DOWNLOAD_LIST_TASK = "list_task";
	/**
	 * cloudDownloadCancelTask方法的method参数值。
	 */
	String METHOD_CLOUD_DOWNLOAD_CANCEL_TASK = "cancel_task";
	/**
	 * listRecycle方法的method参数值。
	 */
	String METHOD_LIST_RECYCLE = "listrecycle";
	/**
	 * restore和restoreBatch方法的method参数值。
	 */
	String METHOD_RESTORE = "restore";

	/**
	 * createSuperFile方法ondup参数值，表示覆盖同名文件。
	 */
	String CREATE_SUPER_FILE_ONDUP_OVERWRITE = "overwrite";
	/**
	 * createSuperFile方法ondup参数值，表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀”。
	 */
	String CREATE_SUPER_FILE_ONDUP_NEWCOPY = "newcopy";

	/**
	 * 获取目录下的文件列表的排序参数值，根据修改时间排序。
	 */
	String LIST_BY_TIME = "time";
	/**
	 * 获取目录下的文件列表的排序参数值，根据文件名排序。
	 */
	String LIST_BY_NAME = "name";
	/**
	 * 获取目录下的文件列表的排序参数值，根据大小（注意目录无大小）排序。
	 */
	String LIST_BY_SIZE = "size";

	/**
	 * 获取目录下的文件列表的顺序参数值，采用升序排序。
	 */
	String LIST_ORDER_ASC = "asc";
	/**
	 * 获取目录下的文件列表的顺序参数值，采用降序排序。
	 */
	String LIST_ORDER_DESC = "desc";

	/**
	 * 搜索文件的递归参数值，不递归。
	 */
	String SEARCH_RE_0 = "0";
	/**
	 * 搜索文件的递归参数值，递归。
	 */
	String SEARCH_RE_1 = "1";

	/**
	 * 视频转码的格式参数值。M3U8_320_240。
	 */
	String STREAMING_TYPE_M3U8_320_240 = "M3U8_320_240";
	/**
	 * 视频转码的格式参数值。M3U8_480_224。
	 */
	String STREAMING_TYPE_M3U8_480_224 = "M3U8_480_224";
	/**
	 * 视频转码的格式参数值。M3U8_480_360。
	 */
	String STREAMING_TYPE_M3U8_480_360 = "M3U8_480_360";
	/**
	 * 视频转码的格式参数值。M3U8_640_480。
	 */
	String STREAMING_TYPE_M3U8_640_480 = "M3U8_640_480";
	/**
	 * 视频转码的格式参数值。M3U8_854_480。
	 */
	String STREAMING_TYPE_M3U8_854_480 = "M3U8_854_480";

	/**
	 * 获取流式文件列表的类型参数值。视频。
	 */
	String LIST_STREAM_TYPE_VIDEO = "video";
	/**
	 * 获取流式文件列表的类型参数值。音频。
	 */
	String LIST_STREAM_TYPE_AUDIO = "audio";
	/**
	 * 获取流式文件列表的类型参数值。图片。
	 */
	String LIST_STREAM_TYPE_IMAGE = "image";
	/**
	 * 获取流式文件列表的类型参数值。文档。
	 */
	String LIST_STREAM_TYPE_DOC = "doc";

	/**
	 * rapidUpload方法ondup参数值，表示覆盖同名文件。
	 */
	String RAPID_UPLOAD_ONDUP_OVERWRITE = "overwrite";
	/**
	 * rapidUpload方法ondup参数值，表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀”。
	 */
	String RAPID_UPLOAD_ONDUP_NEWCOPY = "newcopy";

	/**
	 * 离线下载查询opType参数值，查询任务信息。
	 */
	Integer CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_TASK_INFO = 0;
	/**
	 * 离线下载查询opType参数值，查询进度信息。
	 */
	Integer CLOUD_DOWNLOAD_QUERY_TASK_OP_TYPE_PROGRESS_INFO = 1;

	/**
	 * 离线下载查询(list)asc参数值，降序。
	 */
	Integer CLOUD_DOWNLOAD_LIST_TASK_ASC_0 = 0;
	/**
	 * 离线下载查询(list)asc参数值，升序。
	 */
	Integer CLOUD_DOWNLOAD_LIST_TASK_ASC_1 = 1;

	/**
	 * 离线下载查询(list)need_task_info参数值，不需要返回任务信息。
	 */
	Integer CLOUD_DOWNLOAD_LIST_TASK_NEED_TASK_INFO_0 = 0;
	/**
	 * 离线下载查询(list)need_task_info参数值，需要返回任务信息。
	 */
	Integer CLOUD_DOWNLOAD_LIST_TASK_NEED_TASK_INFO_1 = 1;

	/**
	 * 清空回收站的type参数值。
	 */
	String CLEAR_RECYCLE_TYPE = "recycle";

	/**
	 * 获取当前用户空间配额信息。
	 * 
	 * @param method
	 *            固定值：info。
	 * @param accessToken
	 *            开发者准入标识。
	 * @return
	 */
	@GET("/quota")
	QuotaResponse quotaInfo(@Query("method") String method,
			@Query("access_token") String accessToken) throws Throwable;

	/**
	 * 与分片文件上传的upload方法配合使用，可实现超大文件（>2G）上传，同时也可用于断点续传的场景。
	 * 
	 * @param method
	 *            固定值，createsuperfile。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            上传文件路径（含上传的文件名称）。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param param
	 *            block_list数组，数组的取值为子文件内容的MD5；子文件至少两个，最多1024个。<br>
	 *            本参数必须放在Http Body中进行传输，value示例： <br>
	 *            { "block_list":["d41d8cd98f00b204e9800998ecf8427e",
	 *            "89dfb274b42951b973fc92ee7c252166"
	 *            ,"1c83fe229cb9b1f6116aa745b4ef3c0d"]}
	 * @param ondup
	 *            可选参数。overwrite：表示覆盖同名文件；newcopy：表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀
	 *            ”。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CreateFileResponse createSuperFile(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path,
			@Part("param") CreateSuperFileParam param,
			@Query("ondup") String ondup) throws Throwable;

	/**
	 * 为当前用户创建一个目录。
	 * 
	 * @param method
	 *            固定值，mkdir。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要创建的目录，以/开头的绝对路径。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	MkdirResponse mkdir(@Query("method") String method,
			@Query("access_token") String accessToken, @Part("path") String path)
			throws Throwable;

	/**
	 * 获取单个文件或目录的元信息。
	 * 
	 * @param method
	 *            固定值，meta。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要获取文件属性的目录，以/开头的绝对路径。如：/apps/album/a/b/c<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	MetaBatchResponse meta(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path) throws Throwable;

	/**
	 * 批量获取文件或目录的元信息。
	 * 
	 * @param method
	 *            固定值，meta。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param param
	 *            JSON字符串。<br>
	 *            {"list":[{"path":
	 *            "\/apps\/album\/a\/b\/c"},{"path":"\/apps\/album\/a\/b\/d"}]}<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	MetaBatchResponse metaBatch(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("param") PathListParam param) throws Throwable;

	/**
	 * 获取目录下的文件列表。
	 * 
	 * @param method
	 *            固定值，list。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要list的目录，以/开头的绝对路径。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param by
	 *            可选参数。排序字段，缺省根据文件类型排序：<br>
	 *            <li>time（修改时间）<br> <li>name（文件名）<br> <li>size（大小，注意目录无大小）
	 * @param order
	 *            可选参数。“asc”或“desc”，缺省采用降序排序。<br>
	 *            <li>asc（升序）<br> <li>desc（降序）
	 * @param limit
	 *            可选参数。返回条目控制，参数格式为：n1-n2。返回结果集的[n1, n2)之间的条目，缺省返回所有条目；n1从0开始。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	ListOrSearchResponse list(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path, @Query("by") String by,
			@Query("order") String order, @Query("limit") String limit)
			throws Throwable;

	/**
	 * 移动单个文件/目录。<br>
	 * 注意：调用move接口时，目标文件的名称如果和源文件不相同，将会在move操作时对文件进行重命名。
	 * 
	 * @param method
	 *            固定值，move。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param from
	 *            源文件地址（包括文件名）。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是‘.’或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param to
	 *            目标文件地址（包括文件名）。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return 如果move操作执行成功，那么response会返回执行成功的from/to列表。
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CopyOrMoveResponse move(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("from") String from, @Part("to") String to) throws Throwable;

	/**
	 * 批量移动文件/目录。<br>
	 * 注意：<br>
	 * <li>批量执行move操作时，move接口一次对请求参数中的每个from/to进行操作； <li>
	 * 执行失败就会退出，成功就继续，返回执行成功的from/to列表。
	 * 
	 * @param method
	 *            固定值，move。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param param
	 *            源文件地址和目标文件地址对应的列表。<br>
	 *            {"list":[{"from":"/apps/album/a/b/c","to":"/apps/album/b/b/c"}
	 *            ,{"from":"/apps/album/a/b/d","to":"/apps/album/b/b/d"}]}<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return 返回参数extra由list数组组成，list数组的两个元素分别是“from”和“to”，代表move操作的源地址和目的地址。
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CopyOrMoveResponse moveBatch(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("param") MoveBatchOrCopyBatchParam param) throws Throwable;

	/**
	 * 拷贝文件(目录)。
	 * 
	 * @param method
	 *            固定值，copy。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param from
	 *            源文件地址（包括文件名）。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是‘.’或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param to
	 *            目标文件地址（包括文件名）。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return 如果copy操作执行成功，那么response会返回执行成功的from/to列表。
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CopyOrMoveResponse copy(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("from") String from, @Part("to") String to) throws Throwable;

	/**
	 * 批量拷贝文件(目录)。
	 * 注意：批量执行copy操作时，copy接口一次对请求参数中的每个from/to进行操作；执行失败就会退出，成功就继续，返回执行成功的from
	 * /to列表。
	 * 
	 * @param method
	 *            固定值，copy。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param param
	 *            源文件地址和目标文件地址对应的列表。<br>
	 *            {"list":[{"from":"/apps/album/a/b/c","to":"/apps/album/b/b/c"}
	 *            ,{"from":"/apps/album/a/b/d","to":"/apps/album/b/b/d"}]}<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return 返回参数extra由list数组组成，list数组的两个元素分别是“from”和“to”，代表copy操作的源地址和目的地址。
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CopyOrMoveResponse copyBatch(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("param") MoveBatchOrCopyBatchParam param) throws Throwable;

	/**
	 * 删除单个文件/目录。<br>
	 * 注意：<br>
	 * <li>文件/目录删除后默认临时存放在回收站内，删除文件或目录的临时存放不占用用户的空间配额；<br> <li>
	 * 存放有效期为10天，10天内可还原回原路径下，10天后则永久删除。
	 * 
	 * @param method
	 *            固定值，delete。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要删除的文件或者目录路径。如：/apps/album/a/b/c<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	BasicResponse delete(@Query("method") String method,
			@Query("access_token") String accessToken, @Part("path") String path)
			throws Throwable;

	/**
	 * 批量删除文件/目录。<br>
	 * 注意：<br>
	 * <li>文件/目录删除后默认临时存放在回收站内，删除文件或目录的临时存放不占用用户的空间配额；<br> <li>
	 * 存放有效期为10天，10天内可还原回原路径下，10天后则永久删除。
	 * 
	 * @param method
	 *            固定值，delete。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param param
	 *            需要删除的文件或者目录路径。如：<br>
	 *            {"list":[{"path":
	 *            "\/apps\/album\/a\/b\/c"},{"path":"\/apps\/album\/a\/b\/d"}]}<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	BasicResponse deleteBatch(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("param") PathListParam param) throws Throwable;

	/**
	 * 按文件名搜索文件（不支持查找目录）。
	 * 
	 * @param method
	 *            固定值，search。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要检索的目录。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param wd
	 *            关键词。
	 * @param re
	 *            可选参数。是否递归。“0”表示不递归；“1”表示递归。缺省为“0”。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	ListOrSearchResponse search(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path, @Query("wd") String wd,
			@Query("re") String re) throws Throwable;

	/**
	 * 获取指定图片文件的缩略图。<br>
	 * 注意，有以下限制条件： <li>原图大小(0, 10M]； <li>原图类型: jpg、jpeg、bmp、gif、png； <li>
	 * 目标图类型:和原图的类型有关；例如：原图是gif图片，则缩略后也为gif图片。
	 * 
	 * @param method
	 *            固定值，generate。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            源图片的路径。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param quality
	 *            可选参数。缩略图的质量，默认为“100”，取值范围(0,100]。
	 * @param height
	 *            指定缩略图的高度，取值范围为(0,1600]。
	 * @param width
	 *            指定缩略图的宽度，取值范围为(0,1600]。
	 * @return 缩略图文件内容（原始Response对象）
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/thumbnail")
	Response generateThumbnail(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path, @Query("quality") Integer quality,
			@Query("height") Integer height, @Query("width") Integer width)
			throws Throwable;

	/**
	 * 文件增量更新操作查询接口。本接口有数秒延迟，但保证返回结果为最终一致。
	 * 
	 * @param accessToken
	 *            开发者准入标识。
	 * @param method
	 *            固定值，diff。
	 * @param cursor
	 *            用于标记更新断点。<br>
	 *            <li>首次调用cursor=null； <li>非首次调用，使用最后一次调用diff接口的返回结果中的cursor。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	DiffResponse diff(@Query("access_token") String accessToken,
			@Query("method") String method, @Query("cursor") String cursor)
			throws Throwable;

	/**
	 * 对视频文件进行转码，实现实时观看视频功能。<br>
	 * 目前这个接口支持的源文件格式如下：
	 * <table>
	 * <tr>
	 * <td><b>格式名称</b></td>
	 * <td><b>扩展名</b></td>
	 * <td><b>备注</b></td>
	 * </tr>
	 * <tr>
	 * <td>Apple HTTP Live Streaming</td>
	 * <td>m3u8/m3u</td>
	 * <td>iOS支持的视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>ASF</td>
	 * <td>asf</td>
	 * <td>视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>AVI</td>
	 * <td>avi</td>
	 * <td>视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>Flash Video (FLV)</td>
	 * <td>flv Macromedia</td>
	 * <td>Flash视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>GIF Animation</td>
	 * <td>gif</td>
	 * <td>视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>Matroska</td>
	 * <td>mkv</td>
	 * <td>Matroska/WebM视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>MOV/QuickTime/MP4</td>
	 * <td>mov/mp4/m4a/3gp/3g2/mj2</td>
	 * <td>支持3GP、3GP2、PSP、iPod 之类视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>MPEG-PS (program stream)</td>
	 * <td>mpeg</td>
	 * <td>也就是VOB文件、SVCD DVD格式</td>
	 * </tr>
	 * <tr>
	 * <td>MPEG-TS (transport stream)</td>
	 * <td>ts</td>
	 * <td>即DVB传输流</td>
	 * </tr>
	 * <tr>
	 * <td>RealMedia</td>
	 * <td>rm/rmvb</td>
	 * <td>Real视频格式</td>
	 * </tr>
	 * <tr>
	 * <td>WebM</td>
	 * <td>webm</td>
	 * <td>Html视频格式</td>
	 * </tr>
	 * </table>
	 * 
	 * @param method
	 *            固定值为streaming。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            需要下载的视频文件路径，以/开头的绝对路径，需含源文件的文件名。<br>
	 *            注意： <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param type
	 *            目前支持以下格式：<br>
	 *            M3U8_320_240、M3U8_480_224、M3U8_480_360、
	 *            M3U8_640_480和M3U8_854_480
	 * @return 文件内容（原始Response对象）
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	Response streaming(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path, @Query("type") String type)
			throws Throwable;

	/**
	 * 以视频、音频、图片及文档四种类型的视图获取所创建应用程序下的文件列表。
	 * 
	 * @param method
	 *            固定值为list
	 * @param accessToken
	 *            开发者准入标识。
	 * @param type
	 *            类型分为video、audio、image及doc四种。
	 * @param start
	 *            可选参数。返回条目控制起始值，缺省值为0。
	 * @param limit
	 *            可选参数。返回条目控制长度，缺省为1000，可配置。
	 * @param filter_path
	 *            可选参数。需要过滤的前缀路径，如：/apps/album<br>
	 *            注意： <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/stream")
	ListStreamResponse listStream(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("type") String type, @Query("start") String start,
			@Query("limit") String limit,
			@Query("filter_path") String filter_path) throws Throwable;

	/**
	 * 秒传一个文件。<br>
	 * 注意： <li>被秒传文件必须大于256KB（即 256*1024 B）。 <li>校验段为文件的前256KB，秒传接口需要提供校验段的MD5。<br>
	 * (非强一致接口，上传后请等待1秒后再读取)
	 * 
	 * @param method
	 *            固定值为rapidupload。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            上传文件的全路径名。<br>
	 *            注意： <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param contentLength
	 *            待秒传的文件长度。
	 * @param contentMD5
	 *            待秒传的文件的MD5。
	 * @param sliceMD5
	 *            待秒传文件校验段的MD5。
	 * @param contentCRC32
	 *            待秒传文件CRC32
	 * @param ondup
	 *            可选参数。overwrite：表示覆盖同名文件；newcopy：表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀
	 *            ”。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	CreateFileResponse rapidUpload(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path,
			@Query("content-length") Long contentLength,
			@Query("content-md5") String contentMD5,
			@Query("slice-md5") String sliceMD5,
			@Query("content-crc32") String contentCRC32,
			@Query("ondup") String ondup) throws Throwable;

	/**
	 * 添加离线下载任务，实现单个文件离线下载。
	 * 
	 * @param method
	 *            固定值为add_task。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param expires
	 *            可选参数。请求失效时间，如果有，则会校验。
	 * @param savePath
	 *            下载后的文件保存路径。<br>
	 *            注意： <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param sourceUrl
	 *            源文件的URL。
	 * @param rateLimit
	 *            可选参数。下载限速，默认不限速。
	 * @param timeout
	 *            可选参数。下载超时时间，默认3600秒。
	 * @param callback
	 *            可选参数。下载完毕后的回调，默认为空。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/services/cloud_dl")
	CloudDownloadAddTaskResponse cloudDownloadAddTask(
			@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("expires") Integer expires,
			@Query("save_path") String savePath,
			@Query("source_url") String sourceUrl,
			@Query("rate_limit") Integer rateLimit,
			@Query("timeout") Integer timeout,
			@Query("callback") String callback) throws Throwable;

	/**
	 * 根据任务ID号，查询离线下载任务信息及进度信息。
	 * 
	 * @param method
	 *            固定值为query_task。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param expires
	 *            可选参数。请求失效时间，如果有，则会校验。
	 * @param taskIDs
	 *            要查询的任务ID信息，如：1,2,3,4
	 * @param opType
	 *            0：查任务信息；1：查进度信息，默认为1
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/services/cloud_dl")
	CloudDownloadQueryTaskResponse cloudDownloadQueryTask(
			@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("expires") Integer expires,
			@Query("task_ids") String taskIDs, @Query("op_type") Integer opType)
			throws Throwable;

	/**
	 * 查询离线下载任务ID列表及任务信息。
	 * 
	 * @param method
	 *            固定值为list_task。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param expires
	 *            可选参数。请求失效时间，如果有，则会校验。
	 * @param start
	 *            可选参数。查询任务起始位置，默认为0。
	 * @param limit
	 *            可选参数。设定返回任务数量，默认为10。
	 * @param asc
	 *            可选参数。0：降序，默认值 ；1：升序
	 * @param sourceUrl
	 *            可选参数。源地址URL，默认为空。
	 * @param savePath
	 *            可选参数。文件保存路径，默认为空。<br>
	 *            注意： <li>路径长度限制为1000 <li>路径中不能包含以下字符：\\ ? | " > < : * <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param createTime
	 *            可选参数。任务创建时间，默认为空。
	 * @param status
	 *            可选参数。任务状态，默认为空。
	 * @param needTaskInfo
	 *            可选参数。是否需要返回任务信息:<br>
	 *            <li>0：不需要 <li>1：需要，默认为1
	 * @return 任务信息或任务列表
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/services/cloud_dl")
	CloudDownloadListTaskResponse cloudDownloadListTask(
			@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("expires") Integer expires, @Query("start") Integer start,
			@Query("limit") Integer limit, @Query("asc") Integer asc,
			@Query("source_url") String sourceUrl,
			@Query("save_path") String savePath,
			@Query("create_time") Integer createTime,
			@Query("status") Integer status,
			@Query("need_task_info") Integer needTaskInfo) throws Throwable;

	/**
	 * 取消离线下载任务。
	 * 
	 * @param method
	 *            固定值为cancel_task。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param expires
	 *            可选参数。请求失效时间，如果有，则会校验。
	 * @param taskID
	 *            要取消的任务ID号。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/services/cloud_dl")
	BasicResponse cloudDownloadCancelTask(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("expires") Integer expires, @Query("task_id") String taskID)
			throws Throwable;

	/*
	 * 回收站用于临时存放删除文件，且不占空间配额；但回收站的文件存放具有10天有效期，删除文件默认扔到回收站，10天内可通过回收站找回，逾期永久删除。
	 */

	/**
	 * 获取回收站中的文件及目录列表。
	 * 
	 * @param method
	 *            固定值为listrecycle。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param start
	 *            可选参数。返回条目的起始值，缺省值为0。
	 * @param limit
	 *            可选参数。返回条目的长度，缺省值为1000。
	 * @return 回收站文件或目录列表信息
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	ListOrSearchResponse listRecycle(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("start") Integer start, @Query("limit") Integer limit)
			throws Throwable;

	/**
	 * 还原单个文件或目录（非强一致接口，调用后请sleep 1秒读取）。
	 * 
	 * @param method
	 *            固定值为restore。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param fs_id
	 *            所还原的文件或目录在PCS的临时唯一标识ID。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	RestoreResponse restore(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("fs_id") String fs_id) throws Throwable;

	/**
	 * 批量还原文件或目录（非强一致接口，调用后请sleep1秒 ）
	 * 
	 * @param method
	 *            固定值为restore。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param param
	 *            Body中的JSON串，用于批量处理
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	RestoreResponse restoreBatch(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Part("param") RestoreBatchParam param) throws Throwable;

	/**
	 * 清空回收站。
	 * 
	 * @param method
	 *            固定值为delete。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param type
	 *            固定值为recycle
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	BasicResponse clearRecycle(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("type") String type) throws Throwable;
}
