package net.amygdalum.util.text;

public class CharAlphabet {

	private char minChar;
	private char maxChar;

	private CharAlphabet() {
		this.minChar = Character.MAX_VALUE;
		this.maxChar = Character.MIN_VALUE;
	}

	public static CharAlphabet ranged(char[] pattern, CharMapping mapping) {
		CharAlphabet charAlphabet = new CharAlphabet();
		for (char pc : pattern) {
			for (char c : mapping.map(pc)) {
				charAlphabet.add(c);
			}
		}
		return charAlphabet;
	}

	public static CharAlphabet ranged(char[] pattern) {
		CharAlphabet charAlphabet = new CharAlphabet();
		for (char c : pattern) {
			charAlphabet.add(c);
		}
		return charAlphabet;
	}

	private void add(char c) {
		if (c < minChar) {
			minChar = c;
		}
		if (c > maxChar) {
			maxChar = c;
		}
	}

	public int getRange() {
		if (maxChar <= minChar) {
			return 0;
		}
		return maxChar - minChar + 1;
	}

	public char minChar() {
		return minChar;
	}

	public char maxChar() {
		return maxChar;
	}

}
