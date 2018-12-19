package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.CharUtils.revert;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
import net.amygdalum.util.text.CharAutomaton;
import net.amygdalum.util.text.CharConnectionAdaptor;
import net.amygdalum.util.text.CharDawgBuilder;
import net.amygdalum.util.text.CharMapping;
import net.amygdalum.util.text.CharNode;
import net.amygdalum.util.text.CharTask;
import net.amygdalum.util.text.CharWordSet;
import net.amygdalum.util.text.JoinStrategy;
import net.amygdalum.util.text.linkeddawg.CharClassicDawgFactory;
import net.amygdalum.util.text.linkeddawg.LinkedCharDawgBuilder;

/**
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a
 * finder which can find any of these patterns in documents.
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private CharMapping mapping;
	private CharWordSet<List<char[]>> trie;
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

	private static CharWordSet<List<char[]>> computeTrie(List<char[]> charpatterns, int length, CharMapping mapping) {
		CharDawgBuilder<List<char[]>> builder = new LinkedCharDawgBuilder<>(new CharClassicDawgFactory<List<char[]>>(), new MergePatterns());

		for (char[] pattern : charpatterns) {
			char[] prefix = copyOfRange(pattern, 0, length);
			char[] reversePrefix = revert(prefix);
			char[] suffix = copyOfRange(pattern, length, pattern.length);
			builder.extend(reversePrefix, asList(prefix, suffix));
		}
		builder.work(new BuildOracle());
		builder.work(new UseCharClasses(mapping));

		return builder.build();
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return new Finder(trie, minLength, mapping, chars, options);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public static class MergePatterns implements JoinStrategy<List<char[]>> {

		@Override
		public List<char[]> join(List<char[]> existing, List<char[]> next) {
			if (existing == null) {
				return new ArrayList<>(next);
			} else {
				existing.add(next.get(1));
				return existing;
			}
		}

	}

	public static class BuildOracle implements CharTask<List<char[]>> {
		private Map<CharNode<List<char[]>>, CharNode<List<char[]>>> oracle;
		private CharNode<List<char[]>> init;

		public BuildOracle() {
			oracle = new IdentityHashMap<>();
		}

		@Override
		public List<CharNode<List<char[]>>> init(CharNode<List<char[]>> root) {
			this.init = root;
			return asList(root);
		}

		@Override
		public List<CharNode<List<char[]>>> process(CharNode<List<char[]>> node) {
			List<CharNode<List<char[]>>> nexts = new ArrayList<>();
			for (char c : node.getAlternatives()) {
				CharNode<List<char[]>> current = node.nextNode(c);

				CharNode<List<char[]>> down = oracle.get(node);
				while (down != null) {
					CharNode<List<char[]>> next = down.nextNode(c);
					if (next != null) {
						oracle.put(current, next);
						break;
					}
					addNextNode(down, c, current);
					down = oracle.get(down);
				}
				if (down == null) {
					oracle.put(current, init);
				}

				nexts.add(current);
			}
			return nexts;
		}

		@SuppressWarnings("unchecked")
		private void addNextNode(CharNode<List<char[]>> node, char c, CharNode<List<char[]>> next) {
			((CharConnectionAdaptor<List<char[]>>) node).addNextNode(c, next);
		}
	}

	public static class UseCharClasses implements CharTask<List<char[]>> {

		private CharMapping mapping;
		private Set<CharNode<List<char[]>>> done;

		public UseCharClasses(CharMapping mapping) {
			this.mapping = mapping;
			this.done = new HashSet<>();
		}

		@Override
		public List<CharNode<List<char[]>>> init(CharNode<List<char[]>> root) {
			if (mapping == CharMapping.IDENTITY) {
				return Collections.emptyList();
			}
			return Arrays.asList(root);
		}

		@Override
		public List<CharNode<List<char[]>>> process(CharNode<List<char[]>> node) {
			List<CharNode<List<char[]>>> nexts = new ArrayList<>();

			for (char c : node.getAlternatives()) {
				CharNode<List<char[]>> next = node.nextNode(c);
				for (char cc : mapping.map(c)) {
					addNextNode(node, cc, next);
				}
				if (done.add(next)) {
					nexts.add(next);
				}
			}

			return nexts;
		}

		@SuppressWarnings("unchecked")
		private void addNextNode(CharNode<List<char[]>> node, char c, CharNode<List<char[]>> next) {
			((CharConnectionAdaptor<List<char[]>>) node).addNextNode(c, next);
		}
	}

	private static class Finder extends AbstractStringFinder {

		private final int minLength;
		private final int lookahead;
		private final CharMapping mapping;
		private CharProvider chars;
		private CharAutomaton<List<char[]>> cursor;
		private Queue<StringMatch> buffer;

		public Finder(CharWordSet<List<char[]>> trie, int minLength, CharMapping mapping, CharProvider chars, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.lookahead = minLength - 1;
			this.mapping = mapping;
			this.chars = chars;
			this.cursor = trie.cursor();
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
			next: while (!chars.finished(lookahead)) {
				cursor.reset();
				int j = lookahead;
				boolean success = true;
				while (j >= 0 && success) {
					success = cursor.accept(chars.lookahead(j));
					j--;
				}
				long currentWindowStart = chars.current();
				long currentPos = currentWindowStart + j + 1;
				long currentWindowEnd = currentWindowStart + minLength;
				char[] matchedPrefix = chars.between(currentPos, currentWindowEnd);
				if (success && j < 0) {
					List<char[]> patterns = cursor.iterator().next();
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
