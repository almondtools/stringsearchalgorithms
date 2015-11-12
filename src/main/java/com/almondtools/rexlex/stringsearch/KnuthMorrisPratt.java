package com.almondtools.rexlex.stringsearch;

import java.util.ArrayList;
import java.util.List;

import com.almondtools.rexlex.io.CharProvider;

public class KnuthMorrisPratt implements StringSearchAlgorithm {

	private char[] pattern;
	private int patternLength;
	private int[] next;

	public KnuthMorrisPratt(String pattern) {
		this.pattern = pattern.toCharArray();
		this.patternLength = this.pattern.length;
		this.next = computeNext(this.pattern);
	}

	private int[] computeNext(char[] pattern) {
		int[] next = new int[patternLength + 1];
		next[0] = -1;

		int patternPointer = 0;
		int suffixPointer = -1;
		while (patternPointer < patternLength) {
			while (suffixPointer > -1 && pattern[patternPointer] != pattern[suffixPointer]) {
				suffixPointer = next[suffixPointer];
			}
			patternPointer++;
			suffixPointer++;
			if (patternPointer < patternLength && pattern[patternPointer] == pattern[suffixPointer]) {
				next[patternPointer] = next[suffixPointer];
			} else {
				next[patternPointer] = suffixPointer;
			}
		}
		return next;
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
		private int patternPointer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.patternPointer = 0;
		}

		@Override
		public void skipTo(int pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				while (patternPointer > -1 && pattern[patternPointer] != nextChar) {
					patternPointer = next[patternPointer];
				}
				patternPointer++;
				if (patternPointer >= patternLength) {
					int match = patternPointer;
					patternPointer = next[patternPointer];
					return createMatch(match);
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

		private StringMatch createMatch(int match) {
			int end = chars.current();
			int start = end - match;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements WordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new KnuthMorrisPratt(pattern);
		}

	}

}
