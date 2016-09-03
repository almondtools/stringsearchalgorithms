package net.amygdalum.stringsearchalgorithms.search;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private TrieNode<Void> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private static TrieNode<Void> computeTrie(List<char[]> charpatterns) {
		TrieNode<Void> trie = new TrieNode<>();
		for (char[] pattern : charpatterns) {
			trie.extend(pattern);
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

	private static TrieNode<Void> computeSupportTransition(TrieNode<Void> trie) {
		Queue<TrieNode<Void>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<Void> current = worklist.remove();
			for (Map.Entry<Character, TrieNode<Void>> next : current.getNexts().entrySet()) {
				TrieNode<Void> nextTrie = next.getValue();
				computeSupport(current, next.getKey(), nextTrie, trie);
				worklist.add(nextTrie);
			}
		}
		return trie;
	}

	private static void computeSupport(TrieNode<Void> parent, char c, TrieNode<Void> trie, TrieNode<Void> init) {
		if (parent != null && trie instanceof TrieNode) {
			TrieNode<Void> down = parent.getFallback();
			while (down != null && down.nextNode(c) == null) {
				down = down.getFallback();
			}
			if (down != null) {
				TrieNode<Void> next = down.nextNode(c);
				trie.addFallback(next);
				String nextMatch = next.getMatch();
				if (nextMatch != null && trie.getMatch() == null) {
					trie.setMatch(nextMatch);
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
		protected TrieNode<Void> current;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.current = trie;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
			current = trie;
			clear();
		}

		protected List<StringMatch> createMatches(TrieNode<Void> current, long end) {
			List<StringMatch> matches = new ArrayList<>();
			while (current != null) {
				String currentMatch = current.getMatch();
				if (currentMatch != null) {
					StringMatch nextMatch = createMatch(end, currentMatch);
					if (!matches.contains(nextMatch)) {
						matches.add(nextMatch);
					}
				}
				current = current.getFallback();
			}
			return matches;
		}

		protected StringMatch createMatch(long end, String s) {
			long start = end - s.length();
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
				TrieNode<Void> next = current.nextNode(c);
				while (next == null) {
					TrieNode<Void> nextcurrent = current.getFallback();
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
				if (current.getMatch() != null) {
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
				TrieNode<Void> next = current.nextNode(c);
				if (next == null && !isBufferEmpty()) {
					chars.prev();
					break;
				}
				while (next == null) {
					TrieNode<Void> nextcurrent = current.getFallback();
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
				if (current.getMatch() != null) {
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
