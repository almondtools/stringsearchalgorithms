package net.amygdalum.stringsearchalgorithms.search.chars;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;

/**
 * An implementation of the String Search Algorithm of Knuth-Morris-Pratt.
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
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
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return new Finder(chars, options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private int patternPointer;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.patternPointer = 0;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			patternPointer = 0;
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
					StringMatch match = createMatch();
					patternPointer = next[patternPointer];
					return match;
				}
			}
			return null;
		}

		private StringMatch createMatch() {
			long end = chars.current();
			long start = end - patternPointer;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements StringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new KnuthMorrisPratt(pattern);
		}

	}

}
