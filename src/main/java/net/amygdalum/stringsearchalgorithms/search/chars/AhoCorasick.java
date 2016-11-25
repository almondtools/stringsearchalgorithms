package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.CharObjectMap;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private TrieNode<String> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private static TrieNode<String> computeTrie(List<char[]> charpatterns) {
		TrieNode<String> trie = new TrieNode<>();
		for (char[] pattern : charpatterns) {
			trie.extend(pattern, new String(pattern));
		}
		return computeSupportTransition(trie);
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(chars, options);
		} else {
			return new NextMatchFinder(chars, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private static TrieNode<String> computeSupportTransition(TrieNode<String> trie) {
		Queue<TrieNode<String>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<String> current = worklist.remove();
			for (CharObjectMap<TrieNode<String>>.Entry next : current.getNexts().cursor()) {
				TrieNode<String> nextTrie = next.value;
				computeSupport(current, next.key, nextTrie, trie);
				worklist.add(nextTrie);
			}
		}
		return trie;
	}

	private static void computeSupport(TrieNode<String> parent, char c, TrieNode<String> trie, TrieNode<String> init) {
		if (parent != null && trie instanceof TrieNode) {
			TrieNode<String> down = parent.getFallback();
			while (down != null && down.nextNode(c) == null) {
				down = down.getFallback();
			}
			if (down != null) {
				TrieNode<String> next = down.nextNode(c);
				trie.addFallback(next);
				String nextMatch = next.getAttached();
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
		protected CharProvider chars;
		protected TrieNode<String> current;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.current = trie;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			current = trie;
			clear();
		}

		protected List<StringMatch> createMatches(TrieNode<String> current, long end) {
			List<StringMatch> matches = new ArrayList<>();
			while (current != null) {
				String currentMatch = current.getAttached();
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
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private class NextMatchFinder extends Finder {

		public NextMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!chars.finished()) {
				char c = chars.next();
				TrieNode<String> next = current.nextNode(c);
				while (next == null) {
					TrieNode<String> nextcurrent = current.getFallback();
					if (nextcurrent == null) {
						break;
					}
					current = nextcurrent;
					next = current.nextNode(c);
				}
				if (next != null) {
					current = next;
				} else {
					current = trie;
				}
				if (current.getAttached() != null) {
					push(createMatches(current, chars.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private class LongestMatchFinder extends Finder {

		public LongestMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char c = chars.next();
				TrieNode<String> next = current.nextNode(c);
				if (next == null && !isBufferEmpty()) {
					chars.prev();
					break;
				}
				while (next == null) {
					TrieNode<String> nextcurrent = current.getFallback();
					if (nextcurrent == null) {
						break;
					}
					current = nextcurrent;
					next = current.nextNode(c);
				}
				if (next != null) {
					current = next;
				} else {
					current = trie;
				}
				if (current.getAttached() != null) {
					push(createMatches(current, chars.current()));
				}
			}
			return longestLeftMost();
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new AhoCorasick(patterns);
		}

	}
}
