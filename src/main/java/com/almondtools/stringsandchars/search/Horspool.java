package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;

public class Horspool implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private int[] characterShift;
	private char minChar;
	private char maxChar;

	public Horspool(String pattern) {
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
		for (int i = 0; i < pattern.length - 1; i++) {
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

	private class Finder implements StringFinder {

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
							chars.forward(getShift(nextChar));
							continue next;
						}
					}
					if (patternPointer == 0) {
						StringMatch match = createMatch();
						chars.forward(getShift(nextChar));
						return match;
					}
				} else {
					chars.forward(getShift(nextChar));
				}
			}
			return null;
		}

		@Override
		public List<StringMatch> findAll() {
			List<StringMatch> matches = new ArrayList<StringMatch>();
			while (true) {
				StringMatch match = findNext();
				if (match == null) {
					return matches;
				} else {
					matches.add(match);
				}
			}
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

}
