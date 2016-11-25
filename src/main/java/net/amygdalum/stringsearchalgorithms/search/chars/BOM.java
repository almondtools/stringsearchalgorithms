package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.util.text.CharUtils.revert;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.CharObjectMap;

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
		TrieNode<String> node = trie.extend(revert(pattern), 0);
		node.setAttached(new String(pattern));
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
		for (CharObjectMap<TrieNode<String>>.Entry entry : parent.getNexts().cursor()) {
			char c = entry.key;
			TrieNode<String> trie = entry.value;

			TrieNode<String> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
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

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
		}

		@Override
		public StringMatch findNext() {
			final int lookahead = patternLength - 1;
			while (!chars.finished(lookahead)) {
				TrieNode<String> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				if (current != null && j < 0) {
					String pattern = current.getAttached();
					long start = chars.current();
					long end = start + pattern.length();
					StringMatch match = createMatch(start, end);

					chars.next();
					return match;
				}
				if (j <= 0) {
					chars.next();
				} else {
					chars.forward(j + 1);
				}
			}
			return null;
		}

		public StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BOM(pattern);
		}

	}

}
