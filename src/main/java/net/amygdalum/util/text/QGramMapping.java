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
	
	public char[] newQGram() {
		return new char[q];
	}

	public int getQ() {
		return q;
	}

	public int map(char[] chars) {
		if (q != chars.length) {
			throw new MappingException();
		}
		int code = 0;
		for (int i = 0; i < q; i++) {
			char c = chars[i];
			code = (code << bits) | ((int) c & mask);
		}
		return code;
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

	public int[] map(char[] chars, CharMapping mapping) {
		if (q != chars.length) {
			throw new MappingException();
		}
		List<Integer> codes = new ArrayList<>(1);
		codes.add(0);
		for (int i = 0; i < q; i++) {
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

	public int[] iterate(char[] chars) {
		if (chars.length < q) {
			throw new MappingException();
		}
		int[] qgrams = new int[chars.length - q +1];
		for (int i = 0; i < qgrams.length; i++) {
			qgrams[i] = map(chars, i);
		}
		return qgrams;
	}
	
	public int[][] iterate(char[] chars, CharMapping mapping) {
		if (chars.length < q) {
			throw new MappingException();
		}
		int[][] qgrams = new int[chars.length - q +1][];
		for (int i = 0; i < qgrams.length; i++) {
			qgrams[i] = map(chars, i, mapping);
		}
		return qgrams;
	}

}
