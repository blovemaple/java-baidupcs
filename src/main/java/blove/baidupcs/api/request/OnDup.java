package blove.baidupcs.api.request;

import blove.baidupcs.service.BaiduPcsCService;

/**
 * 创建文件时文件已存在的处理方式。
 * 
 * @author blove
 */
public enum OnDup {
	/**
	 * 覆盖同名文件
	 */
	OVERWRITE(BaiduPcsCService.UPLOAD_ONDUP_OVERWRITE),
	/**
	 * 生成文件副本并进行重命名，命名规则为“文件名_日期.后缀”
	 */
	NEW_COPY(BaiduPcsCService.UPLOAD_ONDUP_NEWCOPY),
	/**
	 * 抛出异常
	 */
	EXCEPTION(null);
	private final String restParam;

	private OnDup(String restParam) {
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
