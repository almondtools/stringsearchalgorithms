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
import net.amygdalum.util.map.ByteObjectMap.Entry;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.tries.ByteTrieNode;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private ByteTrieNode<ByteString> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.trie = computeTrie(bytepatterns, charset);
		this.minLength = minLength(bytepatterns);
	}

	private static ByteTrieNode<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		PreByteTrieNode<ByteString> trie = new PreByteTrieNode<>();
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

	private static ByteTrieNode<ByteString> computeSupportTransition(PreByteTrieNode<ByteString> trie) {
		Queue<PreByteTrieNode<ByteString>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreByteTrieNode<ByteString> current = worklist.remove();
			for (Entry<PreByteTrieNode<ByteString>> next : current.getNexts().cursor()) {
				PreByteTrieNode<ByteString> nextTrie = next.value;
				computeSupport(current, next.key, nextTrie, trie);
				worklist.add(nextTrie);
			}
		}
		return trie.compile();
	}

	private static void computeSupport(PreByteTrieNode<ByteString> parent, byte b, PreByteTrieNode<ByteString> trie, PreByteTrieNode<ByteString> init) {
		if (parent != null) {
			PreByteTrieNode<ByteString> down = parent.getLink();
			while (down != null && down.nextNode(b) == null) {
				down = down.getLink();
			}
			if (down != null) {
				PreByteTrieNode<ByteString> next = down.nextNode(b);
				trie.link(next);
				ByteString nextMatch = next.getAttached();
				if (nextMatch != null && trie.getAttached() == null) {
					trie.setAttached(nextMatch);
				}
			} else {
				trie.link(init);
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends BufferedStringFinder {
		protected ByteProvider bytes;
		protected ByteTrieNode<ByteString> current;

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

		protected List<StringMatch> createMatches(ByteTrieNode<ByteString> current, long end) {
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
				current = current.getLink();
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
				ByteTrieNode<ByteString> next = current.nextNode(b);
				while (next == null) {
					ByteTrieNode<ByteString> nextcurrent = current.getLink();
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
				ByteTrieNode<ByteString> next = current.nextNode(b);
				if (next == null && !isBufferEmpty()) {
					bytes.prev();
					break;
				}
				while (next == null) {
					ByteTrieNode<ByteString> nextcurrent = current.getLink();
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
