package blove.baidupcs.fs.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 值为弱连接的哈希映射。<br>
 * 注意：当value对象被回收时，key对象并不会从此映射中移除，但取其值时将返回null。
 * 
 * @author blove
 * 
 * @param <K>
 * @param <V>
 */
public class WeakValueHashMap<K, V> implements Map<K, V> {

	private final HashMap<K, WeakReference<V>> hashMap;

	public WeakValueHashMap() {
		hashMap = new HashMap<>();
	}

	public WeakValueHashMap(int initialCapacity, float loadFactor) {
		hashMap = new HashMap<>(initialCapacity, loadFactor);
	}

	public WeakValueHashMap(int initialCapacity) {
		hashMap = new HashMap<>(initialCapacity);
	}

	public WeakValueHashMap(Map<? extends K, ? extends V> m) {
		this(m.size());
		putAll(m);
	}

	@Override
	public boolean equals(Object o) {
		return hashMap.equals(o);
	}

	@Override
	public int size() {
		return hashMap.size();
	}

	@Override
	public boolean isEmpty() {
		return hashMap.isEmpty();
	}

	@Override
	public V get(Object key) {
		WeakReference<V> ref = hashMap.get(key);
		if (ref == null) {
			remove(key);
			return null;
		} else
			return ref.get();
	}

	@Override
	public boolean containsKey(Object key) {
		return hashMap.containsKey(key);
	}

	@Override
	public int hashCode() {
		return hashMap.hashCode();
	}

	public WeakReference<V> put(K key, WeakReference<V> value) {
		return hashMap.put(key, value);
	}

	@Override
	public String toString() {
		return hashMap.toString();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), new WeakReference<V>(entry.getValue()));
		}
	}

	@Override
	public V remove(Object key) {
		WeakReference<V> ref = hashMap.get(key);
		if (ref == null) {
			return null;
		} else {
			hashMap.remove(key);
			return ref.get();
		}
	}

	@Override
	public void clear() {
		hashMap.clear();
	}

	@Override
	public boolean containsValue(Object value) {
		return hashMap.containsValue(value);
	}

	@Override
	public Object clone() {
		return hashMap.clone();
	}

	@Override
	public Set<K> keySet() {
		return hashMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return new Collection<V>() {

			private Collection<WeakReference<V>> oriValues = hashMap.values();

			@Override
			public int size() {
				return oriValues.size();
			}

			@Override
			public boolean isEmpty() {
				return oriValues.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				for (V value : this) {
					boolean equal;
					if (o == null)
						equal = (value == null);
					else
						equal = o.equals(value);
					if (equal)
						return true;
				}
				return false;
			}

			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					private Iterator<WeakReference<V>> oriItr = oriValues
							.iterator();

					@Override
					public boolean hasNext() {
						return oriItr.hasNext();
					}

					@Override
					public V next() {
						return oriItr.next().get();
					}

					@Override
					public void remove() {
						oriItr.remove();
					}

				};
			}

			@Override
			public Object[] toArray() {
				List<V> arrayList = new ArrayList<>(size());
				for (V value : this)
					arrayList.add(value);
				return arrayList.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				List<V> arrayList = new ArrayList<>(size());
				for (V value : this)
					arrayList.add(value);
				return arrayList.toArray(a);
			}

			@Override
			public boolean add(V e) {
				return oriValues.add(new WeakReference<>(e));
			}

			@Override
			public boolean remove(Object o) {
				return oriValues.remove(new WeakReference<>(o));
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object o : c)
					if (!contains(o))
						return false;
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends V> c) {
				boolean changed = false;
				for (V o : c)
					if (add(o))
						changed = true;
				return changed;
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				for (Object o : c)
					if (remove(o))
						changed = true;
				return changed;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				Iterator<V> itr = iterator();
				while (itr.hasNext()) {
					V value = itr.next();
					if (!c.contains(value)) {
						itr.remove();
						changed = true;
					}
				}
				return changed;
			}

			@Override
			public void clear() {
				oriValues.clear();
			}

		};
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new Set<Entry<K, V>>() {
			private Set<Entry<K, WeakReference<V>>> oriSet = hashMap.entrySet();

			@Override
			public int size() {
				return oriSet.size();
			}

			@Override
			public boolean isEmpty() {
				return oriSet.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Entry))
					return false;
				@SuppressWarnings("unchecked")
				final Entry<K, V> entry = (Entry<K, V>) o;
				return oriSet.contains(weakEntry(entry));
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K, V>>() {
					private Iterator<Entry<K, WeakReference<V>>> oriItr = oriSet
							.iterator();

					@Override
					public boolean hasNext() {
						return oriItr.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						return unweakEntry(oriItr.next());
					}

					@Override
					public void remove() {
						oriItr.remove();
					}

				};
			}

			@Override
			public Object[] toArray() {
				List<Entry<K, V>> arrayList = new ArrayList<>(size());
				for (Entry<K, V> value : this)
					arrayList.add(value);
				return arrayList.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				List<Entry<K, V>> arrayList = new ArrayList<>(size());
				for (Entry<K, V> value : this)
					arrayList.add(value);
				return arrayList.toArray(a);
			}

			@Override
			public boolean add(final Entry<K, V> e) {
				return oriSet.add(weakEntry(e));
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Entry))
					return false;
				@SuppressWarnings("unchecked")
				final Entry<K, V> entry = (Entry<K, V>) o;
				return oriSet.remove(weakEntry(entry));
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object o : c)
					if (!contains(o))
						return false;
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends Entry<K, V>> c) {
				boolean changed = false;
				for (Entry<K, V> entry : c) {
					if (add(entry))
						changed = true;
				}
				return changed;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				Iterator<Entry<K, V>> itr = iterator();
				while (itr.hasNext()) {
					Entry<K, V> value = itr.next();
					if (!c.contains(value)) {
						itr.remove();
						changed = true;
					}
				}
				return changed;
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				for (Object entry : c) {
					if (remove(entry))
						changed = true;
				}
				return changed;
			}

			@Override
			public void clear() {
				oriSet.clear();
			}

			private Entry<K, V> unweakEntry(
					final Entry<K, WeakReference<V>> entry) {
				return new Entry<K, V>() {

					@Override
					public K getKey() {
						return entry.getKey();
					}

					@Override
					public V getValue() {
						return entry.getValue().get();
					}

					@Override
					public V setValue(V value) {
						WeakReference<V> lastValue = entry
								.setValue(new WeakReference<V>(value));
						if (lastValue == null)
							return null;
						else
							return lastValue.get();
					}

				};
			}

			private Entry<K, WeakReference<V>> weakEntry(final Entry<K, V> entry) {
				return new Entry<K, WeakReference<V>>() {

					private WeakReference<V> value = new WeakReference<V>(
							entry.getValue());

					@Override
					public K getKey() {
						return entry.getKey();
					}

					@Override
					public WeakReference<V> getValue() {
						return value;
					}

					@Override
					public WeakReference<V> setValue(WeakReference<V> value) {
						WeakReference<V> lastValue = this.value;
						entry.setValue(value.get());
						this.value = value;
						return lastValue;
					}
				};
			}

		};
	}

	@Override
	public V put(K key, V value) {
		V lastValue = get(key);
		hashMap.put(key, new WeakReference<V>(value));
		return lastValue;
	}

}
