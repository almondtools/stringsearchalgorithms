package com.almondtools.util.map;


public class HashFunction {

	public static final HashFunction NULL = new HashFunction(new int[] { 0 }, 0, 0);

	private final int[] g;
	private final int n;
	private final int seed1;
	private final int seed2;

	public HashFunction(int[] g, int seed1, int seed2) {
		this.g = g;
		this.n = g.length;
		this.seed1 = seed1;
		this.seed2 = seed2;
	}

	public final int hash(long[] key) {
		int[] hashes = doubleHash(key);
		return g[hashes[0]] + g[hashes[1]];
	}
	
	public int[] doubleHash(long[] key) {
		
		int h1 = hash(key, seed1) % n;
		if (h1 < 0) {
			h1 += n;
		}

		int h2 = hash(key, seed2) % n;
		h2 %= n;
		if (h2 < 0) {
			h2 += n;
		}
		if (h1 == h2) {
			h2 = (h2 + 1) % n;
		}

		return new int[] { h1, h2 };
	}

	public final int hash(int key) {
		int[] hashes = doubleHash(key);
		return g[hashes[0]] + g[hashes[1]];
	}

	public int[] doubleHash(int key) {
		int h1 = hash(key,seed1) % n;
		if (h1 < 0) {
			h1 += n;
		}

		int h2 = hash(key,seed2) % n;
		if (h2 < 0) {
			h2 += n;
		}
		if (h1 == h2) {
			h2 = (h2 + 1) % n;
		}

		return new int[] { h1, h2 };
	}
	
	private static final int hash(long[] key, int seed) {
		int hash = seed;
		for (int i = 0; i < key.length; i++) {
			seed += i;
			hash ^= hash(key[i], seed);
		}
		return hash;
	}
	private static final int hash(long key, int seed) {
		key ^= key >> 23;
		key *= 0x2127599bf4325c37L;
		key ^= key >> 47;
		return hash((int) (key ^ key >>> 32), seed);
	}

	private static final int hash(int key, int seed) {
		key ^= seed;
		key += (key <<  15) ^ 0xffffcd7d;
		key ^= (key >>> 10);
		key += (key <<   3);
		key ^= (key >>>  6);
		key += (key <<   2) + (key << 14);
		return key ^ (key >>> 16);
	}

}
