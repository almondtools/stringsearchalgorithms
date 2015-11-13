package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.almondtools.stringsandchars.io.CharProvider;

public class AhoCorasick implements StringSearchAlgorithm {

	private TrieRoot trie;
	private int minLength;
	private Map<Trie,Trie> support;
	
	public AhoCorasick(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
		this.support = computeSupportTransition(trie);
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

	private static TrieRoot computeTrie(List<char[]> charpatterns) {
		TrieRoot trie = new TrieRoot();
		for (char[] pattern : charpatterns) {
			trie.extend(pattern);
		}
		return trie;
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private static Map<Trie, Trie> computeSupportTransition(TrieRoot trie) {
		final Map<Trie, Trie> support = new IdentityHashMap<Trie, Trie>();
		final Trie init = trie;
		support.put(init, null);
		TrieVisitor<Trie> visitor = new TrieVisitor<Trie>() {

			@Override
			public void visitRoot(TrieRoot trie, Trie parent) {
				visit(trie, parent);
			}

			@Override
			public void visitNode(TrieNode trie, Trie parent) {
				visit(trie, parent);
			}

			private void visit(Trie trie, Trie parent) {
				if (parent != null && trie instanceof TrieNode) {
					char c = ((TrieNode) trie).getChar();
					Trie down = support.get(parent);
					while (down != null && down.nextNode(c) == null) {
						down = support.get(down);
					}
					if (down != null) {
						Trie next = down.nextNode(c);
						support.put(trie, next);
						if (next.isTerminal() && !trie.isTerminal()) {
							trie.setTerminal(next.length());
						}
					} else {
						support.put(trie, init);
					}
				}
			}
			
		};
		final List<TrieTrie> worklist = new LinkedList<TrieTrie>();
		worklist.add(new TrieTrie(init, null));
		while (!worklist.isEmpty()) {
			TrieTrie current = worklist.remove(0);
			Trie currentTrie = current.getTrie();
			Trie currentParent = current.getParent();
			for (Trie next : currentTrie.getNexts()) {
				worklist.add(new TrieTrie(next, currentTrie));
			}
			currentTrie.apply(visitor, currentParent);
		}
		return support;
	}

	private class Finder implements StringFinder {

		private CharProvider chars;
		private Trie current;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.current = trie;
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
			while (!chars.finished()) {
				char c = chars.next();
				Trie next = current.nextNode(c);
				while(next == null) {
					Trie nextcurrent= support.get(current);
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
				if (current.isTerminal()) {
					buffer = createMatches(current, chars.current());
					return buffer.remove(0);
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

		private List<StringMatch> createMatches(Trie current, long end) {
			List<StringMatch> matches = new ArrayList<StringMatch>();
			matches.add(createMatch(end, current.length()));
			while (true) {
				current = support.get(current);
				if (current == null)  {
					break;
				} else if (current.isTerminal()) {
					StringMatch nextMatch = createMatch(end, current.length());
					if (!matches.contains(nextMatch)) {
						matches.add(nextMatch);
					}
				}
			}
			return matches;
		}

		private StringMatch createMatch(long end, int len) {
			long start = end - len;
			String s = chars.slice(start, end);
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
