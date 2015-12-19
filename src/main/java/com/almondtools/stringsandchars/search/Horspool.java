package com.almondtools.stringsandchars.search;

import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the String Search Algorithm of Horspool (or Boyer-Moore-Horpool).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class Horspool implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private CharShift charShift;

	public Horspool(String pattern) {
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
							chars.forward(charShift.getShift(nextChar));
							continue next;
						}
					}
					if (patternPointer == 0) {
						StringMatch match = createMatch();
						chars.forward(charShift.getShift(nextChar));
						return match;
					}
				} else {
					chars.forward(charShift.getShift(nextChar));
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
			return new Horspool(pattern);
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
			this.defaultShift = pattern.length;
		}

		private static int[] computeCharacterShift(char[] pattern, char min, char max) {
			int[] characters = new int[max - min + 1];
			for (int i = 0; i < characters.length; i++) {
				characters[i] = pattern.length;
			}
			for (int i = 0; i < pattern.length - 1; i++) {
				characters[pattern[i] - min] = pattern.length - i - 1;
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

		private CharIntMap characterShift;

		public SmartShift(char[] pattern, char minChar, char maxChar) {
			this.characterShift = computeCharacterShift(pattern, minChar, maxChar);
		}

		private static CharIntMap computeCharacterShift(char[] pattern, char min, char max) {
			CharIntMap.Builder mapBuilder = new CharIntMap.Builder(pattern.length);
			for (int i = 0; i < pattern.length - 1; i++) {
				mapBuilder.put(pattern[i], pattern.length - i - 1);
			}
			return mapBuilder.perfectMinimal();
		}

		@Override
		public int getShift(char c) {
			return characterShift.get(c);
		}

	}

}
