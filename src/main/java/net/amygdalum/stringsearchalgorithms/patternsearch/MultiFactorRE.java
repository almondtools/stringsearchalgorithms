package net.amygdalum.stringsearchalgorithms.patternsearch;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.EmptyMatchFinder;
import net.amygdalum.stringsearchalgorithms.search.MultiStringSearchAlgorithmFactory;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.StringSearchAlgorithm;
import net.amygdalum.stringsearchalgorithms.search.StringSearchAlgorithmFactory;

/**
 * An implementation of the regex pattern search algorithm MultiStringRE.
 * 
 * This algorithm takes one or more regex patterns as input and generates a finder which can find this patterns in documents.
 * 
 * It depends on
 * - a search algorithm (which is an ordinary multi string search algorithm to detect pattern factors)
 * - and a pattern matcher (which matches patterns beginning at a detected factor)
 * 
 * The efficiency of this algorithm depends on the patterns to process:
 * - works better with large min length (min length is the minimum over the mininum length of each searched pattern) 
 * - works better with large alphabets
 * - works better if patterns have limited char combinations in their factors (dont-cares are hard)
 * - works really, really bad for dont-care-loops
 */
public class MultiFactorRE implements StringSearchAlgorithm {

	private static final int DEFAULT_MAX_LENGTH = 3;

	private int minLength;
	private StringSearchAlgorithm searchAlgorithm;
	private Map<String, List<FactorExtender>> extenders;

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, String... patterns) {
		this(factorSearcher, factorExtender, DEFAULT_MAX_LENGTH, asList(patterns));
	}

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int maxLength, String... patterns) {
		this(factorSearcher, factorExtender, maxLength, asList(patterns));
	}

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, Collection<String> patterns) {
		this(factorSearcher, factorExtender, DEFAULT_MAX_LENGTH, patterns);
	}

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int maxLength, Collection<String> patterns) {
		Map<String, FactorExtender> matchers = computeMatchers(patterns, factorExtender);
		this.minLength = computeMinLength(matchers);
		this.extenders = computeExtenders(matchers, minLength, maxLength);
		this.searchAlgorithm = factorSearcher.of(extenders.keySet());
	}

	private static Map<String, FactorExtender> computeMatchers(Collection<String> patterns, FactorExtenderFactory factorExtender) {
		Map<String, FactorExtender> matchers = new LinkedHashMap<>();
		for (String pattern : patterns) {
			matchers.put(pattern, factorExtender.of(pattern));
		}
		return matchers;
	}

	private static int computeMinLength(Map<String, FactorExtender> matchers) {
		int minLength = Integer.MAX_VALUE;
		for (FactorExtender matcher : matchers.values()) {
			minLength = min(minLength, matcher.getPatternLength());
		}
		return minLength;
	}

	private static Map<String, List<FactorExtender>> computeExtenders(Map<String, FactorExtender> matchers, int minLength, int length) {
		Map<String, List<FactorExtender>> factors = new LinkedHashMap<>();

		Collection<FactorExtender> allMatchers = matchers.values();
		for (FactorExtender matcher : allMatchers) {
			List<String> newFactors = matcher.getBestFactors(length);
			for (String newFactor : newFactors) {
				List<FactorExtender> matchersByFactor = factors.get(newFactor);
				if (matchersByFactor == null) {
					matchersByFactor = new ArrayList<>();
					factors.put(newFactor, matchersByFactor);
				}
				matchersByFactor.add(matcher.forFactor(newFactor));
			}
			if (matcher.getPatternLength() == 0) {
				List<FactorExtender> matchersByFactor = factors.get("");
				if (matchersByFactor == null) {
					matchersByFactor = new ArrayList<>();
					factors.put("", matchersByFactor);
				}
				matchersByFactor.add(matcher.forFactor(""));
			}
		}
		for (FactorExtender matcher : allMatchers) {
			String pattern = matcher.getPattern();
			for (Map.Entry<String, List<FactorExtender>> factorEntry : factors.entrySet()) {
				String factor = factorEntry.getKey();
				List<FactorExtender> extenders = factorEntry.getValue();
				Set<String> patterns = getPatterns(extenders);
				if (!patterns.contains(pattern) && matcher.hasFactor(factor)) {
					extenders.add(matcher.forFactor(factor));
				}
			}
		}
		return factors;
	}

	private static Set<String> getPatterns(List<FactorExtender> extenders) {
		Set<String> patterns = new LinkedHashSet<>();
		for (FactorExtender extender : extenders) {
			patterns.add(extender.getPattern());
		}
		return patterns;
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
		Set<String> factors = new LinkedHashSet<>();
		for (List<FactorExtender> matchExtenders : extenders.values()) {
			for (FactorExtender matchExtender : matchExtenders) {
				factors.add(matchExtender.toString());
			}
		}
		return getClass().getSimpleName() + "<" + searchAlgorithm.toString() + ", " + factors + ">";
	}

	private class Finder extends BufferedStringFinder {

		private StringFinder searchFactors;
		private boolean longest;
		private boolean nonEmpty;
		private CharProvider chars;
		private long lastStart;
		private long lastEnd;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.searchFactors = searchAlgorithm.createFinder(chars, options);
			if (minLength == 0) {
				this.searchFactors = new EmptyMatchFinder(searchFactors, chars, options);
			}
			this.longest = LONGEST_MATCH.in(options);
			this.nonEmpty = NON_EMPTY.in(options);
			this.chars = chars;
			this.lastStart = 0;
			this.lastEnd = -1;
		}

		@Override
		public void skipTo(long pos) {
			removeMatchesBefore(pos);
			if (lastStart < pos) {
				lastStart = pos;
			}
			if (lastStart > chars.current()) {
				searchFactors.skipTo(lastStart);
			}
		}

		@Override
		public StringMatch findNext() {
			long firstStart = lastStart;
			long currentStart = lastStart;
			while (!chars.finished() && (isBufferEmpty() || currentStart == firstStart)) {
				StringMatch match = searchFactors.findNext();
				if (match == null) {
					break;
				}

				if (firstStart == lastStart) {
					firstStart = match.start();
				}
				currentStart = match.start();

				extend(match);
			}
			if (chars.finished() && isBufferEmpty()) {
				StringMatch match = searchFactors.findNext();
				if (match != null) {
					extend(match);
				}
			}
			lastStart = currentStart;
			if (!isBufferEmpty()) {
				if (longest) {
					StringMatch current = longestLeftMost();
					lastEnd = current.end();
					return current;
				} else {
					StringMatch current = leftMost();
					lastEnd = current.end();
					return current;
				}
			}
			return null;
		}

		private void extend(StringMatch match) {
			List<FactorExtender> matchers = extenders.get(match.text());
			for (FactorExtender matcher : matchers) {
				long pos = chars.current();
				chars.move(match.end());
				for (StringMatch extendedMatch : matcher.extendFactor(chars, longest)) {
					if (extendedMatch.start() >= lastStart //do only report matches starting after last match
						&& (extendedMatch.start() > lastStart || extendedMatch.end() > lastEnd) // do only reports matches different form the last match
						&& (!longest || extendedMatch.end() > lastEnd) // if longest: do only report matches not being subsumed by last match
						&& (!nonEmpty || !extendedMatch.isEmpty())) { // if non-empty: do only report matches that do not match the empty string
						push(extendedMatch);
					}

				}
				chars.move(pos);
			}
		}
	}

	public static class Factory implements StringSearchAlgorithmFactory, MultiStringSearchAlgorithmFactory {

		private MultiStringSearchAlgorithmFactory factorSearcher;
		private FactorExtenderFactory factorExtender;
		private int maxLength;

		public Factory(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int maxLength) {
			this.factorSearcher = factorSearcher;
			this.factorExtender = factorExtender;
			this.maxLength = maxLength;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new MultiFactorRE(factorSearcher, factorExtender, maxLength, pattern);
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new MultiFactorRE(factorSearcher, factorExtender, maxLength, patterns);
		}

	}
}
