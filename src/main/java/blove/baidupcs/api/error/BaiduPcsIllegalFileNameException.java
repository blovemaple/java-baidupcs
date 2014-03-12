package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 文件名不合法导致不能完成某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsIllegalFileNameException extends FSRelatedException {
	private static final long serialVersionUID = -6952520469836590283L;

	public BaiduPcsIllegalFileNameException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
