package com.almondtools.util.map;

import java.util.Map;

public class CharIntMap {

	private HashFunction h;
	private char[] keys;
	private int[] values;
	private int defaultValue;

	public CharIntMap(HashFunction h, Map<Character, Integer> map, Integer defaultValue) {
		this.h = h;
		this.defaultValue = defaultValue;
		computeKeysAndValues(map);
	}

	private void computeKeysAndValues(Map<Character, Integer> map) {
		int len = map.size();
		if (len == 0) {
			keys = new char[1];
			values = new int[1];
			values[0] = defaultValue;
		} else {
			keys = new char[len];
			values = new int[len];
			for (Map.Entry<Character, Integer> entry : map.entrySet()) {
				char key = entry.getKey();
				int value = entry.getValue();

				int i = h.hash(key);

				keys[i] = key;
				values[i] = value;
			}
		}
	}

	public int get(char value) {
		int i = h.hash(value);
		if (keys[i] == value) {
			return values[i];
		} else {
			return defaultValue;
		}
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public static class Builder extends MinimalPerfectMapBuilder<Character, Integer> {

		public Builder(Integer defaultValue) {
			super(defaultValue);
		}

		public CharIntMap perfectMinimal() {
			try {
				computeFunctions(100, 1.15);
				return new CharIntMap(getH(), getEntries(), getDefaultValue());
			} catch (HashBuildException e) {
				return new Fallback(getEntries(), getDefaultValue());
			}
		}

	}

	private static class Fallback extends CharIntMap {

		private Map<Character, Integer> map;

		public Fallback(Map<Character, Integer> map, Integer defaultValue) {
			super(new HashFunction(new int[] { 0 }, 0, 0), map, defaultValue);
			this.map = map;
		}

		@Override
		public int get(char key) {
			Integer value = map.get(key);
			if (value == null) {
				return getDefaultValue();
			} else {
				return value;
			}
		}

	}

}
