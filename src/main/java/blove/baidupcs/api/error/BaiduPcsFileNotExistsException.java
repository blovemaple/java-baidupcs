package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 文件不存在导致无法完成某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsFileNotExistsException extends FSRelatedException {
	private static final long serialVersionUID = -9072778967330381859L;

	public BaiduPcsFileNotExistsException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
