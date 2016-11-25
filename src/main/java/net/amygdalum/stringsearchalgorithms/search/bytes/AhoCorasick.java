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

import net.amygdalum.stringsearchalgorithms.io.ByteProvider;
import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.ByteObjectMap;
import net.amygdalum.util.text.ByteString;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private TrieNode<ByteString> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.trie = computeTrie(bytepatterns, charset);
		this.minLength = minLength(bytepatterns);
	}

	private static TrieNode<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		TrieNode<ByteString> trie = new TrieNode<>();
		for (byte[] pattern : bytepatterns) {
			trie.extend(pattern, new ByteString(pattern, charset));
		}
		return computeSupportTransition(trie);
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

	private static TrieNode<ByteString> computeSupportTransition(TrieNode<ByteString> trie) {
		Queue<TrieNode<ByteString>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<ByteString> current = worklist.remove();
			for (ByteObjectMap<TrieNode<ByteString>>.Entry next : current.getNexts().cursor()) {
				TrieNode<ByteString> nextTrie = next.value;
				computeSupport(current, next.key, nextTrie, trie);
				worklist.add(nextTrie);
			}
		}
		return trie;
	}

	private static void computeSupport(TrieNode<ByteString> parent, byte b, TrieNode<ByteString> trie, TrieNode<ByteString> init) {
		if (parent != null && trie instanceof TrieNode) {
			TrieNode<ByteString> down = parent.getFallback();
			while (down != null && down.nextNode(b) == null) {
				down = down.getFallback();
			}
			if (down != null) {
				TrieNode<ByteString> next = down.nextNode(b);
				trie.addFallback(next);
				ByteString nextMatch = next.getAttached();
				if (nextMatch != null && trie.getAttached() == null) {
					trie.setAttached(nextMatch);
				}
			} else {
				trie.addFallback(init);
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends BufferedStringFinder {
		protected ByteProvider bytes;
		protected TrieNode<ByteString> current;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
			this.current = trie;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			current = trie;
			clear();
		}

		protected List<StringMatch> createMatches(TrieNode<ByteString> current, long end) {
			List<StringMatch> matches = new ArrayList<>();
			while (current != null) {
				ByteString currentMatch = current.getAttached();
				if (currentMatch != null) {
					long start = end - currentMatch.length(); 
					StringMatch nextMatch = createMatch(start, end);
					if (!matches.contains(nextMatch)) {
						matches.add(nextMatch);
					}
				}
				current = current.getFallback();
			}
			return matches;
		}

		private StringMatch createMatch(long start, long end) {
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

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
			while (!bytes.finished()) {
				byte b = bytes.next();
				TrieNode<ByteString> next = current.nextNode(b);
				while (next == null) {
					TrieNode<ByteString> nextcurrent = current.getFallback();
					if (nextcurrent == null) {
						break;
					}
					current = nextcurrent;
					next = current.nextNode(b);
				}
				if (next != null) {
					current = next;
				} else {
					current = trie;
				}
				if (current.getAttached() != null) {
					push(createMatches(current, bytes.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private class LongestMatchFinder extends Finder {

		public LongestMatchFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte b = bytes.next();
				TrieNode<ByteString> next = current.nextNode(b);
				if (next == null && !isBufferEmpty()) {
					bytes.prev();
					break;
				}
				while (next == null) {
					TrieNode<ByteString> nextcurrent = current.getFallback();
					if (nextcurrent == null) {
						break;
					}
					current = nextcurrent;
					next = current.nextNode(b);
				}
				if (next != null) {
					current = next;
				} else {
					current = trie;
				}
				if (current.getAttached() != null) {
					push(createMatches(current, bytes.current()));
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
