package net.amygdalum.util.text;

import java.util.ArrayList;
import java.util.List;

public class QGramMapping {
	private int q;
	private int bits;
	private int mask;

	public QGramMapping(int q, int bits) {
		this.q = q;
		this.bits = bits;
		this.mask = mask(bits);
	}

	private static int mask(int bits) {
		int mask = 0;
		for (int i = 0; i < bits; i++) {
			mask = (mask << 1) | 1;
		}
		return mask;
	}

	public int map(char[] chars, int pos) {
		if (pos + q > chars.length) {
			throw new MappingException();
		}
		int code = 0;
		for (int i = pos; i < pos + q; i++) {
			char c = chars[i];
			code = (code << bits) | ((int) c & mask);
		}
		return code;
	}

	public int[] map(char[] chars, int pos, CharMapping mapping) {
		if (pos + q > chars.length) {
			throw new MappingException();
		}
		List<Integer> codes = new ArrayList<>(1);
		codes.add(0);
		for (int i = pos; i < pos + q; i++) {
			List<Integer> nextcodes = new ArrayList<>();
			for (Integer code : codes) {
				for (char c : mapping.map(chars[i])) {
					nextcodes.add((code << bits) | ((int) c & mask));
				}
			}
			codes = nextcodes;
		}
		int[] flatcodes = new int[codes.size()];
		for (int i = 0; i < flatcodes.length; i++) {
			flatcodes[i] = codes.get(i);
		}
		return flatcodes;
	}
}
