package blove.baidupcs.util;

/**
 * 用于{@link ProgressInputStream}的进度监视接口。
 * 
 * @author blove
 */
public interface ProgressObserver {
	/**
	 * 进度更新时调用。
	 * 
	 * @param stream
	 *             流
	 * @param rate
	 *             进度。[0,1]，精确到两位小数。
	 */
	void update(ProgressInputStream stream, double rate);
}
