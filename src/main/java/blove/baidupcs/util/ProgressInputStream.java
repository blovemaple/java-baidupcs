package blove.baidupcs.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 可以监视进度的（过滤）输入流。<br>
 * 注意：此过滤流不支持mark/reset操作。
 * 
 * @author blove
 */
public class ProgressInputStream extends FilterInputStream {
	private long length;
	private long readLength = 0;

	private double lastRate = -1;

	private List<ProgressObserver> observers = Collections
			.synchronizedList(new LinkedList<ProgressObserver>());

	/**
	 * 新建一个实例。总长度按照已读长度加上{@link #available()}的大小计算。
	 * 
	 * @param in
	 *            底层输入流
	 */
	public ProgressInputStream(InputStream in) {
		this(in, -1);
	}

	/**
	 * 新建一个实例。
	 * 
	 * @param in
	 *            底层输入流
	 * @param length
	 *            总长度。如果指定负数，则按照已读长度加上{@link #available()}的大小计算。
	 */
	public ProgressInputStream(InputStream in, long length) {
		super(in);
		this.length = length;
	}

	/**
	 * 添加一个监视器。
	 * 
	 * @param observer
	 *            监视器
	 */
	public void addObserver(ProgressObserver observer) {
		observers.add(observer);
	}

	/**
	 * 移除一个监视器。
	 * 
	 * @param observer
	 *            监视器
	 */
	public void removeObserver(ProgressObserver observer) {
		observers.remove(observer);
	}

	@Override
	public int read() throws IOException {
		int ret = in.read();
		if (ret >= 0)
			readed(1);
		return ret;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int ret = in.read(b);
		if (ret > 0)
			readed(ret);
		return ret;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = in.read(b, off, len);
		if (ret > 0)
			readed(ret);
		return ret;
	}

	@Override
	public long skip(long n) throws IOException {
		long ret = in.skip(n);
		if (n > 0)
			readed(n);
		return ret;
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException(
				"Mark is unsupported by ProgressInputStream.");
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException(
				"Reset is unsupported by ProgressInputStream.");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	private static DecimalFormat rateFormat = new DecimalFormat("#.00");

	/**
	 * 读取后调用此方法，计数并通知。
	 * 
	 * @param byteNum
	 *            读取的字节数
	 * @throws IOException
	 */
	private synchronized void readed(long byteNum) throws IOException {
		readLength += byteNum;
		long length = this.length >= 0 ? this.length : readLength + available();

		double rate = Double.parseDouble(rateFormat.format(readLength
				/ (double) length));
		if (rate != lastRate) {
			synchronized (observers) {
				for (ProgressObserver observer : observers)
					observer.update(this, rate);
				lastRate = rate;
			}
		}
	}

}
