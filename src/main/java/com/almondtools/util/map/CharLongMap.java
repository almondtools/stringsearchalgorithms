package com.almondtools.util.map;

import java.util.Map;

public class CharLongMap {

	private HashFunction h;
	private char[] keys;
	private long[] values;
	private long defaultValue;

	public CharLongMap(HashFunction h, Map<Character, Long> map, Long defaultValue) {
		this.h = h;
		this.defaultValue = defaultValue;
		computeKeysAndValues(map);
	}

	private void computeKeysAndValues(Map<Character, Long> map) {
		int len = map.size();
		if (len == 0) {
			keys = new char[1];
			values = new long[1];
			values[0] = defaultValue;
		} else {
			keys = new char[len];
			values = new long[len];
			for (Map.Entry<Character, Long> entry : map.entrySet()) {
				char key = entry.getKey();
				long value = entry.getValue();

				int i = h.hash(key);

				keys[i] = key;
				values[i] = value;
			}
		}
	}

	public char[] keys() {
		return keys;
	}

	public long get(char value) {
		int i = h.hash(value);
		if (i >= keys.length) {
			return defaultValue;
		} else if (keys[i] == value) {
			return values[i];
		} else {
			return defaultValue;
		}
	}

	public long getDefaultValue() {
		return defaultValue;
	}

	public static class Builder extends MinimalPerfectMapBuilder<Character, Long> {

		public Builder(Long defaultValue) {
			super(defaultValue);
		}

		public CharLongMap perfectMinimal() {
			try {
				computeFunctions(100, 1.15);
				return new CharLongMap(getH(), getEntries(), getDefaultValue());
			} catch (HashBuildException e) {
				return new Fallback(getEntries(), getDefaultValue());
			}
		}

	}

	private static class Fallback extends CharLongMap {

		private Map<Character, Long> map;

		public Fallback(Map<Character, Long> map, Long defaultValue) {
			super(new HashFunction(new int[] { 0 }, 0, 0), map, defaultValue);
			this.map = map;
		}

		@Override
		public long get(char key) {
			Long value = map.get(key);
			if (value == null) {
				return getDefaultValue();
			} else {
				return value;
			}
		}

	}
}
