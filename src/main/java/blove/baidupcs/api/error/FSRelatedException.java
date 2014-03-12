package blove.baidupcs.api.error;

import retrofit.client.Response;

/**
 * 文件系统状态致使某项操作无法完成时，抛出此异常。如文件不存在、目录不存在等。
 * 
 * @author blove
 */
public class FSRelatedException extends BaiduPcsException {
	private static final long serialVersionUID = -7224809525683547421L;

	public FSRelatedException(ErrorResponse errorResponse, Response httpResponse) {
		super(errorResponse, httpResponse);
	}

}
