package com.almondtools.stringsandchars.search;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the String Search Algorithm of Horspool (or Boyer-Moore-Horpool).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class Sunday implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private int[] characterShift;
	private char minChar;
	private char maxChar;

	public Sunday(String pattern) {
		this.pattern = pattern.toCharArray();
		this.patternLength = this.pattern.length;
		this.minChar = computeMinChar(this.pattern);
		this.maxChar = computeMaxChar(this.pattern);
		this.characterShift = computeCharacterShift(this.pattern, this.minChar, this.maxChar);
	}

	private char computeMinChar(char[] pattern) {
		char min = Character.MAX_VALUE;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] < min) {
				min = pattern[i];
			}
		}
		return min;
	}

	private char computeMaxChar(char[] pattern) {
		char max = Character.MIN_VALUE;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] > max) {
				max = pattern[i];
			}
		}
		return max;
	}

	private int[] computeCharacterShift(char[] pattern, char min, char max) {
		int[] characters = new int[max - min + 1];
		for (int i = 0; i < characters.length; i++) {
			characters[i] = pattern.length;
		}
		for (int i = 0; i < pattern.length; i++) {
			characters[pattern[i] - min] = pattern.length - i - 1;
		}
		return characters;
	}

	private int getShift(char c) {
		if (c < minChar || c > maxChar) {
			return patternLength;
		}
		return characterShift[c - minChar];
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;

		public Finder(CharProvider chars) {
			this.chars = chars;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			final int lookahead = patternLength - 1;
			next: while (!chars.finished(lookahead)) {
				int patternPointer = lookahead;
				char nextChar = chars.lookahead(patternPointer);
				if (pattern[patternPointer] == nextChar) {
					while (patternPointer > 0) {
						patternPointer--;
						if (pattern[patternPointer] != chars.lookahead(patternPointer)) {
							if (!chars.finished(patternPointer + 1)) {
								char afterNextChar = chars.lookahead(patternPointer + 1);
								chars.forward(getShift(afterNextChar) + 1);
							} else {
								chars.finish();
							}
							continue next;
						}
					}
					if (patternPointer == 0) {
						StringMatch match = createMatch();
						if (!chars.finished(patternPointer + 1)) {
							char afterNextChar = chars.lookahead(patternPointer + 1);
							chars.forward(getShift(afterNextChar) + 1);
						} else {
							chars.finish();
						}
						return match;
					}
				} else {
					if (!chars.finished(patternPointer + 1)) {
						char afterNextChar = chars.lookahead(patternPointer + 1);
						chars.forward(getShift(afterNextChar) + 1);
					} else {
						chars.finish();
					}
				}
			}
			return null;
		}

		private StringMatch createMatch() {
			long start = chars.current();
			long end = start + patternLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements WordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new Sunday(pattern);
		}

	}

}
