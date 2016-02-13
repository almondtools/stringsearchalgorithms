package com.almondtools.util.map;

import static com.almondtools.util.map.HashFunction.NULL;

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
		if (h == NULL) {
			keys = map.keySet().toArray(new BitSet[0]);
		} else if (len == 0) {
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

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("{\n");
		if (keys.length > 0) {
			BitSet key = keys[0];
			T value = get(key);
			buffer.append(toString(key)).append(": ").append(value);
			
		}
		for (int i = 1; i < keys.length; i++) {
			BitSet key = keys[i];
			T value = get(key);
			buffer.append(",\n").append(toString(key)).append(": ").append(value);
		}
		buffer.append("\n}");
		return buffer.toString();
	}

	private String toString(BitSet bits) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < bits.size(); i++) {
			if (i / 4 > 0 && i % 4 == 0) {
				buffer.append(' ');
			}
			if (bits.get(i)) {
				buffer.append(1);
			} else {
				buffer.append(0);
			}
		}
		return buffer.toString();
	}

	public static class Builder<T> extends MinimalPerfectMapBuilder<BitSet, T, BitSetObjectMap<T>> implements KeySerializer<BitSet> {

		public Builder(T defaultValue) {
			super(defaultValue);
			withKeySerializer(this);
		}

		@Override
		public long[] toLongArray(BitSet object) {
			return object.toLongArray();
		}

		@Override
		public BitSetObjectMap<T> perfectMinimal() {
			try {
				computeFunctions(100, 1.55);
				return new BitSetObjectMap<T>(getH(), getEntries(), getDefaultValue());
			} catch (HashBuildException e) {
				return new Fallback<T>(getEntries(), getDefaultValue());
			}
		}

	}

	private static class Fallback<T> extends BitSetObjectMap<T> {

		private Map<BitSet, T> map;

		public Fallback(Map<BitSet, T> map, T defaultValue) {
			super(NULL, map, defaultValue);
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
