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
import net.amygdalum.util.tries.CharTrie;
import net.amygdalum.util.tries.CharTrieCursor;
import net.amygdalum.util.tries.CharTrieTreeCompiler;
import net.amygdalum.util.tries.PreCharTrieNode;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private CharTrie<String> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private static CharTrie<String> computeTrie(List<char[]> charpatterns) {
		PreCharTrieNode<String> trie = new PreCharTrieNode<>();
		computeTrie(trie, charpatterns);
		computeSupportTransition(trie);
		return new CharTrieTreeCompiler<String>(false)
			.compileAndLink(trie);
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(trie, chars, options);
		} else {
			return new NextMatchFinder(trie, chars, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private static void computeTrie(PreCharTrieNode<String> trie, List<char[]> charpatterns) {
		for (char[] pattern : charpatterns) {
			trie.extend(pattern, new String(pattern));
		}
	}

	private static void computeSupportTransition(PreCharTrieNode<String> trie) {
		Queue<PreCharTrieNode<String>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreCharTrieNode<String> current = worklist.remove();
			for (Entry<PreCharTrieNode<String>> next : current.getNexts().cursor()) {
				char c = next.key;
				PreCharTrieNode<String> nextTrie = next.value;
				PreCharTrieNode<String> down = current.getLink();
				while (down != null) {
					PreCharTrieNode<String> nextNode = down.nextNode(c);
					if (nextNode != null) {
						nextTrie.link(nextNode);
						break;
					}
					down = down.getLink();
				}
				if (down == null) {
					nextTrie.link(trie);
				}
				worklist.add(nextTrie);
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static abstract class Finder extends BufferedStringFinder {

		protected CharProvider chars;
		protected CharTrieCursor<String> cursor;
		
		public Finder(CharTrie<String> trie, CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			cursor.reset();
			clear();
		}

		protected List<StringMatch> createMatches(long end) {
			List<StringMatch> matches = new ArrayList<>();
			for (String currentMatch : cursor) {
				long start = end - currentMatch.length();
				StringMatch nextMatch = createMatch(start, end);
				if (!matches.contains(nextMatch)) {
					matches.add(nextMatch);
				}
			}
			return matches;
		}

		private StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(CharTrie<String> trie, CharProvider chars, StringFinderOption... options) {
			super(trie, chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!chars.finished()) {
				char c = chars.next();
				boolean success = cursor.accept(c);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(chars.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(CharTrie<String> trie, CharProvider chars, StringFinderOption... options) {
			super(trie, chars, options);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char c = chars.next();
				boolean success = cursor.lookahead(c);
				if (!success && !isBufferEmpty()) {
					chars.prev();
					break;
				}
				success = cursor.accept(c);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(chars.current()));
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
