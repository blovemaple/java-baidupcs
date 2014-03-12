package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 某文件已存在导致不能完成某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsFileExistsException extends FSRelatedException {
	private static final long serialVersionUID = -4312019993178655333L;

	public BaiduPcsFileExistsException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
