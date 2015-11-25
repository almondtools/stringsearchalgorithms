package com.almondtools.stringsandchars.search;

import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;

import java.util.SortedMap;
import java.util.TreeMap;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the String Search Algorithm of Horspool (or Boyer-Moore-Horpool).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class Sunday implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private CharShift charShift;

	public Sunday(String pattern) {
		this.pattern = pattern.toCharArray();
		this.patternLength = this.pattern.length;
		this.charShift = computeShift(this.pattern);
	}

	private static CharShift computeShift(char[] pattern) {
		char minChar = computeMinChar(pattern);
		char maxChar = computeMaxChar(pattern);
		if (maxChar - minChar < 256 || maxChar - minChar < pattern.length * 2) {
			return new QuickShift(pattern, minChar, maxChar);
		} else {
			return new SmartShift(pattern, minChar, maxChar);
		}
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
								chars.forward(charShift.getShift(afterNextChar));
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
							chars.forward(charShift.getShift(afterNextChar));
						} else {
							chars.finish();
						}
						return match;
					}
				} else {
					if (!chars.finished(patternPointer + 1)) {
						char afterNextChar = chars.lookahead(patternPointer + 1);
						chars.forward(charShift.getShift(afterNextChar));
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

	private static class QuickShift implements CharShift {

		private char minChar;
		private char maxChar;
		private int[] characterShift;
		private int defaultShift;

		public QuickShift(char[] pattern, char minChar, char maxChar) {
			this.minChar = minChar;
			this.maxChar = maxChar;
			this.characterShift = computeCharacterShift(pattern, this.minChar, this.maxChar);
			this.defaultShift = pattern.length + 1;
		}

		private int[] computeCharacterShift(char[] pattern, char min, char max) {
			int[] characters = new int[max - min + 1];
			for (int i = 0; i < characters.length; i++) {
				characters[i] = pattern.length + 1;
			}
			for (int i = 0; i < pattern.length; i++) {
				characters[pattern[i] - min] = pattern.length - i;
			}
			return characters;
		}

		@Override
		public int getShift(char c) {
			if (c < minChar || c > maxChar) {
				return defaultShift;
			}
			return characterShift[c - minChar];
		}

	}

	private static class SmartShift implements CharShift {

		private SparseIntArray characterShift;

		public SmartShift(char[] pattern, char minChar, char maxChar) {
			this.characterShift = computeCharacterShift(pattern, minChar, maxChar);
		}

		private SparseIntArray computeCharacterShift(char[] pattern, char min, char max) {
			SortedMap<Integer, Integer> shift = new TreeMap<>();
			for (int i = 0; i < pattern.length; i++) {
				shift.put((int) pattern[i], pattern.length - i);
			}
			return new SparseIntArray(shift, pattern.length);
		}

		@Override
		public int getShift(char c) {
			return characterShift.get(c);
		}

	}

}
