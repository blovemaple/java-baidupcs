package blove.baidupcs.api.request;

import static blove.baidupcs.service.BaiduPcsService.*;

/**
 * 视频转码的格式参数。
 * 
 * @author blove
 */
public enum StreamingType {
	M3U8_320_240(STREAMING_TYPE_M3U8_320_240),
	M3U8_480_224(STREAMING_TYPE_M3U8_480_224),
	M3U8_480_360(STREAMING_TYPE_M3U8_480_360),
	M3U8_640_480(STREAMING_TYPE_M3U8_640_480),
	M3U8_854_480(STREAMING_TYPE_M3U8_854_480);
	
	private final String restParam;

	private StreamingType(String restParam) {
		this.restParam = restParam;
	}

	public String getRestParam() {
		return restParam;
	}

}
