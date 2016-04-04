package com.almondtools.util.map;

public class TuneableMap {

	public static final int DEFAULT_SIZE = 16;
	public static final float DEFAULT_LOAD = 0.7f;

    private static final int INT_PHI = 0x9E3779B9;
    private static final long LONG_PHI = 0x9E3779B97F4A7C15L;

	protected static int mask(int initialSize, float loadFactor) {
		long size = (long) Math.ceil((double) initialSize / (double) loadFactor);
		if (size <= 0) {
			return 1;
		} else {
			size--;
			size |= size >> 1;
			size |= size >> 2;
			size |= size >> 4;
			size |= size >> 8;
			size |= size >> 16;
			size |= size >> 32;
			if (size < 2) {
				return 1;
			} else {
				return (int) size;
			}
		}
	}

	public static int hash(int key) {
		int h = key * INT_PHI;
		return h ^ (h >> 16);
	}

	public static int hash(long key) {
		long h = key * LONG_PHI;
		h ^= h >> 32;
		return (int) (h ^ (h >> 16));
	}

}
