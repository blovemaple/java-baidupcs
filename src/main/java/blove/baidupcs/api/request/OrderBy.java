package blove.baidupcs.api.request;

import blove.baidupcs.service.BaiduPcsService;

/**
 * 文件或目录列表的排序字段。
 * 
 * @author blove
 */
public enum OrderBy {
	/**
	 * 修改时间
	 */
	TIME(BaiduPcsService.LIST_BY_TIME),
	/**
	 * 文件名
	 */
	NAME(BaiduPcsService.LIST_BY_NAME),
	/**
	 * 大小（注意目录无大小）
	 */
	SIZE(BaiduPcsService.LIST_BY_SIZE),
	/**
	 * 默认（文件类型）
	 */
	DEFAULT(null);

	private final String restParam;

	private OrderBy(String restParam) {
		this.restParam = restParam;
	}

	/**
	 * 返回REST参数值。
	 * 
	 * @return
	 */
	public String getRestParam() {
		return restParam;
	}
}
