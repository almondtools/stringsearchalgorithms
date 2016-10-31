package net.amygdalum.stringsearchalgorithms.search;

import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.util.map.CharIntMap;

/**
 * An implementation of the String Search Algorithm of Sunday.
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class Sunday implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private CharShift charShift;

	public Sunday(String pattern) {
		this(pattern, false);
	}

	public Sunday(String pattern, boolean relaxed) {
		this.pattern = pattern.toCharArray();
		this.patternLength = this.pattern.length;
		this.charShift = computeShift(this.pattern, relaxed);
	}

	private static CharShift computeShift(char[] pattern, boolean relaxed) {
		if (isCompactRange(pattern)) {
			return new QuickShift(pattern);
		} else if (relaxed) {
			return new RelaxedShift(pattern);
		} else {
			return new SmartShift(pattern);
		}
	}

	public static boolean isCompactRange(char[] pattern) {
		char minChar = computeMinChar(pattern);
		char maxChar = computeMaxChar(pattern);
		return maxChar - minChar < 256 || maxChar - minChar < pattern.length * 2;
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return new Finder(chars, options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
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

	public static class Factory implements StringSearchAlgorithmFactory {

		private boolean relaxed;

		public Factory() {
			this(false);
		}

		public Factory(boolean relaxed) {
			this.relaxed = relaxed;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new Sunday(pattern, relaxed);
		}

	}

	private static class QuickShift implements CharShift {

		private char minChar;
		private char maxChar;
		private int[] characterShift;
		private int defaultShift;

		public QuickShift(char[] pattern) {
			this.minChar = computeMinChar(pattern);
			this.maxChar = computeMaxChar(pattern);
			this.characterShift = computeCharacterShift(pattern, this.minChar, this.maxChar);
			this.defaultShift = pattern.length + 1;
		}

		private static int[] computeCharacterShift(char[] pattern, char min, char max) {
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

	private static class RelaxedShift implements CharShift {

		private int[] characterShift;

		public RelaxedShift(char[] pattern) {
			this.characterShift = computeCharacterShift(pattern);
		}

		private static int[] computeCharacterShift(char[] pattern) {
			int[] characters = new int[256];
			for (int i = 0; i < characters.length; i++) {
				characters[i] = pattern.length + 1;
			}
			for (int i = 0; i < pattern.length; i++) {
				int index = pattern[i] % 256;
				int newShift = pattern.length - i;
				if (newShift < characters[index]) {
					characters[index] = newShift;
				}
			}
			return characters;
		}

		@Override
		public int getShift(char c) {
			return characterShift[c % 256];
		}

	}

	private static class SmartShift implements CharShift {

		private CharIntMap characterShift;

		public SmartShift(char[] pattern) {
			this.characterShift = computeCharacterShift(pattern);
		}

		private static CharIntMap computeCharacterShift(char[] pattern) {
			CharIntMap map = new CharIntMap(pattern.length);
			for (int i = 0; i < pattern.length; i++) {
				map.put(pattern[i], pattern.length - i);
			}
			return map;
		}

		@Override
		public int getShift(char c) {
			return characterShift.get(c);
		}

	}

}
