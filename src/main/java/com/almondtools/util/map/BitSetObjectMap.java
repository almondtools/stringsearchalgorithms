package com.almondtools.util.map;

import java.util.BitSet;
import java.util.Map;

public class BitSetObjectMap<T> {

	private HashFunction h;
	private BitSet[] keys;
	private T[] values;
	private T defaultValue;

	public BitSetObjectMap(HashFunction h, Map<BitSet, T> map, T defaultValue) {
		this.h = h;
		this.defaultValue = defaultValue;
		computeKeysAndValues(map);
	}

	@SuppressWarnings("unchecked")
	private void computeKeysAndValues(Map<BitSet, T> map) {
		int len = map.size();
		if (len == 0) {
			keys = new BitSet[1];
			keys[0] = new BitSet(0);
			values = (T[]) new Object[1];
			values[0] = defaultValue;
		} else {
			keys = new BitSet[len];
			values = (T[]) new Object[len];
			for (Map.Entry<BitSet, T> entry : map.entrySet()) {
				BitSet key = entry.getKey();
				T value = entry.getValue();

				int i = h.hash(key.toLongArray());

				keys[i] = key;
				values[i] = value;
			}
		}
	}
	
	public BitSet[] keys() {
		return keys;
	}

	public T get(BitSet value) {
		int i = h.hash(value.toLongArray());
		if (i >= keys.length) {
			return defaultValue;
		} else if (keys[i].equals(value)) {
			return values[i];
		} else {
			return defaultValue;
		}
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public static class Builder<T> extends MinimalPerfectMapBuilder<BitSet, T> implements KeySerializer<BitSet> {

		public Builder(T defaultValue) {
			super(defaultValue);
			withKeySerializer(this);
		}

		@Override
		public long[] toLongArray(BitSet object) {
			return object.toLongArray();
		}

		public BitSetObjectMap<T> perfectMinimal() {
			try {
				computeFunctions(100, 1.15);
				return new BitSetObjectMap<T>(getH(), getEntries(), getDefaultValue());
			} catch (HashBuildException e) {
				return new Fallback<T>(getEntries(), getDefaultValue());
			}
		}

	}

	private static class Fallback<T> extends BitSetObjectMap<T> {

		private Map<BitSet, T> map;

		public Fallback(Map<BitSet, T> map, T defaultValue) {
			super(new HashFunction(new int[] { 0 }, 0, 0), map, defaultValue);
			this.map = map;
		}

		@Override
		public T get(BitSet key) {
			T value = map.get(key);
			if (value == null) {
				return getDefaultValue();
			} else {
				return value;
			}
		}

	}

}
