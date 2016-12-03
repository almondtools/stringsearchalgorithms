package net.amygdalum.util.text;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StringSet {

	private Set<String>[] strings;
	private int[] lengths;

	public StringSet(List<char[]> chars) {
		this.lengths = CharUtils.lengths(chars);
		this.strings = computeStrings(lengths, chars);
	}

	@SuppressWarnings("unchecked")
	private static Set<String>[] computeStrings(int[] lengths, List<char[]> chars) {
		int max = lengths[0];
		int min = lengths[lengths.length - 1];
		Set<String>[] strings = new Set[max - min + 1];
		for (int len : lengths) {
			strings[max - len] = new LinkedHashSet<>();
		}
		for (char[] pattern : chars) {
			int len = pattern.length;
			Set<String> set = strings[max - len];
			set.add(new String(pattern));
		}
		return strings;
	}

	public int minLength() {
		return lengths[lengths.length - 1];
	}

	public int maxLength() {
		return lengths[0];
	}

	public int[] containedLengths() {
		return lengths;
	}

	public boolean contains(char[] pattern) {
		int max = maxLength();
		int len = pattern.length;
		Set<String> set = strings[max - len];
		if (set == null) {
			return false;
		} else {
			return set.contains(new String(pattern));
		}
	}

}
