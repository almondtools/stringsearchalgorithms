package com.almondtools.stringsandchars.search;

import java.util.SortedMap;
import java.util.TreeMap;

public class CharObjectMap<T> {

	private char[] keys;
	private T[] values;
	private T defaultValue;

	public CharObjectMap(SortedMap<Character, T> map, T defaultValue) {
		this.keys = createKeys(map);
		this.values = createValues(map);
		this.defaultValue = defaultValue;
	}

	public static char[] createKeys(SortedMap<Character, ?> map) {
		char[] array = new char[map.size()];
		int i = 0;
		for (Character key : map.keySet()) {
			array[i] = key;
			i++;
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] createValues(SortedMap<Character, T> map) {
		T[] array = (T[]) new Object[map.size()];
		int i = 0;
		for (T value : map.values()) {
			array[i] = value;
			i++;
		}
		return array;
	}

	public T get(char i) {
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

	public static class Builder<T> {

		private T defaultValue;
		private SortedMap<Character, T> entries;

		public Builder(T defaultValue) {
			this.defaultValue = defaultValue;
			this.entries = new TreeMap<Character, T>();
		}

		public CharObjectMap<T> build() {
			return new CharObjectMap<>(entries, defaultValue);
		}

		public T get(char key) {
			T result = entries.get(key);
			if (result == null) {
				return defaultValue;
			} else {
				return result;
			}
		}

		public void put(char key, T value) {
			entries.put(key, value);
		}

	}

}
