package com.almondtools.stringsandchars.patternsearch;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.regex.RegexNode;
import com.almondtools.stringsandchars.regex.RegexParser;
import com.almondtools.stringsandchars.search.StringMatch;

public class GlushkovPrefixPatternMatcher implements PrefixPatternMatcher {

	private GlushkovAutomaton automaton;
	private int minLength;
	
	private int prefixLength;
	private BitSet prefixInitial;

	public GlushkovPrefixPatternMatcher(String pattern) {
		GlushkovAnalyzer analyzer = parseAndNormalizeRegex(pattern);
		automaton = analyzer.buildAutomaton();
		minLength = analyzer.minLength();
	}

	private GlushkovPrefixPatternMatcher(GlushkovAutomaton automaton, int minLength, int prefixLength, BitSet prefixInitial) {
		this.automaton = automaton;
		this.minLength = minLength;
		this.prefixLength = prefixLength;
		this.prefixInitial = prefixInitial;
	}

	private static GlushkovAnalyzer parseAndNormalizeRegex(String pattern) {
		RegexParser parser = new RegexParser(pattern);
		RegexNode root = parser.parse();
		root = root.accept(new GlushkovNormalizer());
		return new GlushkovAnalyzer(root).analyze();
	}
	
	public GlushkovPrefixPatternMatcher withPrefix(String prefix) {
		BitSet prefixInitial = match(automaton.getInitial(), new StringCharProvider(prefix, 0));
		return new GlushkovPrefixPatternMatcher(automaton, minLength, prefix.length(), prefixInitial);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public List<String> getPrefixes(int max) {
		return new ArrayList<>(automaton.getPrefixes(max));
	}

	@Override
	public List<StringMatch> match(CharProvider chars, boolean longest) {
		MatchBuilder listener = new MatchBuilder(longest);
		match(prefixInitial, chars, listener);
		return listener.getMatches();
	}

	private BitSet match(BitSet state, CharProvider chars, MatchListener... listeners) {
		boolean notify = listeners != null && listeners.length > 0; 
		long pos = chars.current();
		long start = pos - this.prefixLength;
		while (!chars.finished() && !state.isEmpty()) {
			if (notify && automaton.isFinal(state)) {
				long end = chars.current();
				for (MatchListener listener : listeners) {
					listener.notify(start, end, chars);
				}
			}
			char c = chars.next();
			state = automaton.next(state, c);
		}
		if (notify && chars.finished() && automaton.isFinal(state)) {
			long end = chars.current();
			for (MatchListener listener : listeners) {
				listener.notify(start, end, chars);
			}
		}
		chars.move(pos);
		return state;
	}

	public class MatchBuilder implements MatchListener {
		
		private boolean longest;
		private List<StringMatch> matches;

		public MatchBuilder(boolean longest) {
			this.longest = longest;
			this.matches = new ArrayList<>();
		}

		@Override
		public void notify(long start, long end, CharProvider chars) {
			matches.add(new StringMatch(start, end, chars.slice(start, end)));
		}
		
		public List<StringMatch> getMatches() {
			if (longest && !matches.isEmpty()) {
				return matches.subList(matches.size() -1, matches.size());
			} else {
				return matches;
			}
		}

	}
	
	public static class Factory implements PatternMatcherFactory {

		@Override
		public PrefixPatternMatcher of(String pattern) {
			return new GlushkovPrefixPatternMatcher(pattern);
		}
		
	}
}
