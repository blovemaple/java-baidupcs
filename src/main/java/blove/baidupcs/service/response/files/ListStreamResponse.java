package blove.baidupcs.service.response.files;

public class ListStreamResponse extends ListOrSearchResponse {
	private int total;
	private int start;
	private int limit;

	/**
	 * 文件总数。
	 * 
	 * @return
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * 起始数。
	 * 
	 * @return
	 */
	public int getStart() {
		return start;
	}

	/**
	 * 获取数。
	 * 
	 * @return
	 */
	public int getLimit() {
		return limit;
	}

}
