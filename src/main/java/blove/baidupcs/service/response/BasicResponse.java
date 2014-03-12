package blove.baidupcs.service.response;

public class BasicResponse {
	private String request_id;

	/**
	 * 请求ID，也就是服务器用于追踪错误的的日志ID。
	 * 
	 * @return
	 */
	public String getRequest_id() {
		return request_id;
	}

	@Override
	public String toString() {
		return "BasicResponse [\n\trequest_id=" + request_id + "\n]";
	}

}
