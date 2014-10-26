package blove.baidupcs.service.response.auth;

public class DeviceCodeResponse {
	private String device_code;
	private String user_code;
	private String verification_url;
	private String qrcode_url;
	private int expires_in;
	private int interval;

	/**
	 * Device Code，设备需要保存，供下一步使用。
	 * 
	 * @return
	 */
	public String getDevice_code() {
		return device_code;
	}

	/**
	 * User Code，设备需要展示给用户。
	 * 
	 * @return
	 */
	public String getUser_code() {
		return user_code;
	}

	/**
	 * 用户填写User Code并进行授权的url，设备需要展示给用户。
	 * 
	 * @return
	 */
	public String getVerification_url() {
		return verification_url;
	}

	/**
	 * 用于二维码登陆的Qr Code图片url，用户用智能终端扫描该二维码之后，可直接进入步骤2的登陆授权页面。
	 * 
	 * @return
	 */
	public String getQrcode_url() {
		return qrcode_url;
	}

	/**
	 * Device Code/ Use Code的过期时间，单位为秒。
	 * 
	 * @return
	 */
	public int getExpires_in() {
		return expires_in;
	}

	/**
	 * 设备尝试获取Access Token的时间间隔，单位为秒，设备下一步会使用。
	 * 
	 * @return
	 */
	public int getInterval() {
		return interval;
	}
}
