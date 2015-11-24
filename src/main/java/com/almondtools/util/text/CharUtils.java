package com.almondtools.util.text;

public class CharUtils {

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

	public static char computeMaxChar(char[] pattern) {
		char max = Character.MIN_VALUE;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] > max) {
				max = pattern[i];
			}
		}
		return max;
	}

}
