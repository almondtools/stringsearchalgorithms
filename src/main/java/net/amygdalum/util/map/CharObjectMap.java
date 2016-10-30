package net.amygdalum.util.map;

import static java.util.Arrays.sort;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharObjectMap<T> extends TuneableMap {

	private static final char NULL_KEY = 0;

	private float loadFactor;
	private int mask;
	private int expandAt;
	private int size;

	private char[] keys;
	private T[] values;
	private T defaultValue;
	private T nullValue;

	public CharObjectMap(T defaultValue) {
		this(DEFAULT_SIZE, DEFAULT_LOAD, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public CharObjectMap(int initialSize, float loadFactor, T defaultValue) {
		this.loadFactor = loadFactor;
		this.mask = mask(initialSize, loadFactor);
		this.expandAt = initialSize;
		this.size = 0;
		this.keys = new char[mask + 1];
		this.values = (T[]) new Object[mask + 1];
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
		sort(keys);
		return keys;
	}

	public CharObjectMap<T> add(char key, T value) {
		put(key, value);
		return this;
	}

	public void put(char key, T value) {
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

	public T get(char key) {
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

	public T getDefaultValue() {
		return defaultValue;
	}

	public Iterable<Entry> cursor() {
		return new EntryIterable();
	}

	@SuppressWarnings("unchecked")
	private void expand(int size) {
		int mask = mask(size, this.loadFactor);

		char[] oldkeys = this.keys;
		T[] oldvalues = this.values;

		char[] keys = new char[mask + 1];
		T[] values = (T[]) new Object[mask + 1];

		int[] delayed = new int[this.size];
		int pos = 0;

		for (int i = 0; i < oldkeys.length; i++) {
			char key = oldkeys[i];
			if (key != NULL_KEY) {
				T value = oldvalues[i];
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
			T value = oldvalues[j];
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
			T value = values[0];
			buffer.append(key).append(": ").append(value);

		}
		for (int i = 1; i < keys.length; i++) {
			char key = keys[i];
			T value = values[i];
			buffer.append(",\n").append(key).append(": ").append(value);
		}
		buffer.append("\n}");
		return buffer.toString();
	}

	public class EntryIterable implements Iterable<CharObjectMap<T>.Entry> {

		@Override
		public Iterator<CharObjectMap<T>.Entry> iterator() {
			return new EntryIterator();
		}
	}

	public class EntryIterator implements Iterator<CharObjectMap<T>.Entry> {

		private int index;
		private int currentKey;
		private int fixedSize;
		private Entry entry;

		public EntryIterator() {
			this.index = 0;
			this.currentKey = -1;
			this.fixedSize = size;
			this.entry = new Entry();
		}
		
		@Override
		public boolean hasNext() {
			if (size != fixedSize) {
				throw new ConcurrentModificationException();
			}
			return index < fixedSize || index == fixedSize && nullValue != defaultValue;
		}

		@Override
		public CharObjectMap<T>.Entry next() {
			if (size != fixedSize) {
				throw new ConcurrentModificationException();
			}
			while (currentKey < keys.length - 1) {
				currentKey++;
				char c = keys[currentKey];
				if (c != NULL_KEY) {
					entry.key = keys[currentKey];
					entry.value = values[currentKey];
					index++;
					return entry;
				}
			}
			if (nullValue != defaultValue) {
				entry.key = NULL_KEY;
				entry.value = nullValue;
				index++;
				return entry;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			if (currentKey < 0 || currentKey >= keys.length) {
				throw new NoSuchElementException();
			}
			keys[currentKey] = NULL_KEY;
			values[currentKey] = defaultValue;
		}
	}

	public class Entry {

		public char key;
		public T value;

	}

}
