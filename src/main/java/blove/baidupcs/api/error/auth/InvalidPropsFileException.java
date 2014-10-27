package blove.baidupcs.api.error.auth;

/**
 * 配置文件不合法时抛出的异常
 * 
 * @author blove
 */
public class InvalidPropsFileException extends Exception {
	private static final long serialVersionUID = -4816916236554318390L;

	/**
	 * 新建一个实例。
	 * 
	 * @param message
	 *             错误信息
	 */
	public InvalidPropsFileException(String message) {
		super(message);
	}
}
