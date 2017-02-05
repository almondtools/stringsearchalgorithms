package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.amygdalum.stringsearchalgorithms.patternsearch.chars.GlushkovAnalyzerOption.SELF_LOOP;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.regexparser.RegexParser;
import net.amygdalum.regexparser.RegexParserOption;
import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.MatchOption;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.chars.StringSearchAlgorithm;
import net.amygdalum.stringsearchalgorithms.search.chars.StringSearchAlgorithmFactory;
import net.amygdalum.util.bits.BitSet;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.ReverseCharProvider;

/**
 * An implementation of the regex pattern search algorithm Bit-Parallel Glushkov.
 * 
 * This algorithm takes a regex pattern as input and generates a finder which can find this pattern in documents.
 * 
 * The efficiency of this algorithm depends on the pattern to process:
 * - works fine for complex patterns (many regex operators)
 * - works better for short patterns (few chars (excluding regex operators))
*/
public class BPGlushkov implements StringSearchAlgorithm {

	private GlushkovAutomaton search;
	private DualGlushkovAutomaton back;
	private int minLength;

	public BPGlushkov(String pattern, RegexParserOption... options) {
		GlushkovAnalyzer analyzer = parseAndNormalizeRegex(pattern, options);
		search = analyzer.buildAutomaton(SELF_LOOP);
		back = analyzer.buildReverseAutomaton();
		minLength = analyzer.minLength();
	}

	private static GlushkovAnalyzer parseAndNormalizeRegex(String pattern, RegexParserOption... options) {
		RegexParser parser = new RegexParser(pattern, options);
		RegexNode root = parser.parse();
		root = root.accept(new GlushkovNormalizer());
		return new GlushkovAnalyzer(root).analyze();
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

	private class Finder extends BufferedStringFinder {

		private boolean longestMatch;
		private boolean nonEmpty;
		private CharProvider chars;
		private CharProvider reverse;
		private long border;
		private BitSet state;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.longestMatch = LONGEST_MATCH.in(options);
			this.nonEmpty = MatchOption.NON_EMPTY.in(options);
			this.chars = chars;
			this.reverse = new ReverseCharProvider(chars);
			this.border = -1;
			this.state = search.getInitial();
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			border = last;
			if (last > chars.current()) {
				chars.move(last);
			}
		}

		@Override
		public StringMatch findNext() {
			if (chars.finished() && border >= chars.current() && isBufferEmpty()) {
				return null;
			}
			if (isBufferEmpty()) {
				while (!chars.finished()) {
					if (search.isFinal(state)) {
						push(createMatches(chars.current(), state));
					}
					char c = chars.next();
					state = search.next(state, c);
					if (search.isInitial(state) && !isBufferEmpty()) {
						break;
					}
				}
				if (chars.finished() && search.isFinal(state)) {
					push(createMatches(chars.current(), state));
					border = chars.current();
				}
			}
			if (isBufferEmpty()) {
				return null;
			} else if (!longestMatch) {
				return leftMost();
			} else {
				return longestLeftMost();
			}
		}

		private List<StringMatch> createMatches(long end, BitSet state) {
			if (end <= border) {
				return emptyList();
			}
			state = state.and(back.getInitial());

			List<StringMatch> matches = new ArrayList<>();

			long backup = reverse.current();
			reverse.move(end);
			while (!reverse.finished() && !state.isEmpty()) {
				if (back.isFinal(state)) {
					long start = reverse.current();
					matches.add(createMatch(start, end));
				}
				char c = reverse.next();
				state = back.next(state, c);
			}
			if (reverse.finished() && back.isFinal(state)) {
				long start = reverse.current();
				matches.add(createMatch(start, end));
			}
			reverse.move(backup);
			if (nonEmpty) {
				removeEmpty(matches);
			}
			if (longestMatch && !matches.isEmpty()) {
				StringMatch longest = matches.remove(matches.size() - 1);
				return asList(longest);
			} else {
				return matches;
			}
		}

		public StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

		private void removeEmpty(List<StringMatch> matches) {
			Iterator<StringMatch> matchIterator = matches.iterator();
			while (matchIterator.hasNext()) {
				StringMatch match = matchIterator.next();
				if (match.isEmpty()) {
					matchIterator.remove();
				}
			}
		}
	}

	public static class Factory implements StringSearchAlgorithmFactory {

		private RegexParserOption[] options;

		public Factory(RegexParserOption... options) {
			this.options = options;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BPGlushkov(pattern, options);
		}

	}
}
