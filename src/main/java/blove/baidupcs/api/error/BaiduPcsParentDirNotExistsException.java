package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 父目录不存在导致不能进行某项操作时，抛出此异常。
 * 
 * @author blove
 */
public class BaiduPcsParentDirNotExistsException extends FSRelatedException {
	private static final long serialVersionUID = 4992935671310254456L;

	public BaiduPcsParentDirNotExistsException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
