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
import net.amygdalum.util.map.CharObjectMap.Entry;
import net.amygdalum.util.text.CharMapping;
import net.amygdalum.util.tries.CharTrieNode;
import net.amygdalum.util.tries.CharTrieNodeCompiler;
import net.amygdalum.util.tries.PreCharTrieNode;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private CharTrieNode<char[]> trie;
	private int patternLength;

	public BOM(String pattern) {
		this(pattern, CharMapping.IDENTITY);
	}

	public BOM(String pattern, CharMapping mapping) {
		this.patternLength = pattern.length();
		this.trie = computeTrie(mapping.normalized(pattern.toCharArray()), patternLength, mapping);
	}

	private static <T> void applyMapping(CharMapping mapping, PreCharTrieNode<char[]> trie) {
		Set<PreCharTrieNode<char[]>> nodes = trie.nodes();
		for (PreCharTrieNode<char[]> node : nodes) {
			applyMapping(node, mapping);
		}
	}

	private static void applyMapping(PreCharTrieNode<char[]> node, CharMapping mapping) {
		CharObjectMap<PreCharTrieNode<char[]>> nexts = node.getNexts();
		node.reset();
		for (Entry<PreCharTrieNode<char[]>> entry : nexts.cursor()) {
			char ec = entry.key;
			PreCharTrieNode<char[]> next = entry.value;
			for (char c : mapping.map(ec)) {
				node.addNext(c, next);
			}
		}
	}

	private static CharTrieNode<char[]> computeTrie(char[] pattern, int length, CharMapping mapping) {
		PreCharTrieNode<char[]> trie = new PreCharTrieNode<>();
		PreCharTrieNode<char[]> node = trie.extend(revert(pattern), 0);
		node.setAttached(pattern);
		computeOracle(trie);
		if (mapping != CharMapping.IDENTITY) {
			applyMapping(mapping, trie);
		}
		return new CharTrieNodeCompiler<char[]>(false).compileAndLink(trie);
	}

	private static void computeOracle(PreCharTrieNode<char[]> trie) {
		Map<PreCharTrieNode<char[]>, PreCharTrieNode<char[]>> oracle = new IdentityHashMap<>();
		PreCharTrieNode<char[]> init = trie;
		oracle.put(init, null);
		Queue<PreCharTrieNode<char[]>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreCharTrieNode<char[]> current = worklist.remove();
			List<PreCharTrieNode<char[]>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<PreCharTrieNode<char[]>> process(PreCharTrieNode<char[]> parent, Map<PreCharTrieNode<char[]>, PreCharTrieNode<char[]>> oracle, PreCharTrieNode<char[]> init) {
		List<PreCharTrieNode<char[]>> nexts = new ArrayList<>();
		for (Entry<PreCharTrieNode<char[]>> entry : parent.getNexts().cursor()) {
			char c = entry.key;
			PreCharTrieNode<char[]> trie = entry.value;

			PreCharTrieNode<char[]> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				PreCharTrieNode<char[]> next = down.nextNode(c);
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
				CharTrieNode<char[]> current = trie;
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
