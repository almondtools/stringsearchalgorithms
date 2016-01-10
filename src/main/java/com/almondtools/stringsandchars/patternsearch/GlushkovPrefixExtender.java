package com.almondtools.stringsandchars.patternsearch;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.regex.RegexNode;
import com.almondtools.stringsandchars.regex.RegexParser;
import com.almondtools.stringsandchars.search.StringMatch;

public class GlushkovPrefixExtender implements FactorExtender {

	private GlushkovAutomaton automaton;
	private int minLength;
	
	private int prefixLength;
	private BitSet prefixInitial;

	public GlushkovPrefixExtender(String pattern) {
		RegexNode root = parseAndNormalizeRegex(pattern);
		GlushkovAnalyzer analyzer = new GlushkovAnalyzer(root).analyze();
		automaton = analyzer.buildAutomaton();
		minLength = analyzer.minLength();
	}

	private GlushkovPrefixExtender(GlushkovAutomaton automaton, int minLength, int prefixLength, BitSet prefixInitial) {
		this.automaton = automaton;
		this.minLength = minLength;
		this.prefixLength = prefixLength;
		this.prefixInitial = prefixInitial;
	}

	private static RegexNode parseAndNormalizeRegex(String pattern) {
		RegexParser parser = new RegexParser(pattern);
		RegexNode root = parser.parse();
		return root.accept(new GlushkovNormalizer());
	}
	
	public GlushkovPrefixExtender forFactor(String prefix) {
		BitSet prefixInitial = match(automaton.getInitial(), new StringCharProvider(prefix, 0));
		return new GlushkovPrefixExtender(automaton, minLength, prefix.length(), prefixInitial);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public List<String> getBestFactors(int max) {
		return new ArrayList<>(getPrefixes(max));
	}

	public Set<String> getPrefixes(int max) {
		return getPrefixes(automaton.getInitial(), 1, max);
	}

	private Set<String> getPrefixes(BitSet state, int min, int max) {
		Set<String> prefixes = new LinkedHashSet<String>();
		if (min <= 0 && automaton.isFinal(state)) {
			prefixes.add("");
			return prefixes;
		} else if (max <= 0) {
			prefixes.add("");
			return prefixes;
		}
		for (char c : automaton.supportedChars()) {
			BitSet next = automaton.next(state, c);
			if (!next.isEmpty()) {
				Set<String> subPrefixes = getPrefixes(next, min - 1, max - 1);
				for (String subPrefix : subPrefixes) {
					prefixes.add(c + subPrefix);
				}
			}
		}
		return prefixes;
	}

	@Override
	public List<StringMatch> extendFactor(CharProvider chars, boolean longest) {
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
	
	public static class Factory implements FactorExtenderFactory {

		@Override
		public FactorExtender of(String pattern) {
			return new GlushkovPrefixExtender(pattern);
		}
		
	}
}
