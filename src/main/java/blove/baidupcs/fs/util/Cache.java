package blove.baidupcs.fs.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 以指定类型对象为键，存放指定类型对象的缓存，可指定超时时间。
 * 
 * @param <K>
 *             键类型
 * @param <V>
 *             值类型
 * @author blove
 */
public class Cache<K, V> {
	private final int expireTime;

	private final Map<K, Long> keyTimes = new HashMap<>();
	private final Map<K, V> keyValues = new HashMap<>();

	/**
	 * 新建一个实例。
	 * 
	 * @param expireTime
	 *             缓存超时时间。单位：毫秒。
	 */
	public Cache(int expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * 将指定对象以指定键放入缓存。如果键已存在，则会覆盖，并重新计时。
	 * 
	 * @param key
	 *             键
	 * @param value
	 *             值
	 */
	public void put(K key, V value) {
		keyValues.put(key, value);
		keyTimes.put(key, System.currentTimeMillis());
	}

	/**
	 * 获取指定键对应的值。
	 * 
	 * @param key
	 *             键
	 * @return 值。如果没有或已超时则返回null。
	 */
	public V get(K key) {
		Long time = keyTimes.get(key);
		if (time == null)
			return null;
		if (System.currentTimeMillis() - time > expireTime) {
			keyTimes.remove(key);
			keyValues.remove(key);
			return null;
		}

		V value = keyValues.get(key);
		if (value == null) {
			keyTimes.remove(key);
			return null;
		}

		return value;
	}

	/**
	 * 删除指定键对应的值。
	 * 
	 * @param key
	 *             键
	 */
	public void remove(K key) {
		Long time = keyTimes.remove(key);
		if (time != null)
			keyValues.remove(key);
	}
}
