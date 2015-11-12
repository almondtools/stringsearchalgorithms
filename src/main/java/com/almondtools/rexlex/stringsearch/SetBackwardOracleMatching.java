package com.almondtools.rexlex.stringsearch;

import static java.util.Arrays.copyOfRange;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.almondtools.rexlex.io.CharProvider;

public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private TrieRoot trie;
	private int minLength;
	private Map<Trie, List<String>> terminals;

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

	private static TrieRoot computeTrie(List<char[]> charpatterns, int length) {
		TrieRoot trie = new TrieRoot();
		for (char[] pattern : charpatterns) {
			char[] prefix = copyOfRange(pattern, 0, length);
			trie.extendReverse(prefix, pattern.length == prefix.length);
		}
		computeOracle(trie, length);
		return trie;
	}

	private static void computeOracle(TrieRoot trie, int length) {
		final Map<Trie, Trie> oracle = new IdentityHashMap<Trie, Trie>();
		final Trie init = trie;
		oracle.put(init, null);
		trie.apply(new TrieVisitor<Trie>() {

			@Override
			public void visitRoot(TrieRoot trie, Trie parent) {
				visit(trie, parent);
			}

			@Override
			public void visitNode(TrieNode trie, Trie parent) {
				visit(trie, parent);
			}

			private void visit(Trie trie, Trie parent) {
				List<? extends Trie> nexts = new ArrayList<Trie>(trie.getNexts());
				if (parent != null && trie instanceof TrieNode) {
					TrieNode node = (TrieNode) trie;
					char c = node.getChar();
					Trie down = oracle.get(parent);
					while (down != null && down.nextNode(c) == null) {
						down.addNext(node);
						down = oracle.get(down);
					}
					if (down != null) {
						Trie next = down.nextNode(c);
						oracle.put(trie, next);
					} else {
						oracle.put(trie, init);
					}
				}
				for (Trie next : nexts) {
					next.apply(this, trie);
				}
			}

		}, null);
	}

	private Map<Trie, List<String>> computeTerminals(TrieRoot trie, List<char[]> patterns, int minLength) {
		final Map<Trie, List<String>> terminals = new IdentityHashMap<Trie, List<String>>();
		for (char[] pattern : patterns) {
			String stringPattern = new String(pattern);
			String prefix = stringPattern.substring(0, minLength);
			Trie terminal = trie.nextNode(TrieRoot.revert(prefix.toCharArray()));
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

	private class Finder implements StringFinder {

		private CharProvider chars;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.buffer = new LinkedList<StringMatch>();
		}

		@Override
		public void skipTo(int pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove(0);
			}
			final int lookahead = minLength - 1;
			next: while (!chars.finished(lookahead)) {
				Trie current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(chars.lookahead(j));
					j--;
				}
				int currentWindowStart = chars.current();
				int currentPos = currentWindowStart + j + 1;
				int currentWindowEnd = currentWindowStart + minLength;
				String matchedPrefix = chars.slice(currentPos, currentWindowEnd);
				if (current != null && j < 0) {
					List<String> patterns = terminals.get(current);
					Iterator<String> iPatterns = patterns.iterator();
					String prefix = iPatterns.next();
					if (prefix.equals(matchedPrefix)) {
						while (iPatterns.hasNext()) {
							String suffix = iPatterns.next();
							if (!chars.finished(suffix.length())) {
								int currentWordEnd = currentWindowEnd + suffix.length();
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

		@Override
		public List<StringMatch> findAll() {
			List<StringMatch> matches = new ArrayList<StringMatch>();
			while (true) {
				StringMatch match = findNext();
				if (match == null) {
					return matches;
				} else {
					matches.add(match);
				}
			}
		}

	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new SetBackwardOracleMatching(patterns);
		}

	}

}
