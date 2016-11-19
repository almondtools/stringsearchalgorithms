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

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.CharObjectMap;

/**
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private TrieNode<List<String>> trie;
	private int minLength;

	public SetBackwardOracleMatching(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.minLength = minLength(charpatterns);
		this.trie = computeTrie(charpatterns, minLength);
	}

	private static TrieNode<List<String>> computeTrie(List<char[]> charpatterns, int length) {
		TrieNode<List<String>> trie = new TrieNode<>();
		for (char[] pattern : charpatterns) {
			char[] prefix = copyOfRange(pattern, 0, length);
			trie.extend(revert(prefix), 0);
		}
		computeOracle(trie);
		computeTerminals(trie, charpatterns, length);
		return trie;
	}

	private static void computeOracle(TrieNode<List<String>> trie) {
		Map<TrieNode<List<String>>, TrieNode<List<String>>> oracle = new IdentityHashMap<>();
		TrieNode<List<String>> init = trie;
		oracle.put(init, null);
		Queue<TrieNode<List<String>>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode<List<String>> current = worklist.remove();
			List<TrieNode<List<String>>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<TrieNode<List<String>>> process(TrieNode<List<String>> parent, Map<TrieNode<List<String>>, TrieNode<List<String>>> oracle, TrieNode<List<String>> init) {
		List<TrieNode<List<String>>> nexts = new ArrayList<>();
		for (CharObjectMap<TrieNode<List<String>>>.Entry entry : parent.getNexts().cursor()) {
			char c = entry.key;
			TrieNode<List<String>> trie = entry.value;

			TrieNode<List<String>> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				TrieNode<List<String>> next = down.nextNode(c);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}

			nexts.add(trie);
		}
		return nexts;
	}

	private static void computeTerminals(TrieNode<List<String>> trie, List<char[]> patterns, int minLength) {
		for (char[] pattern : patterns) {
			char[] prefix = Arrays.copyOfRange(pattern, 0, minLength);
			TrieNode<List<String>> terminal = trie.nextNode(revert(prefix));
			List<String> terminalPatterns = terminal.getAttached();
			if (terminalPatterns == null) {
				terminalPatterns = new ArrayList<>();
				terminal.setAttached(terminalPatterns);
				terminalPatterns.add(new String(prefix));
			}
			char[] tail = Arrays.copyOfRange(pattern, minLength, pattern.length);
			terminalPatterns.add(new String(tail));
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
				TrieNode<List<String>> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				long currentWindowStart = chars.current();
				long currentPos = currentWindowStart + j + 1;
				long currentWindowEnd = currentWindowStart + minLength;
				String matchedPrefix = chars.slice(currentPos, currentWindowEnd);
				if (current != null && j < 0) {
					List<String> patterns = current.getAttached();
					Iterator<String> iPatterns = patterns.iterator();
					String prefix = iPatterns.next();
					if (prefix.equals(matchedPrefix)) {
						while (iPatterns.hasNext()) {
							String suffix = iPatterns.next();
							long currentWordEnd = currentWindowEnd + suffix.length();
							if (!chars.finished((int) (currentWordEnd - currentWindowStart - 1))) {
								if (chars.slice(currentWindowEnd, currentWordEnd).equals(suffix)) {
									buffer.add(createMatch(currentWindowStart, currentWordEnd, prefix + suffix));
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
					chars.forward(j + 1);
				}
			}
			return null;
		}

		public StringMatch createMatch(long start, long end, String match) {
			return new StringMatch(start, end, match);
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new SetBackwardOracleMatching(patterns);
		}

	}

}
