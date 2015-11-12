package com.almondtools.util.text;

import java.util.Iterator;

public class StringUtils {

	public static String reverse(String word) {
		char[] chars = word.toCharArray();
        int size = word.length();
		for (int li = 0, ri = size - 1; li < ri; li++,ri--) {
            char swap = chars[li];
            chars[li] = chars[ri];
            chars[ri] = swap;
        }
		return new String(chars);
	}

	public static String join(Iterable<?> objects) {
		Iterator<?> iterator = objects.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder buffer = new StringBuilder(iterator.next().toString());
		while (iterator.hasNext()) {
			buffer.append(iterator.next().toString());
		}
		return buffer.toString();
	}

	public static String join(Iterable<?> objects, char c) {
		Iterator<?> iterator = objects.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder buffer = new StringBuilder(iterator.next().toString());
		while (iterator.hasNext()) {
			buffer.append(c);
			buffer.append(iterator.next().toString());
		}
		return buffer.toString();
	}

	public static String join(Iterable<?> objects, String s) {
		Iterator<?> iterator = objects.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder buffer = new StringBuilder(iterator.next().toString());
		while (iterator.hasNext()) {
			buffer.append(s);
			buffer.append(iterator.next().toString());
		}
		return buffer.toString();
	}

}
