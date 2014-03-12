package blove.baidupcs.api.error;

import retrofit.client.Response;
import retrofit.converter.ConversionException;

/**
 * 调用请求的接口或返回数据的接口与当前百度云存储实际接口不符时抛出此异常。一般是由于请求参数错误，或服务接口升级而与升级前不兼容造成的。
 * 
 * @author blove
 */
public class InterfaceException extends BaiduPcsException {
	private static final long serialVersionUID = 4290017956836280139L;

	/**
	 * 当请求数据接口不符时创建实例。
	 * 
	 * @param errorResponse
	 *            服务器返回的错误信息
	 * @param httpResponse
	 * 
	 */
	public InterfaceException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

	/**
	 * 当返回数据接口不符时创建实例。
	 * 
	 * @param cause
	 *            返回的json转换异常
	 * @param httpResponse
	 */
	public InterfaceException(ConversionException cause, Response httpResponse) {
		super(cause, httpResponse);
	}

}
