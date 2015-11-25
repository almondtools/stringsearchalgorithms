package com.almondtools.stringsandchars.search;

import java.util.Map;
import java.util.SortedMap;

public class SparseIntArray {

	private long[] array;
	private int defaultValue;

	public SparseIntArray(SortedMap<Integer, Integer> shift, int defaultValue) {
		this.array = createArray(shift);
		this.defaultValue = defaultValue;
	}

	public static long[] createArray(SortedMap<Integer, Integer> shift) {
		long[] array = new long[shift.size()];
		int i = 0;
		for (Map.Entry<Integer, Integer> entry : shift.entrySet()) {
			array[i] = (((long) entry.getKey()) << 32) | ((long) entry.getValue());
			i++;
		}
		return array;
	}

	public int get(int i) {
		int leftI = 0;
		int rightI = array.length - 1;
		long left = array[leftI];
		long right = array[rightI];
		int leftKey = (int) (left >> 32);
		int rightKey = (int) (right >> 32);
		if (i < leftKey || i > rightKey) {
			return defaultValue;
		} else if (leftKey == i) {
			return (int) (left & 0x0000ffff);
		} else if (rightKey == i) {
			return (int) (right & 0x0000ffff);
		}
		while (true) {
			if (leftKey == rightKey) {
				return defaultValue;
			}
			int midI = (leftI + rightI) >> 1;
			long mid = array[midI];
			int midKey = (int) (mid >> 32);
			if (midKey == i) {
				return (int) (mid & 0x0000ffff);
			} else if (midKey < i && midKey > leftKey) {
				leftI = midI;
				left = mid;
				leftKey = midKey;
			} else if (midKey > i && midKey < rightKey) {
				rightI = midI;
				right = mid;
				rightKey = midKey;
			} else {
				return defaultValue;
			}
		}
	}

}
