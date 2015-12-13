package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private TrieNode<Void> trie;
	private int minLength;
	
	public AhoCorasick(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private int minLength(List<char[]> patterns) {
		int len = Integer.MAX_VALUE;
		for (char[] pattern : patterns) {
			if (pattern.length < len) {
				len = pattern.length;
			}
		}
		return len;
	}

	private List<char[]> toCharArray(List<String> patterns) {
		List<char[]> charpatterns = new ArrayList<char[]>(patterns.size());
		for (String pattern : patterns) {
			charpatterns.add(pattern.toCharArray());
		}
		return charpatterns;
	}

	private static TrieNode<Void> computeTrie(List<char[]> charpatterns) {
		TrieNode<Void> trie = new TrieNode<>();
		for (char[] pattern : charpatterns) {
			trie.extend(pattern);
		}
		return computeSupportTransition(trie);
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
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

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private TrieNode<Void> current;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.current = trie;
			this.buffer = new LinkedList<>();
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
		}
		
		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove(0);
			}
			while (!chars.finished()) {
				char c = chars.next();
				TrieNode<Void> next = current.nextNode(c);
				while(next == null) {
					TrieNode<Void> nextcurrent= current.getFallback();
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
					buffer = createMatches(current, chars.current());
					return buffer.remove(0);
				}
			}
			return null;
		}

		private List<StringMatch> createMatches(TrieNode<Void> current, long end) {
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

		private StringMatch createMatch(long end, String s) {
			long start = end - s.length();
			return new StringMatch(start, end, s);
		}

	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new AhoCorasick(patterns);
		}

	}
}
