package blove.baidupcs.api.request;

import blove.baidupcs.service.BaiduPcsService;

/**
 * 文件或目录列表的排序顺序。
 * 
 * @author blove
 */
public enum Order {
	/**
	 * 升序
	 */
	ASC(BaiduPcsService.LIST_ORDER_ASC),
	/**
	 * 降序
	 */
	DESC(BaiduPcsService.LIST_ORDER_DESC),
	/**
	 * 默认（降序）
	 */
	DEFAULT(null);

	private final String restParam;

	private Order(String restParam) {
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
