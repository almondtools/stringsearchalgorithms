package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.patternsearch.GlushkovAnalyzerOption.SELF_LOOP;
import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.io.ReverseCharProvider;
import com.almondtools.stringsandchars.regex.RegexNode;
import com.almondtools.stringsandchars.regex.RegexParser;
import com.almondtools.stringsandchars.search.AbstractStringFinder;
import com.almondtools.stringsandchars.search.MatchOption;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringFinderOption;
import com.almondtools.stringsandchars.search.StringMatch;
import com.almondtools.stringsandchars.search.StringSearchAlgorithm;

/**
 * An implementation of the regex pattern search algorithm Bit-Parallel Glushkov.
 * 
 * This algorithm takes a regex pattern as input and generates a finder which can find this pattern in documents.
 */
public class BPGlushkov implements StringSearchAlgorithm {

	private GlushkovAutomaton search;
	private DualGlushkovAutomaton back;
	private int minLength;

	public BPGlushkov(String pattern) {
		GlushkovAnalyzer analyzer = parseAndNormalizeRegex(pattern);
		search = analyzer.buildAutomaton(SELF_LOOP);
		back = analyzer.buildReverseAutomaton();
		minLength = search.minLength();
	}

	private static GlushkovAnalyzer parseAndNormalizeRegex(String pattern) {
		RegexParser parser = new RegexParser(pattern);
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

	private class Finder extends AbstractStringFinder {

		private boolean longestMatch;
		private boolean nonEmpty;
		private CharProvider chars;
		private CharProvider reverse;
		private long border;
		private BitSet state;
		private Queue<StringMatch> buffer;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.longestMatch = LONGEST_MATCH.in(options);
			this.nonEmpty = MatchOption.NON_EMPTY.in(options);
			this.chars = chars;
			this.reverse = new ReverseCharProvider(chars);
			this.border = -1;
			this.state = search.getInitial();
			this.buffer = new PriorityQueue<StringMatch>();
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(buffer, pos);
			border = last;
			chars.move(last);
		}

		@Override
		public StringMatch findNext() {
			if (chars.finished() && border >= chars.current() && buffer.isEmpty()) {
				return null;
			}
			if (buffer.isEmpty()) {
				while (!chars.finished()) {
					if (search.isFinal(state)) {
						buffer.addAll(createMatches(chars.current(), state));
					}
					char c = chars.next();
					state = search.next(state, c);
					if (search.isInitial(state) && !buffer.isEmpty()) {
						break;
					}
				}
				if (chars.finished() && search.isFinal(state)) {
					buffer.addAll(createMatches(chars.current(), state));
					border = chars.current();
				}
			}
			if (buffer.isEmpty()) {
				return null;
			} else if (!longestMatch) {
				return buffer.remove();
			} else {
				return longestLeftMost(buffer);
			}
		}

		private List<StringMatch> createMatches(long end, BitSet state) {
			if (end <= border) {
				return emptyList();
			}
			state = (BitSet) state.clone();
			state.and(back.getInitial());

			List<StringMatch> matches = new ArrayList<>();

			long backup = reverse.current();
			reverse.move(end);
			while (!reverse.finished() && !state.isEmpty()) {
				if (back.isFinal(state)) {
					long start = reverse.current();
					matches.add(new StringMatch(start, end, chars.slice(start, end)));
				}
				char c = reverse.next();
				state = back.next(state, c);
			}
			if (reverse.finished() && back.isFinal(state)) {
				long start = reverse.current();
				matches.add(new StringMatch(start, end, chars.slice(start, end)));
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
}
