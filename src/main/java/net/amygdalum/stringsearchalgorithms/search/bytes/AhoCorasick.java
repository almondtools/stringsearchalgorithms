package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toByteArray;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.map.ByteObjectMap.Entry;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.tries.ByteTrie;
import net.amygdalum.util.tries.ByteTrieCursor;
import net.amygdalum.util.tries.ByteTrieTreeCompiler;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private ByteTrie<ByteString> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.trie = computeTrie(bytepatterns, charset);
		this.minLength = minLength(bytepatterns);
	}

	private static ByteTrie<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		PreByteTrieNode<ByteString> trie = new PreByteTrieNode<>();
		computeTrie(trie, bytepatterns, charset);
		computeSupportTransition(trie);
		return new ByteTrieTreeCompiler<ByteString>(false)
			.compileAndLink(trie);
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(trie, bytes, options);
		} else {
			return new NextMatchFinder(trie, bytes, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private static void computeTrie(PreByteTrieNode<ByteString> trie, List<byte[]> bytepatterns, Charset charset) {
		for (byte[] pattern : bytepatterns) {
			trie.extend(pattern, new ByteString(pattern, charset));
		}
	}

	private static void computeSupportTransition(PreByteTrieNode<ByteString> trie) {
		Queue<PreByteTrieNode<ByteString>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreByteTrieNode<ByteString> current = worklist.remove();
			for (Entry<PreByteTrieNode<ByteString>> next : current.getNexts().cursor()) {
				byte b = next.key;
				PreByteTrieNode<ByteString> nextTrie = next.value;
				PreByteTrieNode<ByteString> down = current.getLink();
				while (down != null) {
					PreByteTrieNode<ByteString> nextNode = down.nextNode(b);
					if (nextNode != null) {
						nextTrie.link(nextNode);
						break;
					}
					down = down.getLink();
				}
				if (down == null) {
					nextTrie.link(trie);
				}
				worklist.add(nextTrie);
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static abstract class Finder extends BufferedStringFinder {

		protected ByteProvider bytes;
		protected ByteTrieCursor<ByteString> cursor;

		public Finder(ByteTrie<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			cursor.reset();
			clear();
		}

		protected List<StringMatch> createMatches(long end) {
			List<StringMatch> matches = new ArrayList<>();
			for (ByteString currentMatch : cursor) {
				long start = end - currentMatch.length();
				StringMatch nextMatch = createMatch(start, end);
				if (!matches.contains(nextMatch)) {
					matches.add(nextMatch);
				}
			}
			return matches;
		}

		private StringMatch createMatch(long start, long end) {
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(ByteTrie<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(trie, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!bytes.finished()) {
				byte b = bytes.next();
				boolean success = cursor.accept(b);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(bytes.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(ByteTrie<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(trie, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte b = bytes.next();
				boolean success = cursor.lookahead(b);
				if (!success && !isBufferEmpty()) {
					bytes.prev();
					break;
				}
				success = cursor.accept(b);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(bytes.current()));
				}
			}
			return longestLeftMost();
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
			return new AhoCorasick(patterns, charset);
		}

	}
}
