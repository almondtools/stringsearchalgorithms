package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.AttachmentAdaptor.attach;
import static net.amygdalum.util.text.CharFallbackAdaptor.getFallback;
import static net.amygdalum.util.text.CharFallbackAdaptor.setFallback;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.text.CharAutomaton;
import net.amygdalum.util.text.CharNode;
import net.amygdalum.util.text.CharTask;
import net.amygdalum.util.text.CharTrie;
import net.amygdalum.util.text.CharWordSet;
import net.amygdalum.util.text.CharWordSetBuilder;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayCharFallbackTrieCompiler;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private CharWordSet<String> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
	}

	private static CharWordSet<String> computeTrie(List<char[]> charpatterns) {
		CharWordSetBuilder<String, CharTrie<String>> builder = new CharWordSetBuilder<>(new DoubleArrayCharFallbackTrieCompiler<String>());

		for (char[] pattern : charpatterns) {
			builder.extend(pattern, new String(pattern));
		}

		return builder
			.work(new FallbackLinks())
			.build();
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

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static class FallbackLinks implements CharTask<String> {

		private CharNode<String> root;

		@Override
		public List<CharNode<String>> init(CharNode<String> root) {
			this.root = root;
			setFallback(root, null);
			return asList(root);
		}

		@Override
		public List<CharNode<String>> process(CharNode<String> node) {
			List<CharNode<String>> nexts = new ArrayList<>();
			for (char c : node.getAlternatives()) {
				CharNode<String> next = node.nextNode(c);
				CharNode<String> down = getFallback(node);
				nextdown: while (down != null) {
					CharNode<String> nextNode = down.nextNode(c);
					if (nextNode != null) {
						setFallback(next, nextNode);
						if (next.getAttached() == null) {
							String attachment = nextNode.getAttached();
							if (attachment != null) {
								attach(next, attachment);
							}
						}
						break nextdown;
					}
					down = getFallback(down);
				}
				if (down == null) {
					setFallback(next, root);
				}
				nexts.add(next);
			}
			return nexts;
		}

	}

	private static abstract class Finder extends BufferedStringFinder {

		protected CharProvider chars;
		protected CharAutomaton<String> cursor;

		public Finder(CharWordSet<String> trie, CharProvider chars, StringFinderOption... options) {
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

		public NextMatchFinder(CharWordSet<String> trie, CharProvider chars, StringFinderOption... options) {
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

		public LongestMatchFinder(CharWordSet<String> trie, CharProvider chars, StringFinderOption... options) {
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
