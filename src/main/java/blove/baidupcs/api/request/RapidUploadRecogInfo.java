package blove.baidupcs.api.request;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * 秒传文件所需的文件识别信息参数。
 * 
 * @author blove
 */
public class RapidUploadRecogInfo {
	public static final long SLICE_SIZE = 1024 * 256;

	private long contentLength;
	private String contentMD5;
	private String sliceMD5;
	private String contentCRC32;

	/**
	 * 从指定输入流读取数据生成实例。以输入流的实际长度为总大小。
	 * 
	 * @param in
	 *             输入流
	 * @return 实例。如果不符合秒传条件则返回null。
	 * @throws IOException
	 */
	public static RapidUploadRecogInfo fromInputStream(InputStream in) throws IOException {
		return fromInputStream(in, -1);
	}

	/**
	 * 从指定输入流读取数据生成实例。
	 * 
	 * @param in
	 *             输入流
	 * @param size
	 *             总大小。如果指定为负数，则以输入流的实际长度为总大小。
	 * @return 实例。如果不符合秒传条件则返回null。
	 * @throws IOException
	 */
	public static RapidUploadRecogInfo fromInputStream(InputStream in, long size) throws IOException {
		if (size < 0)
			size = Long.MAX_VALUE;
		if (size < SLICE_SIZE)
			return null;

		try {
			RapidUploadRecogInfo instance = new RapidUploadRecogInfo();

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			MessageDigest sliceMD5 = MessageDigest.getInstance("MD5");
			CRC32 crc32 = new CRC32();
			byte[] buf = new byte[8192];
			int readOnce;
			long readCount = 0;

			// 读取一直到SLICE_SIZE或者流结束（不用考虑size，因为保证了size>=SLICE_SIZE）
			while ((readOnce = in.read(buf, 0,
					Math.min(buf.length, (int) Math.min(Integer.MAX_VALUE, SLICE_SIZE - readCount)))) >= 0) {
				md5.update(buf, 0, readOnce);
				sliceMD5.update(buf, 0, readOnce);
				crc32.update(buf, 0, readOnce);
				readCount += readOnce;
				if (readCount == SLICE_SIZE)
					break;
			}

			// 如果读到了SLICE_SIZE则计算sliceMD5，否则判断为流不足SLICE_SIZE，返回null
			if (readCount < SLICE_SIZE)
				return null;
			instance.setSliceMD5(hexString(sliceMD5.digest()));

			// 读取一直到size或者流结束
			while ((readOnce = in.read(buf, 0,
					Math.min(buf.length, (int) Math.min(Integer.MAX_VALUE, size - readCount)))) >= 0) {
				md5.update(buf, 0, readOnce);
				crc32.update(buf, 0, readOnce);
				readCount += readOnce;
				if (readCount == size)
					break;
			}

			// 计算contentMD5和contentCRC32
			instance.setContentMD5(hexString(md5.digest()));
			instance.setContentCRC32(String.valueOf(crc32.getValue()));

			instance.setContentLength(readCount);

			return instance;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String hexString(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			if ((0xff & bytes[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & bytes[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		return hexString.toString();
	}

	/**
	 * 文件长度。
	 * 
	 * @return
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * 设置文件长度参数。
	 * 
	 * @param contentLength
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * 文件MD5。
	 * 
	 * @return
	 */
	public String getContentMD5() {
		return contentMD5;
	}

	/**
	 * 设置文件MD5参数。
	 * 
	 * @param contentMD5
	 */
	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	/**
	 * 校验段MD5。校验段为文件的前256KB。
	 * 
	 * @return
	 */
	public String getSliceMD5() {
		return sliceMD5;
	}

	/**
	 * 设置文件校验段MD5参数。校验段为文件的前256KB。
	 * 
	 * @param sliceMD5
	 */
	public void setSliceMD5(String sliceMD5) {
		this.sliceMD5 = sliceMD5;
	}

	/**
	 * 文件CRC32。
	 * 
	 * @return
	 */
	public String getContentCRC32() {
		return contentCRC32;
	}

	/**
	 * 设置文件CRC32参数。
	 * 
	 * @param contentCRC32
	 */
	public void setContentCRC32(String contentCRC32) {
		this.contentCRC32 = contentCRC32;
	}

	/**
	 * 校验参数是否合法。如果不合法则抛出IllegalArgumentException。
	 * 
	 * @throws IllegalArgumentException
	 *              参数不合法
	 */
	public void validate() {
		// TODO
	}

}
