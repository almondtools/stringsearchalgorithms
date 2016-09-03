package net.amygdalum.util.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class StringUtils {

	private StringUtils() {
	}
	
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

	public static List<char[]> toCharArray(Collection<String> patterns) {
		List<char[]> charpatterns = new ArrayList<char[]>(patterns.size());
		for (String pattern : patterns) {
			charpatterns.add(pattern.toCharArray());
		}
		return charpatterns;
	}

}
