package com.almondtools.stringsandchars.search;

import java.util.SortedMap;
import java.util.TreeMap;

public class CharLongMap {

	private char[] keys;
	private long[] values;
	private long defaultValue;

	public CharLongMap(SortedMap<Character, Long> map, long defaultValue) {
		this.keys = createKeys(map);
		this.values = createValues(map);
		this.defaultValue = defaultValue;
	}

	public static char[] createKeys(SortedMap<Character, Long> map) {
		char[] array = new char[map.size()];
		int i = 0;
		for (Character key : map.keySet()) {
			array[i] = key;
			i++;
		}
		return array;
	}

	public static long[] createValues(SortedMap<Character, Long> map) {
		long[] array = new long[map.size()];
		int i = 0;
		for (Long value : map.values()) {
			array[i] = value;
			i++;
		}
		return array;
	}

	public long get(char i) {
		int leftI = 0;
		int rightI = keys.length - 1;
		char leftKey = keys[leftI];
		char rightKey = keys[rightI];
		if (i < leftKey || i > rightKey) {
			return defaultValue;
		} else if (leftKey == i) {
			return values[leftI];
		} else if (rightKey == i) {
			return values[rightI];
		}
		while (true) {
			if (leftKey == rightKey) {
				return defaultValue;
			}
			int midI = (leftI + rightI) >> 1;
			char midKey = keys[midI];
			if (midKey == i) {
				return values[midI];
			} else if (midKey < i && midKey > leftKey) {
				leftI = midI;
				leftKey = midKey;
			} else if (midKey > i && midKey < rightKey) {
				rightI = midI;
				rightKey = midKey;
			} else {
				return defaultValue;
			}
		}
	}

	public static class Builder {

		private int defaultValue;
		private SortedMap<Character, Long> entries;

		public Builder(int defaultValue) {
			this.defaultValue = defaultValue;
			this.entries = new TreeMap<Character, Long>();
		}

		public CharLongMap build() {
			return new CharLongMap(entries, defaultValue);
		}

		public long get(char key) {
			Long result = entries.get(key);
			if (result == null) {
				return defaultValue;
			} else {
				return result;
			}
		}

		public void put(char key, long value) {
			entries.put(key, value);
		}

	}

}
