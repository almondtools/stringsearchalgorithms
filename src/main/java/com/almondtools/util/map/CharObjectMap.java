package com.almondtools.util.map;

import static com.almondtools.util.map.HashFunction.NULL;

import java.util.Map;

public class CharObjectMap<T> {

	private HashFunction h;
	private char[] keys;
	private T[] values;
	private T defaultValue;

	public CharObjectMap(HashFunction h, Map<Character, T> map, T defaultValue) {
		this.h = h;
		this.defaultValue = defaultValue;
		computeKeysAndValues(map);
	}

	@SuppressWarnings("unchecked")
	private void computeKeysAndValues(Map<Character, T> map) {
		int len = map.size();
		if (h == NULL) {
			keys = new char[len];
			Character[] objectkeys = map.keySet().toArray(new Character[0]);
			for (int i = 0; i < objectkeys.length; i++) {
				keys[i] = objectkeys[i];
			}
		} else if (len == 0) {
			keys = new char[1];
			values = (T[]) new Object[1];
			values[0] = defaultValue;
		} else {
			keys = new char[len];
			values = (T[]) new Object[len];
			for (Map.Entry<Character, T> entry : map.entrySet()) {
				char key = entry.getKey();
				T value = entry.getValue();

				int i = h.hash(key);

				keys[i] = key;
				values[i] = value;
			}
		}
	}

	public char[] keys() {
		return keys;
	}

	public T get(char value) {
		int i = h.hash(value);
		if (i >= keys.length) {
			return defaultValue;
		} else if (keys[i] == value) {
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
			char key = keys[0];
			T value = get(key);
			buffer.append(key).append(": ").append(value);
			
		}
		for (int i = 1; i < keys.length; i++) {
			char key = keys[i];
			T value = get(key);
			buffer.append(",\n").append(key).append(": ").append(value);
		}
		buffer.append("\n}");
		return buffer.toString();
	}

	public static class Builder<T> extends MinimalPerfectMapBuilder<Character, T, CharObjectMap<T>> {

		public Builder(T defaultValue) {
			super(defaultValue);
		}

		public CharObjectMap<T> perfectMinimal() {
			try {
				computeFunctions(100, 1.15);
				return new CharObjectMap<T>(getH(), getEntries(), getDefaultValue());
			} catch (HashBuildException e) {
				return new Fallback<T>(getEntries(), getDefaultValue());
			}
		}

	}

	private static class Fallback<T> extends CharObjectMap<T> {

		private Map<Character, T> map;

		public Fallback(Map<Character, T> map, T defaultValue) {
			super(NULL, map, defaultValue);
			this.map = map;
		}
		
		@Override
		public T get(char key) {
			T value = map.get(key);
			if (value == null) {
				return getDefaultValue();
			} else {
				return value;
			}
		}

	}

}
