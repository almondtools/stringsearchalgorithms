package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.patternsearch.GlushkovAnalyzerOption.FACTORS;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.io.ReverseCharProvider;
import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.regex.RegexNode;
import com.almondtools.stringsandchars.regex.RegexParser;
import com.almondtools.stringsandchars.search.StringMatch;

public class GlushkovFactorExtender implements FactorExtender {

	private Set<String> bestFactors;
	private DualGlushkovAutomaton factors;
	private GlushkovAutomaton automaton;
	private int minLength;

	private int factorLength;
	private BitSet factorInitial;

	public GlushkovFactorExtender(String pattern) {
		RegexNode root = parseAndNormalizeRegex(pattern);
		BestFactorAnalyzer bestFactorAnalyzer = new BestFactorAnalyzer(root).analyze();
		GlushkovAnalyzer analyzer = new GlushkovAnalyzer(root).analyze();

		bestFactors = bestFactorAnalyzer.getBestFactors(asStrings(analyzer.firstChars()), asStrings(analyzer.lastChars()));
		factors = analyzer.buildReverseAutomaton(FACTORS);
		automaton = analyzer.buildAutomaton();
		minLength = analyzer.minLength();
	}

	private GlushkovFactorExtender(DualGlushkovAutomaton factors, GlushkovAutomaton automaton, int minLength, int factorLength, BitSet factorInitial) {
		this.factors = factors;
		this.automaton = automaton;
		this.minLength = minLength;
		this.factorLength = factorLength;
		this.factorInitial = factorInitial;
	}

	private static RegexNode parseAndNormalizeRegex(String pattern) {
		RegexParser parser = new RegexParser(pattern);
		RegexNode root = parser.parse();
		return root.accept(new GlushkovNormalizer());
	}

	public GlushkovFactorExtender forFactor(String factor) {
		BitSet factorInitial = backTrack(factors.getInitial(), factor);
		return new GlushkovFactorExtender(factors, automaton, minLength, factor.length(), factorInitial);
	}

	private Set<String> asStrings(Set<Character> chars) {
		Set<String> strings = new LinkedHashSet<>();
		for (Character c : chars) {
			strings.add(c.toString());
		}
		return strings;
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public List<String> getBestFactors(int max) {
		Set<String> bestFactorsMax = new LinkedHashSet<>();
		for (String factor : bestFactors) {
			if (factor.length() <= max) {
				bestFactorsMax.add(factor);
			} else {
				bestFactorsMax.add(factor.substring(0, max));
			}
		}
		return new ArrayList<>(bestFactorsMax);
	}

	@Override
	public List<StringMatch> extendFactor(CharProvider chars, boolean longest) {
		long pos = chars.current();
		List<Long> starts = findStarts(chars);
		MatchBuilder listener = new MatchBuilder(longest);
		match(starts, chars, listener);
		chars.move(pos);
		return listener.getMatches();
	}

	private BitSet backTrack(BitSet state, String factor) {
		CharProvider reverse = new ReverseCharProvider(new StringCharProvider(factor, factor.length()));
		while (!reverse.finished() && !state.isEmpty()) {
			char c = reverse.next();
			state = factors.next(state, c);
		}
		return state;
	}

	private List<Long> findStarts(CharProvider chars) {
		long factorStart = chars.current() - factorLength;
		chars.move(factorStart);
		List<Long> starts = new LinkedList<>();
		BitSet state = factorInitial;
		CharProvider reverse = new ReverseCharProvider(chars);
		while (!reverse.finished() && !state.isEmpty()) {
			if (factors.isFinal(state)) {
				starts.add(0, chars.current());
			}
			char c = reverse.next();
			state = factors.next(state, c);
		}
		if (reverse.finished() && automaton.isFinal(state)) {
			starts.add(0, chars.current());
		}
		return starts;
	}

	private void match(List<Long> starts, CharProvider chars, MatchListener... listeners) {
		boolean notify = listeners != null && listeners.length > 0;
		for (long start : starts) {
			chars.move(start);
			BitSet state = automaton.getInitial();
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
		}
	}

	public static class Factory implements FactorExtenderFactory {

		@Override
		public FactorExtender of(String pattern) {
			return new GlushkovFactorExtender(pattern);
		}

	}
}
