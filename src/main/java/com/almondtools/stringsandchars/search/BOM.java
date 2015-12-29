package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private TrieNode<String> trie;
	private int patternLength;

	public BOM(String pattern) {
		this.patternLength = pattern.length();
		this.trie = computeTrie(pattern.toCharArray(), patternLength);
	}

	private static TrieNode<String> computeTrie(char[] pattern, int length) {
		TrieNode<String> trie = new TrieNode<>();
		TrieNode<String> node = trie.extendReverse(pattern, 0);
		node.setMatch(new String(pattern));
		computeOracle(trie);
		return trie;
	}

	private static void computeOracle(TrieNode<String> trie) {
		Map<TrieNode<String>, TrieNode<String>> oracle = new IdentityHashMap<>();
		TrieNode<String> init = trie;
		oracle.put(init, null);
		Queue<TrieNode<String>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<String> current = worklist.remove();
			List<TrieNode<String>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<TrieNode<String>> process(TrieNode<String> parent, Map<TrieNode<String>, TrieNode<String>> oracle, TrieNode<String> init) {
		List<TrieNode<String>> nexts = new ArrayList<>();
		for (Map.Entry<Character,TrieNode<String>> entry : parent.getNexts().entrySet()) {
			char c = entry.getKey();
			TrieNode<String> trie = entry.getValue();
			
			TrieNode<String> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c,trie);
				down = oracle.get(down);
			}
			if (down != null) {
				TrieNode<String> next = down.nextNode(c);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}
			
			nexts.add(trie);
		}
		return nexts;
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return new Finder(chars, options);
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.buffer = new LinkedList<>();
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
			buffer.clear();
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove(0);
			}
			final int lookahead = patternLength - 1;
			next: while (!chars.finished(lookahead)) {
				TrieNode<String> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				if (current != null && j < 0) {
					String pattern = current.getMatch();
					long currentWindowStart = chars.current();
					buffer.add(createMatch(currentWindowStart, pattern));

					chars.next();
					if (buffer.isEmpty()) {
						continue next;
					} else {
						return buffer.remove(0);
					}

				}
				if (j <= 0) {
					chars.next();
				} else {
					chars.forward(j + 1);
				}
			}
			return null;
		}

		public StringMatch createMatch(long start, String match) {
			return new StringMatch(start, start + match.length(), match);
		}

	}

	public static class Factory implements WordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BOM(pattern);
		}

	}

}
