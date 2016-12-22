package net.amygdalum.util.map;

import static java.util.Arrays.sort;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ByteObjectMap<T> extends TuneableMap {

	private static final byte NULL_KEY = 0;

	private float loadFactor;
	private int mask;
	private int expandAt;
	private int size;

	private byte[] keys;
	private T[] values;
	private T defaultValue;
	private T nullValue;

	public ByteObjectMap(T defaultValue) {
		this(DEFAULT_SIZE, DEFAULT_LOAD, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public ByteObjectMap(int initialSize, float loadFactor, T defaultValue) {
		this.loadFactor = loadFactor;
		this.mask = mask(initialSize, loadFactor);
		this.expandAt = initialSize;
		this.size = 0;
		this.keys = new byte[mask + 1];
		this.values = (T[]) new Object[mask + 1];
		this.defaultValue = defaultValue;
		this.nullValue = defaultValue;
	}

	public int size() {
		int size = this.size;
		if (nullValue != defaultValue) {
			size++;
		}
		return size;
	}

	public byte[] keys() {
		int size = this.size;
		if (nullValue != defaultValue) {
			size++;
		}
		byte[] keys = new byte[size];
		int pos = 0;
		for (byte b : this.keys) {
			if (b != NULL_KEY) {
				keys[pos] = b;
				pos++;
			}
		}
		if (nullValue != defaultValue && pos < keys.length) {
			keys[pos] = NULL_KEY;
		}
		sort(keys);
		return keys;
	}

	public ByteObjectMap<T> add(byte key, T value) {
		put(key, value);
		return this;
	}

	public void put(byte key, T value) {
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

	public T get(byte key) {
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

	public Iterable<Entry<T>> cursor() {
		return new EntryIterable<T>(this);
	}

	@SuppressWarnings("unchecked")
	private void expand(int size) {
		int mask = mask(size, this.loadFactor);

		byte[] oldkeys = this.keys;
		T[] oldvalues = this.values;

		byte[] keys = new byte[mask + 1];
		T[] values = (T[]) new Object[mask + 1];

		int[] delayed = new int[this.size];
		int pos = 0;

		for (int i = 0; i < oldkeys.length; i++) {
			byte key = oldkeys[i];
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
			byte key = oldkeys[j];
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
			byte key = keys[0];
			T value = values[0];
			buffer.append(key).append(": ").append(value);

		}
		for (int i = 1; i < keys.length; i++) {
			byte key = keys[i];
			T value = values[i];
			buffer.append(",\n").append(key).append(": ").append(value);
		}
		buffer.append("\n}");
		return buffer.toString();
	}

	public static class EntryIterable<T> implements Iterable<Entry<T>> {

		private ByteObjectMap<T> map;

		public EntryIterable(ByteObjectMap<T> map) {
			this.map = map;
		}

		@Override
		public Iterator<Entry<T>> iterator() {
			return new EntryIterator<T>(map);
		}
	}

	public static class EntryIterator<T> implements Iterator<Entry<T>> {

		private ByteObjectMap<T> map;
		private int index;
		private int currentKey;
		private int fixedSize;
		private Entry<T> entry;

		public EntryIterator(ByteObjectMap<T> map) {
			this.map = map;
			this.index = 0;
			this.currentKey = -1;
			this.fixedSize = map.size;
			this.entry = new Entry<>();
		}
		
		@Override
		public boolean hasNext() {
			if (map.size != fixedSize) {
				throw new ConcurrentModificationException();
			}
			return index < fixedSize || index == fixedSize && map.nullValue != map.defaultValue;
		}

		@Override
		public Entry<T> next() {
			if (map.size != fixedSize) {
				throw new ConcurrentModificationException();
			}
			while (currentKey < map.keys.length - 1) {
				currentKey++;
				byte b = map.keys[currentKey];
				if (b != NULL_KEY) {
					entry.key = map.keys[currentKey];
					entry.value = map.values[currentKey];
					index++;
					return entry;
				}
			}
			if (map.nullValue != map.defaultValue) {
				entry.key = NULL_KEY;
				entry.value = map.nullValue;
				index++;
				return entry;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			if (currentKey < 0 || currentKey >= map.keys.length) {
				throw new NoSuchElementException();
			}
			map.keys[currentKey] = NULL_KEY;
			map.values[currentKey] = map.defaultValue;
		}
	}

	public static class Entry<T> {

		public byte key;
		public T value;

	}

}
