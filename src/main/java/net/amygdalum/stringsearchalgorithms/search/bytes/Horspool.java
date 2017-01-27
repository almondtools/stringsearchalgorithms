package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.util.text.ByteEncoding.encode;

import java.nio.charset.Charset;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteString;

/**
 * An implementation of the String Search Algorithm of Horspool.
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class Horspool implements StringSearchAlgorithm {

	private byte[] pattern;
	private int patternLength;
	private ByteShift byteShift;

	public Horspool(String pattern, Charset charset) {
		this.pattern = encode(pattern, charset);
		this.patternLength = this.pattern.length;
		this.byteShift = computeShift(this.pattern);
	}

	private static ByteShift computeShift(byte[] pattern) {
		return new QuickShift(pattern);
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

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
		}

		@Override
		public StringMatch findNext() {
			final int lookahead = patternLength - 1;
			next: while (!bytes.finished(lookahead)) {
				int patternPointer = lookahead;
				byte nextByte = bytes.lookahead(patternPointer);
				if (pattern[patternPointer] == nextByte) {
					while (patternPointer > 0) {
						patternPointer--;
						if (pattern[patternPointer] != bytes.lookahead(patternPointer)) {
							bytes.forward(byteShift.getShift(nextByte));
							continue next;
						}
					}
					if (patternPointer == 0) {
						StringMatch match = createMatch();
						bytes.forward(byteShift.getShift(nextByte));
						return match;
					}
				} else {
					bytes.forward(byteShift.getShift(nextByte));
				}
			}
			return null;
		}

		private StringMatch createMatch() {
			long start = bytes.current();
			long end = start + patternLength;
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
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
			return new Horspool(pattern, charset);
		}

	}

	private static class QuickShift implements ByteShift {

		private int[] byteShift;

		public QuickShift(byte[] pattern) {
			this.byteShift = computeByteShift(pattern);
		}

		private static int[] computeByteShift(byte[] pattern) {
			int[] bytes = new int[256];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = pattern.length;
			}
			for (int i = 0; i < pattern.length - 1; i++) {
				bytes[pattern[i] & 0xff] = pattern.length - i - 1;
			}
			return bytes;
		}

		@Override
		public int getShift(byte b) {
			return byteShift[b & 0xff];
		}

	}

}
