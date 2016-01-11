package com.almondtools.stringsandchars.patternsearch;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.BufferedStringFinder;
import com.almondtools.stringsandchars.search.MatchOption;
import com.almondtools.stringsandchars.search.MultiStringSearchAlgorithmFactory;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringFinderOption;
import com.almondtools.stringsandchars.search.StringMatch;
import com.almondtools.stringsandchars.search.StringSearchAlgorithm;
import com.almondtools.stringsandchars.search.StringSearchAlgorithmFactory;

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

	private static final int MIN_LENGTH = 1;
	
	private int minLength;
	private StringSearchAlgorithm searchAlgorithm;
	private Map<String, List<FactorExtender>> extenders;

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, String... patterns) {
		this(factorSearcher, factorExtender, MIN_LENGTH, asList(patterns));
	}

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int length, String... patterns) {
		this(factorSearcher, factorExtender, length, asList(patterns));
	}

	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, Collection<String> patterns) {
		this(factorSearcher, factorExtender, MIN_LENGTH, patterns);
	}
	
	public MultiFactorRE(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int length, Collection<String> patterns) {
		Map<String, FactorExtender> matchers = computeMatchers(patterns, factorExtender);
		this.minLength = computeMinLength(matchers, length);
		this.extenders = computeExtenders(matchers, minLength);
		this.searchAlgorithm = factorSearcher.of(extenders.keySet());
	}

	private static Map<String, FactorExtender> computeMatchers(Collection<String> patterns, FactorExtenderFactory factorExtender) {
		Map<String, FactorExtender> matchers = new LinkedHashMap<>();
		for (String pattern : patterns) {
			matchers.put(pattern, factorExtender.of(pattern));
		}
		return matchers;
	}

	private static int computeMinLength(Map<String, FactorExtender> matchers, int length) {
		int minLength = Integer.MAX_VALUE;
		for (FactorExtender matcher : matchers.values()) {
			minLength = min(minLength, matcher.getPatternLength());
		}
		return max(max(length, MIN_LENGTH), minLength);
	}

	private static Map<String, List<FactorExtender>> computeExtenders(Map<String, FactorExtender> matchers, int length) {
		Collection<FactorExtender> allMatchers = matchers.values();
		Map<String, List<FactorExtender>> factors = new LinkedHashMap<>();
		for (FactorExtender matcher : allMatchers) {
			List<String> newFactors = matcher.getBestFactors(length);
			for (String newFactor : newFactors) {
				List<FactorExtender> matchersByPrefix = factors.get(newFactor);
				if (matchersByPrefix == null) {
					matchersByPrefix = new ArrayList<>();
					factors.put(newFactor, matchersByPrefix);
				}
				matchersByPrefix.add(matcher.forFactor(newFactor));
			}
		}
		for (FactorExtender matcher : allMatchers) {
			String pattern = matcher.getPattern();
			for (Map.Entry<String,List<FactorExtender>> factorEntry : factors.entrySet()) {
				String factor = factorEntry.getKey();
				List<FactorExtender> extenders = factorEntry.getValue();
				Set<String> patterns = getPatterns(extenders);
				if (!patterns.contains(pattern) && matcher.hasFactor(factor))  {
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

	private class Finder extends BufferedStringFinder {

		private StringFinder searchFactors;
		private boolean longest;
		private CharProvider chars;
		private long last;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.searchFactors = searchAlgorithm.createFinder(chars, options);
			this.longest = MatchOption.LONGEST_MATCH.in(options);
			this.chars = chars;
			this.last = 0;
		}

		@Override
		public void skipTo(long pos) {
			last = removeMatchesBefore(pos);
			searchFactors.skipTo(last);
		}

		@Override
		public StringMatch findNext() {
			long firstStart = last;
			long currentStart = last;
			while (!chars.finished() && (isBufferEmpty() || currentStart == firstStart)) {
				StringMatch match = searchFactors.findNext();
				if (match == null) {
					break;
				}
				
				if (firstStart == last) {
					firstStart = match.start();
				}
				currentStart = match.start();
				
				List<FactorExtender> matchers = extenders.get(match.text());
				for (FactorExtender matcher : matchers) {
					for (StringMatch extendedMatch : matcher.extendFactor(chars, longest)) {
						if (extendedMatch.start() >= last) {
							push(extendedMatch);
						}
					}
				}
			}
			last = currentStart;
			if (!isBufferEmpty()) {
				if (longest) {
					return longestLeftMost();
				} else {
					return leftMost();
				}
			}
			return null;
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory, MultiStringSearchAlgorithmFactory {

		private MultiStringSearchAlgorithmFactory factorSearcher;
		private FactorExtenderFactory factorExtender;
		private int length;

		public Factory(MultiStringSearchAlgorithmFactory factorSearcher, FactorExtenderFactory factorExtender, int length) {
			this.factorSearcher = factorSearcher;
			this.factorExtender = factorExtender;
			this.length = length;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new MultiFactorRE(factorSearcher, factorExtender, length, pattern);
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new MultiFactorRE(factorSearcher, factorExtender, length, patterns);
		}

	}
}
