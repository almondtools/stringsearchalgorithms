package com.almondtools.util.map;

import java.util.BitSet;

public class BitSetObjectMap<T> extends TuneableMap {

	private static final BitSet NULL_KEY = null;

	private float loadFactor;
	private int mask;
	private int expandAt;
	private int size;

	private BitSet[] keys;
	private T[] values;
	private T defaultValue;
	private T nullValue;

	public BitSetObjectMap(T defaultValue) {
		this(DEFAULT_SIZE, DEFAULT_LOAD, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public BitSetObjectMap(int initialSize, float loadFactor, T defaultValue) {
		this.loadFactor = loadFactor;
		this.mask = mask(initialSize, loadFactor);
		this.expandAt = initialSize;
		this.size = 0;
		this.keys = new BitSet[mask + 1];
		this.values = (T[]) new Object[mask + 1];
		this.defaultValue = defaultValue;
		this.nullValue = defaultValue;
	}

	public BitSet[] keys() {
		int size = this.size;
		if (nullValue != defaultValue) {
			size++;
		}
		BitSet[] keys = new BitSet[size];
		int pos = 0;
		for (BitSet c : this.keys) {
			if (c != NULL_KEY && c != null) {
				keys[pos] = c;
				pos++;
			}
		}
		if (nullValue != defaultValue && pos < keys.length) {
			keys[pos] = NULL_KEY;
		}
		return keys;
	}

	public BitSetObjectMap<T> add(BitSet key, T value) {
		put(key, value);
		return this;
	}

	public void put(BitSet key, T value) {
		if (key == NULL_KEY) {
			nullValue = value;
			return;
		}
		int slot = hash(key.hashCode()) & mask;
		while (keys[slot] != NULL_KEY && keys[slot] != null && !keys[slot].equals(key)) {
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

	public T get(BitSet key) {
		if (key == NULL_KEY) {
			return nullValue;
		}
		int slot = hash(key.hashCode()) & mask;
		while (keys[slot] != NULL_KEY && keys[slot] != null && !keys[slot].equals(key)) {
			slot = (slot + 1) & mask;
		}
		if (keys[slot] == NULL_KEY) {
			return defaultValue;
		} else {
			return values[slot];
		}
	}

	public T getDefaultValue() {
		return defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	private void expand(int size) {
		int mask = mask(size, this.loadFactor);
		
		BitSet[] oldkeys = this.keys;
		T[] oldvalues = this.values;
		
		BitSet[] keys = new BitSet[mask + 1];
		T[] values = (T[]) new Object[mask + 1];

		int[] delayed = new int[this.size];
		int pos = 0;
		
		for (int i = 0; i < oldkeys.length; i++) {
			BitSet key = oldkeys[i];
			if (key != NULL_KEY && key != null) {
				T value = oldvalues[i];
				int slot = hash(key.hashCode()) & mask;
				if (keys[slot] == NULL_KEY || keys[slot] == null) {
					keys[slot] = key;
					values[slot] = value;
				} else {
					delayed[pos] = i;
					pos++;
				}
			}
		}
		for (int i = 0; i < pos; i++) {
			int j = delayed[i];
			BitSet key = oldkeys[j];
			T value = oldvalues[j];
			int slot = hash(key.hashCode()) & mask;
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
			BitSet key = keys[0];
			T value = values[0];
			buffer.append(toString(key)).append(": ").append(value);
			
		}
		for (int i = 1; i < keys.length; i++) {
			BitSet key = keys[i];
			T value = values[0];
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

}
