package com.almondtools.stringsandchars.patternsearch;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.AbstractStringFinder;
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
		this.extenders = computePrefixes(matchers, minLength);
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

	private static Map<String, List<FactorExtender>> computePrefixes(Map<String, FactorExtender> matchers, int length) {
		Map<String, List<FactorExtender>> prefixes = new LinkedHashMap<>();
		for (FactorExtender matcher : matchers.values()) {
			List<String> newprefixes = matcher.getBestFactors(length);
			for (String newprefix : newprefixes) {
				List<FactorExtender> matchersByPrefix = prefixes.get(newprefix);
				if (matchersByPrefix == null) {
					matchersByPrefix = new ArrayList<>();
					prefixes.put(newprefix, matchersByPrefix);
				}
				matchersByPrefix.add(matcher.forFactor(newprefix));
			}
		}
		return prefixes;
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

		private StringFinder searchFactors;
		private boolean longest;
		private CharProvider chars;
		private Queue<StringMatch> buffer;
		private long last;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.searchFactors = searchAlgorithm.createFinder(chars, options);
			this.longest = MatchOption.LONGEST_MATCH.in(options);
			this.chars = chars;
			this.buffer = new PriorityQueue<StringMatch>();
			this.last = 0;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(buffer, pos);
			searchFactors.skipTo(last);
		}

		@Override
		public StringMatch findNext() {
			long firstStart = last;
			long currentStart = last;
			while (!chars.finished() && (buffer.isEmpty() || currentStart == firstStart)) {
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
					buffer.addAll(matcher.extendFactor(chars, longest));
				}
			}
			last = currentStart;
			if (!buffer.isEmpty()) {
				if (longest) {
					return longestLeftMost(buffer);
				} else {
					return buffer.remove();
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
