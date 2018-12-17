package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.util.text.CharUtils.revert;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.map.CharObjectMap.Entry;
import net.amygdalum.util.text.CharMapping;
import net.amygdalum.util.tries.CharTrie;
import net.amygdalum.util.tries.CharTrieCursor;
import net.amygdalum.util.tries.CharTrieTreeCompiler;
import net.amygdalum.util.tries.PreCharTrieNode;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private CharTrie<char[]> trie;
	private int patternLength;

	public BOM(String pattern) {
		this(pattern, CharMapping.IDENTITY);
	}

	public BOM(String pattern, CharMapping mapping) {
		this.patternLength = pattern.length();
		this.trie = computeTrie(mapping.normalized(pattern.toCharArray()), mapping);
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

	private static CharTrie<char[]> computeTrie(char[] pattern, CharMapping mapping) {
		PreCharTrieNode<char[]> trie = new PreCharTrieNode<>();
		PreCharTrieNode<char[]> node = trie.extend(revert(pattern), 0);
		node.setAttached(pattern);
		computeOracle(trie);
		if (mapping != CharMapping.IDENTITY) {
			applyMapping(mapping, trie);
		}
		return new CharTrieTreeCompiler<char[]>(false)
			.compileAndLink(trie);
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
		return new Finder(trie, patternLength, chars, options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static class Finder extends AbstractStringFinder {

		private final int lookahead;
		private CharProvider chars;
		private CharTrieCursor<char[]> cursor;

		public Finder(CharTrie<char[]> trie, int patternLength, CharProvider chars, StringFinderOption... options) {
			super(options);
			this.lookahead = patternLength - 1;
			this.chars = chars;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished(lookahead)) {
				cursor.reset();
				int j = lookahead;
				boolean success = true;
				while (j >= 0 && success) {
					success = cursor.accept(chars.lookahead(j));
					j--;
				}
				if (success && j < 0) {
					char[] pattern = cursor.iterator().next();
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
