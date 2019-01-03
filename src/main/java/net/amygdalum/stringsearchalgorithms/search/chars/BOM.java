package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.asList;
import static net.amygdalum.util.text.CharUtils.revert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.text.CharAutomaton;
import net.amygdalum.util.text.CharConnectionAdaptor;
import net.amygdalum.util.text.CharDawg;
import net.amygdalum.util.text.CharMapping;
import net.amygdalum.util.text.CharNode;
import net.amygdalum.util.text.CharTask;
import net.amygdalum.util.text.CharWordSet;
import net.amygdalum.util.text.CharWordSetBuilder;
import net.amygdalum.util.text.linkeddawg.LinkedCharDawgCompiler;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle
 * Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which
 * can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private CharWordSet<char[]> trie;
	private int patternLength;

	public BOM(String pattern) {
		this(pattern, CharMapping.IDENTITY);
	}

	public BOM(String pattern, CharMapping mapping) {
		this.patternLength = pattern.length();
		this.trie = computeTrie(pattern.toCharArray(), mapping);
	}

	private static CharWordSet<char[]> computeTrie(char[] pattern, CharMapping mapping) {
		if (mapping != CharMapping.IDENTITY) {
			pattern = mapping.normalized(pattern);
		}

		CharWordSetBuilder<char[], CharDawg<char[]>> builder = new CharWordSetBuilder<>(new LinkedCharDawgCompiler<char[]>());

		builder.extend(revert(pattern), pattern);
		builder.work(new BuildOracle());
		builder.work(new UseCharClasses(mapping));

		return builder.build();
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

	public static class BuildOracle implements CharTask<char[]> {
		private Map<CharNode<char[]>, CharNode<char[]>> oracle;
		private CharNode<char[]> init;

		public BuildOracle() {
			oracle = new IdentityHashMap<>();
		}

		@Override
		public List<CharNode<char[]>> init(CharNode<char[]> root) {
			this.init = root;
			return asList(root);
		}

		@Override
		public List<CharNode<char[]>> process(CharNode<char[]> node) {
			List<CharNode<char[]>> nexts = new ArrayList<>();
			for (char c : node.getAlternatives()) {
				CharNode<char[]> current = node.nextNode(c);

				CharNode<char[]> down = oracle.get(node);
				while (down != null) {
					CharNode<char[]> next = down.nextNode(c);
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
		private void addNextNode(CharNode<char[]> node, char c, CharNode<char[]> next) {
			((CharConnectionAdaptor<char[]>) node).addNextNode(c, next);
		}

	}

	public static class UseCharClasses implements CharTask<char[]> {

		private CharMapping mapping;
		private Set<CharNode<char[]>> done;

		public UseCharClasses(CharMapping mapping) {
			this.mapping = mapping;
			this.done = new HashSet<>();
		}

		@Override
		public List<CharNode<char[]>> init(CharNode<char[]> root) {
			if (mapping == CharMapping.IDENTITY) {
				return Collections.emptyList();
			}
			return asList(root);
		}

		@Override
		public List<CharNode<char[]>> process(CharNode<char[]> node) {
			List<CharNode<char[]>> nexts = new ArrayList<>();

			for (char c : node.getAlternatives()) {
				CharNode<char[]> next = node.nextNode(c);
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
		private void addNextNode(CharNode<char[]> node, char c, CharNode<char[]> next) {
			((CharConnectionAdaptor<char[]>) node).addNextNode(c, next);
		}

	}

	private static class Finder extends AbstractStringFinder {

		private final int lookahead;
		private CharProvider chars;
		private CharAutomaton<char[]> cursor;

		public Finder(CharWordSet<char[]> trie, int patternLength, CharProvider chars, StringFinderOption... options) {
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
