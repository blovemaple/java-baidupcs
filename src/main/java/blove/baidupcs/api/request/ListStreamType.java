package blove.baidupcs.api.request;

import blove.baidupcs.service.BaiduPcsService;

/**
 * 获取流式文件列表的类型参数。
 * 
 * @author blove
 */
public enum ListStreamType {
	/**
	 * 视频
	 */
	VIDEO(BaiduPcsService.LIST_STREAM_TYPE_VIDEO),
	/**
	 * 音频
	 */
	AUDIO(BaiduPcsService.LIST_STREAM_TYPE_AUDIO),
	/**
	 * 图片
	 */
	IMAGE(BaiduPcsService.LIST_STREAM_TYPE_IMAGE),
	/**
	 * 文档
	 */
	DOC(BaiduPcsService.LIST_STREAM_TYPE_DOC);

	private final String restParam;

	private ListStreamType(String restParam) {
		this.restParam = restParam;
	}

	public String getRestParam() {
		return restParam;
	}

}
