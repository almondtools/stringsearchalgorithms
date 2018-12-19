package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.ByteUtils.maxLength;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.ByteUtils.revert;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteAutomaton;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.ByteTrieBuilder;
import net.amygdalum.util.text.ByteWordSet;
import net.amygdalum.util.text.StringUtils;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayByteCompactTrie;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayByteTrieBuilder;

/**
 * An implementation of the Set Horspool Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a
 * finder which can find any of these patterns in documents.
 */
public class SetHorspool implements StringSearchAlgorithm {

	private ByteWordSet<ByteString> trie;
	private int minLength;
	private int maxLength;
	private ByteShift byteShift;

	public SetHorspool(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = StringUtils.toByteArray(patterns, charset);
		this.trie = computeTrie(bytepatterns, charset);
		this.minLength = minLength(bytepatterns);
		this.maxLength = maxLength(bytepatterns);
		this.byteShift = computeByteShift(bytepatterns, minLength);
	}

	private ByteShift computeByteShift(List<byte[]> bytepatterns, int minLength) {
		return new QuickShift(bytepatterns, minLength);
	}

	private static ByteWordSet<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		ByteTrieBuilder<ByteString> builder = new DoubleArrayByteTrieBuilder<>(new DoubleArrayByteCompactTrie<ByteString>());

		for (byte[] pattern : bytepatterns) {
			builder.extend(revert(pattern), new ByteString(pattern, charset));
		}

		return builder.build();
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(trie, minLength, maxLength, byteShift, bytes, options);
		} else {
			return new NextMatchFinder(trie, minLength, maxLength, byteShift, bytes, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static abstract class Finder extends BufferedStringFinder {

		protected final int minLength;
		protected final int maxLength;
		protected final ByteShift byteShift;
		protected ByteProvider bytes;
		protected ByteAutomaton<ByteString> cursor;

		public Finder(ByteWordSet<ByteString> trie, int minLength, int maxLength, ByteShift byteShift, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.byteShift = byteShift;
			this.bytes = bytes;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			if (last > bytes.current()) {
				bytes.move(last);
			}
		}

		protected StringMatch createMatch(long start, long end) {
			ByteString slice = bytes.slice(start, end);
			return new StringMatch(start, end, slice.getString());
		}
	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(ByteWordSet<ByteString> trie, int minLength, int maxLength, ByteShift byteShift, ByteProvider bytes, StringFinderOption... options) {
			super(trie, minLength, maxLength, byteShift, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			int lookahead = minLength - 1;
			while (!bytes.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = bytes.current();
				byte current = bytes.lookahead(patternPointer);

				cursor.reset();
				boolean success = cursor.accept(current);
				while (success) {
					if (cursor.hasAttachments()) {
						ByteString match = cursor.iterator().next();
						long start = bytes.current() + patternPointer;
						long end = bytes.current() + patternPointer + match.length();
						push(createMatch(start, end));
					}
					patternPointer--;
					if (pos + patternPointer < 0) {
						break;
					}
					success = cursor.accept(bytes.lookahead(patternPointer));
				}
				bytes.forward(byteShift.getShift(current));
				if (!isBufferEmpty()) {
					return leftMost();
				}
			}
			return null;
		}

	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(ByteWordSet<ByteString> trie, int minLength, int maxLength, ByteShift byteShift, ByteProvider bytes, StringFinderOption... options) {
			super(trie, minLength, maxLength, byteShift, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			int lookahead = minLength - 1;
			while (!bytes.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = bytes.current();
				byte current = bytes.lookahead(patternPointer);

				cursor.reset();
				boolean success = cursor.accept(current);
				while (success) {
					if (cursor.hasAttachments()) {
						ByteString match = cursor.iterator().next();
						long start = bytes.current() + patternPointer;
						long end = bytes.current() + patternPointer + match.length();
						StringMatch stringMatch = createMatch(start, end);
						if (lastStart < 0) {
							lastStart = start;
						}
						push(stringMatch);
					}
					patternPointer--;
					if (pos + patternPointer < 0) {
						break;
					}
					success = cursor.accept(bytes.lookahead(patternPointer));
				}
				bytes.forward(byteShift.getShift(current));
				if (bufferContainsLongestMatch(lastStart)) {
					break;
				}
			}
			return longestLeftMost();
		}

		public boolean bufferContainsLongestMatch(long lastStart) {
			return !isBufferEmpty()
				&& bytes.current() - lastStart > maxLength;
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new SetHorspool(patterns, charset);
		}

	}

	private static class QuickShift implements ByteShift {

		private int[] byteShift;

		public QuickShift(List<byte[]> bytepatterns, int minLength) {
			this.byteShift = computeByteShift(bytepatterns, minLength);
		}

		private static int[] computeByteShift(List<byte[]> patterns, int minLength) {
			int[] bytes = new int[256];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = minLength;
			}
			for (byte[] pattern : patterns) {
				for (int i = 0; i < pattern.length - 1; i++) {
					bytes[pattern[i] & 0xff] = min(bytes[pattern[i] & 0xff], pattern.length - i - 1);
				}
			}
			return bytes;
		}

		@Override
		public int getShift(byte b) {
			return byteShift[b & 0xff];
		}

	}

}
