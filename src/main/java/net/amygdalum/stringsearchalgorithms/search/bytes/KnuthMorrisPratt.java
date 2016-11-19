package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.stringsearchalgorithms.search.bytes.Encoding.encode;

import java.nio.charset.Charset;

import net.amygdalum.stringsearchalgorithms.io.ByteProvider;
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

	private byte[] pattern;
	private int patternLength;
	private int[] next;

	public KnuthMorrisPratt(String pattern, Charset charset) {
		this.pattern = encode(pattern, charset);
		this.patternLength = this.pattern.length;
		this.next = computeNext(this.pattern);
	}

	private int[] computeNext(byte[] pattern) {
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
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		return new Finder(bytes, options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private ByteProvider bytes;
		private int patternPointer;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
			this.patternPointer = 0;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			patternPointer = 0;
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte nextByte = bytes.next();
				while (patternPointer > -1 && pattern[patternPointer] != nextByte) {
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

		private StringMatch createMatch(int match) {
			long end = bytes.current();
			long start = end - match;
			String s = bytes.slice(start, end).getString();
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements StringSearchAlgorithmFactory {

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new KnuthMorrisPratt(pattern, charset);
		}

	}

}
