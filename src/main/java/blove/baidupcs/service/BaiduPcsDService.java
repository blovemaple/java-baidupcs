package blove.baidupcs.service;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

public interface BaiduPcsDService {
	/**
	 * 构建RestAdapter.Builder需要使用的server配置。<br>
	 * 也可以使用{@link BaiduPcsService#SERVER}。
	 */
	// String SERVER = "https://d.pcs.baidu.com/rest/2.0/pcs";
	String SERVER = "https://pcs.baidu.com/rest/2.0/pcs";

	/**
	 * download方法method参数值。
	 */
	String METHOD_DOWNLOAD = "download";

	/**
	 * 下载单个文件。<br>
	 * Download接口支持HTTP协议标准range定义，通过指定range的取值可以实现断点下载功能。 例如：<br>
	 * 如果在request消息中指定“Range: bytes=0-99”，那么响应消息中会返回该文件的前100个字节的内容；继续指定“Range:
	 * bytes=100-199”，那么响应消息中会返回该文件的第二个100字节内容。
	 * 
	 * @param method
	 *             固定值，download。
	 * @param access_token
	 *             开发者准入标识。
	 * @param path
	 *             下载文件路径，以/开头的绝对路径。<br>
	 *             注意：<br>
	 *             <li>路径长度限制为1000<br> <li>路径中不能包含以下字符：\\ ? | " > < : *<br> <li>
	 *             文件名或路径名开头结尾不能是“.”或空白字符，空白字符包括: \r, \n, \t, 空格, \0, \x0B
	 * @param range
	 *             HTTP Range header，通过指定range的取值可以实现断点下载功能。 例如：<br>
	 *             如果指定“bytes=0-99”，那么响应消息中会返回该文件的前100个字节的内容；<br>
	 *             继续指定“bytes=100-199”，那么响应消息中会返回该文件的第二个100字节内容。<br>
	 *             如果为null则下载整个文件。
	 * @return 文件内容（原始Response对象）
	 * @throws Throwable
	 *              ErrorHandler可能返回的任何异常或错误
	 */
	@GET("/file")
	Response download(@Query("method") String method, @Query("access_token") String access_token,
			@Query("path") String path, @Header("Range") String range) throws Throwable;
}
