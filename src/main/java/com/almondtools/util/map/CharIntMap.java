package com.almondtools.util.map;

public class CharIntMap extends TuneableMap {

	private static final char NULL_KEY = 0;

	private float loadFactor;
	private int mask;
	private int expandAt;
	private int size;

	private char[] keys;
	private int[] values;
	private int defaultValue;
	private int nullValue;

	public CharIntMap(int defaultValue) {
		this(DEFAULT_SIZE, DEFAULT_LOAD, defaultValue);
	}

	public CharIntMap(int initialSize, float loadFactor, int defaultValue) {
		this.loadFactor = loadFactor;
		this.mask = mask(initialSize, loadFactor);
		this.expandAt = initialSize;
		this.size = 0;
		this.keys = new char[mask + 1];
		this.values = new int[mask + 1];
		this.defaultValue = defaultValue;
		this.nullValue = defaultValue;
	}

	public char[] keys() {
		int size = this.size;
		if (nullValue != defaultValue) {
			size++;
		}
		char[] keys = new char[size];
		int pos = 0;
		for (char c : this.keys) {
			if (c != NULL_KEY) {
				keys[pos] = c;
				pos++;
			}
		}
		if (nullValue != defaultValue && pos < keys.length) {
			keys[pos] = NULL_KEY;
		}
		return keys;
	}

	public CharIntMap add(char key, int value) {
		put(key, value);
		return this;
	}

	public void put(char key, int value) {
		if (key == NULL_KEY) {
			nullValue = value;
			return;
		}
		int slot = hash(key) & mask;
		while (keys[slot] != key && keys[slot] != NULL_KEY) {
			slot = (slot + 1) & mask;
		}
		if (keys[slot] == NULL_KEY) {
			size++;
		}
		keys[slot] = key;
		values[slot] = value;
		if (size > expandAt) {
			expand(size * 2);
		}
	}

	public int get(char key) {
		if (key == NULL_KEY) {
			return nullValue;
		}
		int slot = hash(key) & mask;
		while (keys[slot] != key && keys[slot] != NULL_KEY) {
			slot = (slot + 1) & mask;
		}
		if (keys[slot] == NULL_KEY) {
			return defaultValue;
		} else {
			return values[slot];
		}
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	private void expand(int size) {
		int mask = mask(size, this.loadFactor);
		
		char[] oldkeys = this.keys;
		int[] oldvalues = this.values;
		
		char[] keys = new char[mask + 1];
		int[] values = new int[mask + 1];

		int[] delayed = new int[this.size];
		int pos = 0;
		
		for (int i = 0; i < oldkeys.length; i++) {
			char key = oldkeys[i];
			if (key != NULL_KEY) {
				int value = oldvalues[i];
				int slot = hash(key) & mask;
				if (keys[slot] == NULL_KEY) {
					keys[slot] = key;
					values[slot] = value;
				} else {
					delayed[pos] = i;
					pos++;
				}
			}
		}
		for (int i = 0; i <= pos; i++) {
			int j = delayed[i];
			char key = oldkeys[j];
			int value = oldvalues[j];
			int slot = hash(key) & mask;
			while (keys[slot] != key && keys[slot] != NULL_KEY) {
				slot = (slot + 1) & mask;
			}
			keys[slot] = key;
			values[slot] = value;
		}

		this.expandAt = size;
		this.mask = mask;
		this.keys = keys;
		this.values = values;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("{\n");
		if (keys.length > 0) {
			char key = keys[0];
			int value = values[0];
			buffer.append(key).append(": ").append(value);

		}
		for (int i = 1; i < keys.length; i++) {
			char key = keys[i];
			int value = values[0];
			buffer.append(",\n").append(key).append(": ").append(value);
		}
		buffer.append("\n}");
		return buffer.toString();
	}
}
