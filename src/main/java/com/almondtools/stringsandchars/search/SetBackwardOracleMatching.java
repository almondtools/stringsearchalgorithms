package com.almondtools.stringsandchars.search;

import static java.util.Arrays.copyOfRange;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private TrieNode trie;
	private int minLength;
	private Map<TrieNode, List<String>> terminals;

	public SetBackwardOracleMatching(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.minLength = minLength(charpatterns);
		this.trie = computeTrie(charpatterns, minLength);
		this.terminals = computeTerminals(trie, charpatterns, minLength);
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

	private static TrieNode computeTrie(List<char[]> charpatterns, int length) {
		TrieNode trie = new TrieNode();
		for (char[] pattern : charpatterns) {
			char[] prefix = copyOfRange(pattern, 0, length);
			boolean terminate = pattern.length == prefix.length;
			TrieNode node = trie.extendReverse(prefix, 0);
			if (terminate) {
				node.setMatch(new String(pattern));
			}
		}
		computeOracle(trie, length);
		return trie;
	}

	private static void computeOracle(TrieNode trie, int length) {
		Map<TrieNode, TrieNode> oracle = new IdentityHashMap<>();
		TrieNode init = trie;
		oracle.put(init, null);
		Queue<TrieNode> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			TrieNode current = worklist.remove();
			List<TrieNode> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<TrieNode> process(TrieNode parent, Map<TrieNode, TrieNode> oracle, TrieNode init) {
		List<TrieNode> nexts = new ArrayList<>();
		for (Map.Entry<Character,TrieNode> entry : parent.getNexts().entrySet()) {
			char c = entry.getKey();
			TrieNode trie = entry.getValue();
			
			TrieNode down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c,trie);
				down = oracle.get(down);
			}
			if (down != null) {
				TrieNode next = down.nextNode(c);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}
			
			nexts.add(trie);
		}
		return nexts;
	}

	private Map<TrieNode, List<String>> computeTerminals(TrieNode trie, List<char[]> patterns, int minLength) {
		final Map<TrieNode, List<String>> terminals = new IdentityHashMap<TrieNode, List<String>>();
		for (char[] pattern : patterns) {
			String stringPattern = new String(pattern);
			String prefix = stringPattern.substring(0, minLength);
			TrieNode terminal = trie.nextNode(TrieNode.revert(prefix.toCharArray()));
			List<String> terminalPatterns = terminals.get(terminal);
			if (terminalPatterns == null) {
				terminalPatterns = new ArrayList<String>();
				terminalPatterns.add(prefix);
				terminals.put(terminal, terminalPatterns);
			}
			terminalPatterns.add(stringPattern.substring(minLength));
		}
		return terminals;
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}
	
	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.buffer = new LinkedList<StringMatch>();
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
			final int lookahead = minLength - 1;
			next: while (!chars.finished(lookahead)) {
				TrieNode current = trie;
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
					List<String> patterns = terminals.get(current);
					Iterator<String> iPatterns = patterns.iterator();
					String prefix = iPatterns.next();
					if (prefix.equals(matchedPrefix)) {
						while (iPatterns.hasNext()) {
							String suffix = iPatterns.next();
							long currentWordEnd = currentWindowEnd + suffix.length();
							if (!chars.finished((int) (currentWordEnd - currentWindowStart - 1))) {
								if (chars.slice(currentWindowEnd, currentWordEnd).equals(suffix)) {
									buffer.add(new StringMatch(currentWindowStart, currentWordEnd, prefix + suffix));
								}
							}
						}
						chars.next();
						if (buffer.isEmpty()) {
							continue next;
						} else {
							return buffer.remove(0);
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

	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new SetBackwardOracleMatching(patterns);
		}

	}

}
