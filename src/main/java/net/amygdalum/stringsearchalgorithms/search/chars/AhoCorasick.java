package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.map.CharObjectMap.Entry;
import net.amygdalum.util.tries.CharTrieNode;
import net.amygdalum.util.tries.CharTrieNodeCompiler;
import net.amygdalum.util.tries.PreCharTrieNode;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private CharTrieNode<String> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private static CharTrieNode<String> computeTrie(List<char[]> charpatterns) {
		PreCharTrieNode<String> trie = new PreCharTrieNode<>();
		for (char[] pattern : charpatterns) {
			trie.extend(pattern, new String(pattern));
		}
		return computeSupportTransition(trie);
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(chars, options);
		} else {
			return new NextMatchFinder(chars, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private static CharTrieNode<String> computeSupportTransition(PreCharTrieNode<String> trie) {
		Queue<PreCharTrieNode<String>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreCharTrieNode<String> current = worklist.remove();
			for (Entry<PreCharTrieNode<String>> next : current.getNexts().cursor()) {
				PreCharTrieNode<String> nextTrie = next.value;
				computeSupport(current, next.key, nextTrie, trie);
				worklist.add(nextTrie);
			}
		}
		return new CharTrieNodeCompiler<String>(false).compileAndLink(trie);
	}

	private static void computeSupport(PreCharTrieNode<String> parent, char c, PreCharTrieNode<String> trie, PreCharTrieNode<String> init) {
		if (parent != null) {
			PreCharTrieNode<String> down = parent.getLink();
			while (down != null && down.nextNode(c) == null) {
				down = down.getLink();
			}
			if (down != null) {
				PreCharTrieNode<String> next = down.nextNode(c);
				trie.link(next);
				String nextMatch = next.getAttached();
				if (nextMatch != null && trie.getAttached() == null) {
					trie.setAttached(nextMatch);
				}
			} else {
				trie.link(init);
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends BufferedStringFinder {
		protected CharProvider chars;
		protected CharTrieNode<String> current;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.current = trie;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			current = trie;
			clear();
		}

		protected List<StringMatch> createMatches(CharTrieNode<String> current, long end) {
			List<StringMatch> matches = new ArrayList<>();
			while (current != null) {
				String currentMatch = current.getAttached();
				if (currentMatch != null) {
					long start = end - currentMatch.length();
					StringMatch nextMatch = createMatch(start, end);
					if (!matches.contains(nextMatch)) {
						matches.add(nextMatch);
					}
				}
				current = current.getLink();
			}
			return matches;
		}

		private StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private class NextMatchFinder extends Finder {

		public NextMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!chars.finished()) {
				char c = chars.next();
				CharTrieNode<String> next = current.nextNode(c);
				while (next == null) {
					CharTrieNode<String> nextcurrent = current.getLink();
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
				if (current.getAttached() != null) {
					push(createMatches(current, chars.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private class LongestMatchFinder extends Finder {

		public LongestMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char c = chars.next();
				CharTrieNode<String> next = current.nextNode(c);
				if (next == null && !isBufferEmpty()) {
					chars.prev();
					break;
				}
				while (next == null) {
					CharTrieNode<String> nextcurrent = current.getLink();
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
				if (current.getAttached() != null) {
					push(createMatches(current, chars.current()));
				}
			}
			return longestLeftMost();
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new AhoCorasick(patterns);
		}

	}
}
