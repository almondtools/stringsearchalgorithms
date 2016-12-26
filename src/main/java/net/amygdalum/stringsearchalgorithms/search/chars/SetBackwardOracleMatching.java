package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.copyOfRange;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.CharUtils.revert;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private CharMapping mapping;
	private CharTrieNode<List<char[]>> trie;
	private int minLength;

	public SetBackwardOracleMatching(Collection<String> patterns) {
		this(patterns, CharMapping.IDENTITY);
	}

	public SetBackwardOracleMatching(Collection<String> patterns, CharMapping mapping) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.mapping = mapping;
		this.minLength = minLength(charpatterns);
		this.trie = computeTrie(normalized(mapping, charpatterns), minLength, mapping);
	}

	private List<char[]> normalized(CharMapping mapping, List<char[]> charpatterns) {
		List<char[]> normalized = new ArrayList<>(charpatterns.size());
		for (char[] cs : charpatterns) {
			normalized.add(mapping.normalized(cs));
		}
		return normalized;
	}

	private static void applyMapping(CharMapping mapping, PreCharTrieNode<List<char[]>> trie) {
		Set<PreCharTrieNode<List<char[]>>> nodes = trie.nodes();
		for (PreCharTrieNode<List<char[]>> node : nodes) {
			applyMapping(node, mapping);
		}
	}

	private static void applyMapping(PreCharTrieNode<List<char[]>> node, CharMapping mapping) {
		CharObjectMap<PreCharTrieNode<List<char[]>>> nexts = node.getNexts();
		node.reset();
		for (Entry<PreCharTrieNode<List<char[]>>> entry : nexts.cursor()) {
			char ec = entry.key;
			PreCharTrieNode<List<char[]>> next = entry.value;
			for (char c : mapping.map(ec)) {
				node.addNext(c, next);
			}
		}
	}

	private static CharTrieNode<List<char[]>> computeTrie(List<char[]> charpatterns, int length, CharMapping mapping) {
		PreCharTrieNode<List<char[]>> trie = new PreCharTrieNode<>();
		for (char[] pattern : charpatterns) {
			char[] prefix = copyOfRange(pattern, 0, length);
			trie.extend(revert(prefix), 0);
		}
		computeOracle(trie);
		computeTerminals(trie, charpatterns, length);
		if (mapping != CharMapping.IDENTITY) {
			applyMapping(mapping, trie); 
		}
		return new CharTrieNodeCompiler<List<char[]>>(false).compileAndLink(trie);
	}

	private static void computeOracle(PreCharTrieNode<List<char[]>> trie) {
		Map<PreCharTrieNode<List<char[]>>, PreCharTrieNode<List<char[]>>> oracle = new IdentityHashMap<>();
		PreCharTrieNode<List<char[]>> init = trie;
		oracle.put(init, null);
		Queue<PreCharTrieNode<List<char[]>>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreCharTrieNode<List<char[]>> current = worklist.remove();
			List<PreCharTrieNode<List<char[]>>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<PreCharTrieNode<List<char[]>>> process(PreCharTrieNode<List<char[]>> parent, Map<PreCharTrieNode<List<char[]>>, PreCharTrieNode<List<char[]>>> oracle, PreCharTrieNode<List<char[]>> init) {
		List<PreCharTrieNode<List<char[]>>> nexts = new ArrayList<>();
		for (Entry<PreCharTrieNode<List<char[]>>> entry : parent.getNexts().cursor()) {
			char c = entry.key;
			PreCharTrieNode<List<char[]>> trie = entry.value;

			PreCharTrieNode<List<char[]>> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				PreCharTrieNode<List<char[]>> next = down.nextNode(c);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}

			nexts.add(trie);
		}
		return nexts;
	}

	private static void computeTerminals(PreCharTrieNode<List<char[]>> trie, List<char[]> patterns, int minLength) {
		for (char[] pattern : patterns) {
			char[] prefix = Arrays.copyOfRange(pattern, 0, minLength);
			PreCharTrieNode<List<char[]>> terminal = trie.nextNode(revert(prefix));
			List<char[]> terminalPatterns = terminal.getAttached();
			if (terminalPatterns == null) {
				terminalPatterns = new ArrayList<>();
				terminal.setAttached(terminalPatterns);
				terminalPatterns.add(prefix);
			}
			char[] tail = Arrays.copyOfRange(pattern, minLength, pattern.length);
			terminalPatterns.add(tail);
		}
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return new Finder(chars, options);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private Queue<StringMatch> buffer;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.buffer = new LinkedList<>();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			buffer.clear();
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove();
			}
			final int lookahead = minLength - 1;
			next: while (!chars.finished(lookahead)) {
				CharTrieNode<List<char[]>> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				long currentWindowStart = chars.current();
				long currentPos = currentWindowStart + j + 1;
				long currentWindowEnd = currentWindowStart + minLength;
				char[] matchedPrefix = chars.between(currentPos, currentWindowEnd);
				if (current != null && j < 0) {
					List<char[]> patterns = current.getAttached();
					Iterator<char[]> iPatterns = patterns.iterator();
					char[] prefix = iPatterns.next();
					if (Arrays.equals(prefix, mapping.normalized(matchedPrefix))) {
						while (iPatterns.hasNext()) {
							char[] suffix = iPatterns.next();
							long currentWordEnd = currentWindowEnd + suffix.length;
							if (!chars.finished((int) (currentWordEnd - currentWindowStart - 1))) {
								char[] matchedSuffix = chars.between(currentWindowEnd, currentWordEnd);
								if (Arrays.equals(suffix, mapping.normalized(matchedSuffix))) {
									buffer.add(createMatch(currentWindowStart, currentWordEnd));
								}
							}
						}
						chars.next();
						if (buffer.isEmpty()) {
							continue next;
						} else {
							return buffer.remove();
						}
					}

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

	public static class Factory implements MultiStringSearchAlgorithmFactory, SupportsCharClasses {

		private CharMapping mapping;

		@Override
		public void enableCharClasses(CharMapping mapping) {
			this.mapping = mapping;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			if (mapping == null) {
				return new SetBackwardOracleMatching(patterns);
			} else {
				return new SetBackwardOracleMatching(patterns, mapping);
			}
		}

	}

}
