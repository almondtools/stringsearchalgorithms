package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.util.text.CharUtils.revert;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.text.CharMapping;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private TrieNode<char[]> trie;
	private int patternLength;

	public BOM(String pattern) {
		this(pattern, CharMapping.IDENTITY);
	}

	public BOM(String pattern, CharMapping mapping) {
		this.patternLength = pattern.length();
		this.trie = computeTrie(mapping.normalized(pattern.toCharArray()), patternLength);
		if (mapping != CharMapping.IDENTITY) {
			applyMapping(mapping);
		}
	}

	private void applyMapping(CharMapping mapping) {
		Set<TrieNode<char[]>> nodes = trie.nodes();
		for (TrieNode<char[]> node : nodes) {
			applyMapping(node, mapping);
		}
	}

	private void applyMapping(TrieNode<char[]> node, CharMapping mapping) {
		CharObjectMap<TrieNode<char[]>> nexts = node.getNexts();
		node.reset();
		for (CharObjectMap<TrieNode<char[]>>.Entry entry : nexts.cursor()) {
			char ec = entry.key;
			TrieNode<char[]> next = entry.value;
			for (char c : mapping.map(ec)) {
				node.addNext(c, next);
			}
		}
	}

	private static TrieNode<char[]> computeTrie(char[] pattern, int length) {
		TrieNode<char[]> trie = new TrieNode<>();
		TrieNode<char[]> node = trie.extend(revert(pattern), 0);
		node.setAttached(pattern);
		computeOracle(trie);
		return trie;
	}

	private static void computeOracle(TrieNode<char[]> trie) {
		Map<TrieNode<char[]>, TrieNode<char[]>> oracle = new IdentityHashMap<>();
		TrieNode<char[]> init = trie;
		oracle.put(init, null);
		Queue<TrieNode<char[]>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<char[]> current = worklist.remove();
			List<TrieNode<char[]>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<TrieNode<char[]>> process(TrieNode<char[]> parent, Map<TrieNode<char[]>, TrieNode<char[]>> oracle, TrieNode<char[]> init) {
		List<TrieNode<char[]>> nexts = new ArrayList<>();
		for (CharObjectMap<TrieNode<char[]>>.Entry entry : parent.getNexts().cursor()) {
			char c = entry.key;
			TrieNode<char[]> trie = entry.value;

			TrieNode<char[]> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				TrieNode<char[]> next = down.nextNode(c);
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
				TrieNode<char[]> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				if (current != null && j < 0) {
					char[] pattern = current.getAttached();
					long start = chars.current();
					long end = start + pattern.length;
					StringMatch match = createMatch(start, end);

					chars.next();
					return match;
				}
				if (j <= 0) {
					chars.next();
				} else {
					chars.forward(j + 2);
				}
			}
			return null;
		}

		private StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory, SupportsCharClasses {

		private CharMapping mapping;

		@Override
		public void enableCharClasses(CharMapping mapping) {
			this.mapping = mapping;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			if (mapping == null) {
				return new BOM(pattern);
			} else {
				return new BOM(pattern, mapping);
			}
		}

	}

}
