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
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.StringUtils;
import net.amygdalum.util.tries.ByteTrieNode;
import net.amygdalum.util.tries.ByteTrieNodeCompiler;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the Set Horspool Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetHorspool implements StringSearchAlgorithm {

	private ByteTrieNode<ByteString> trie;
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

	private static ByteTrieNode<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		PreByteTrieNode<ByteString> trie = new PreByteTrieNode<>();
		for (byte[] pattern : bytepatterns) {
			PreByteTrieNode<ByteString> node = trie.extend(revert(pattern), 0);
			node.setAttached(new ByteString(pattern, charset));
		}
		return new ByteTrieNodeCompiler<ByteString>(false).compileAndLink(trie);
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(bytes, options);
		} else {
			return new NextMatchFinder(bytes, options);
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

	private class NextMatchFinder extends Finder {

		public NextMatchFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
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
				ByteTrieNode<ByteString> node = trie.nextNode(current);
				while (node != null) {
					ByteString match = node.getAttached();
					if (match != null) {
						long start = bytes.current() + patternPointer;
						long end = bytes.current() + patternPointer + match.length();
						push(createMatch(start, end));
					}
					patternPointer--;
					if (pos + patternPointer < 0) {
						break;
					}
					node = node.nextNode(bytes.lookahead(patternPointer));
				}
				bytes.forward(byteShift.getShift(current));
				if (!isBufferEmpty()) {
					return leftMost();
				}
			}
			return null;
		}

	}

	private abstract class Finder extends BufferedStringFinder {

		protected ByteProvider bytes;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
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

	private class LongestMatchFinder extends Finder {

		public LongestMatchFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			int lookahead = minLength - 1;
			while (!bytes.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = bytes.current();
				byte current = bytes.lookahead(patternPointer);
				ByteTrieNode<ByteString> node = trie.nextNode(current);
				while (node != null) {
					ByteString match = node.getAttached();
					if (match != null) {
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
					node = node.nextNode(bytes.lookahead(patternPointer));
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
