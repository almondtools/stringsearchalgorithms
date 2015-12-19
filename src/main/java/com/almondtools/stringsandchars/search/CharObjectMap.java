package com.almondtools.stringsandchars.search;

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

	public T get(char value) {
		int i = h.hash(value);
		if (keys[i] == value) {
			return values[i];
		} else {
			return defaultValue;
		}
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public static class Builder<T> extends MinimalPerfectMapBuilder<Character, T> {

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
			super(new HashFunction(new int[]{0}, 0, 0), map, defaultValue);
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
