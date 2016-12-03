package net.amygdalum.util.text;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public final class CharUtils {

	private CharUtils() {
	}

	public static char[] revert(char[] chars) {
		final int ri = chars.length - 1;
		char[] reversechars = new char[chars.length];
		for (int i = 0; i < reversechars.length; i++) {
			reversechars[i] = chars[ri - i];
		}
		return reversechars;
	}

	public static int lastIndexOf(char[] pattern, char[] block) {
		nextPos: for (int i = pattern.length - block.length; i >= 0; i--) {
			for (int j = block.length - 1; j >= 0; j--) {
				if (pattern[j + i] == block[j]) {
					continue;
				} else {
					continue nextPos;
				}
			}
			return i;
		}
		return -1;
	}

	public static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	public static String charToString(char ch) {
		if (isAsciiPrintable(ch)) {
			return String.valueOf(ch);
		} else {
			StringBuilder buffer = new StringBuilder("\\u");
			String hex = Integer.toHexString((int) ch);
			for (int i = 0; i < 4 - hex.length(); i++) {
				buffer.append('0');
			}
			buffer.append(hex);
			return buffer.toString();
		}
	}

	public static char after(char c) {
		return (char) (c + 1);
	}

	public static char before(char c) {
		return (char) (c - 1);
	}

	public static char computeMinChar(char[] pattern) {
		char min = Character.MAX_VALUE;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] < min) {
				min = pattern[i];
			}
		}
		return min;
	}

	public static char computeMinChar(Collection<char[]> patterns) {
		char min = Character.MAX_VALUE;
		for (char[] pattern : patterns) {
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i] < min) {
					min = pattern[i];
				}
			}
		}
		return min;
	}

	public static char computeMaxChar(char[] pattern) {
		char max = Character.MIN_VALUE;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] > max) {
				max = pattern[i];
			}
		}
		return max;
	}

	public static char computeMaxChar(Collection<char[]> patterns) {
		char max = Character.MIN_VALUE;
		for (char[] pattern : patterns) {
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i] > max) {
					max = pattern[i];
				}
			}
		}
		return max;
	}

	public static int minLength(List<char[]> patterns) {
		int len = Integer.MAX_VALUE;
		for (char[] pattern : patterns) {
			if (pattern.length < len) {
				len = pattern.length;
			}
		}
		return len;
	}

	public static int maxLength(List<char[]> patterns) {
		int len = Integer.MIN_VALUE;
		for (char[] pattern : patterns) {
			if (pattern.length > len) {
				len = pattern.length;
			}
		}
		return len;
	}

	public static int[] lengths(List<char[]> patterns) {
		SortedSet<Integer> lengths = new TreeSet<>();
		for (char[] pattern : patterns) {
			lengths.add(pattern.length);
		}
		int[] lengthsArray = new int[lengths.size()];
		int i = lengthsArray.length - 1;
		for (int length : lengths) {
			lengthsArray[i] = length;
			i--;
		}
		return lengthsArray;
	}

}
