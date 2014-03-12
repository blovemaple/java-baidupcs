package blove.baidupcs.service;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedOutput;
import blove.baidupcs.service.response.files.CreateFileResponse;
import blove.baidupcs.service.response.files.UploadFileBlockResponse;

public interface BaiduPcsCService {
	/**
	 * 构建RestAdapter.Builder需要使用的server配置。<br>
	 * 也可以使用{@link BaiduPcsService#SERVER}。
	 */
	String SERVER = "https://c.pcs.baidu.com/rest/2.0/pcs";

	/**
	 * upload和uploadBlock方法method参数值。
	 */
	String METHOD_UPLOAD = "upload";

	/**
	 * upload方法ondup参数值，表示覆盖同名文件。
	 */
	String UPLOAD_ONDUP_OVERWRITE = "overwrite";
	/**
	 * upload方法ondup参数值，表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀”。
	 */
	String UPLOAD_ONDUP_NEWCOPY = "newcopy";

	/**
	 * uploadBlock方法type参数值。
	 */
	String UPLOADBLOCK_TYPE_TMPFILE = "tmpfile";

	/**
	 * 上传单个文件。<br>
	 * 百度PCS服务目前支持最大2G的单个文件上传。<br>
	 * 如需支持超大文件（>2G）的断点续传，请参考下面的“分片文件上传”方法。
	 * 
	 * @param method
	 *            固定值，upload。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param path
	 *            上传文件路径（含上传的文件名称)。<br>
	 *            注意：<br>
	 *            <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *            文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param file
	 *            上传文件的内容。API要求fileName值不能为null。
	 * @param ondup
	 *            可选参数。overwrite：表示覆盖同名文件；newcopy：表示生成文件副本并进行重命名，命名规则为“文件名_日期.后缀”。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	CreateFileResponse upload(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("path") String path, @Part("file") TypedOutput file,
			@Query("ondup") String ondup) throws Throwable;

	/**
	 * 百度PCS服务支持每次直接上传最大2G的单个文件。<br>
	 * 如需支持上传超大文件（>2G），则可以通过组合调用分片文件上传的upload方法和createsuperfile方法实现：<br>
	 * 首先，将超大文件分割为2G以内的单文件，并调用upload将分片文件依次上传；<br>
	 * 其次，调用createsuperfile，完成分片文件的重组。<br>
	 * 除此之外，如果应用中需要支持断点续传的功能，也可以通过分片上传文件并调用createsuperfile接口的方式实现。
	 * 
	 * @param method
	 *            固定值，upload。
	 * @param accessToken
	 *            开发者准入标识。
	 * @param type
	 *            固定值，tmpfile。
	 * @param file
	 *            上传文件的内容。API要求fileName值不能为null。
	 * @return
	 * @throws Throwable
	 *             ErrorHandler可能返回的任何异常或错误
	 */
	@Multipart
	@POST("/file")
	UploadFileBlockResponse uploadBlock(@Query("method") String method,
			@Query("access_token") String accessToken,
			@Query("type") String type, @Part("file") TypedOutput file)
			throws Throwable;
}
