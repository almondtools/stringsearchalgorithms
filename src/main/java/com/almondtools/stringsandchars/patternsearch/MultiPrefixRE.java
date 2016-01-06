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
import com.almondtools.stringsandchars.search.MultiWordSearchAlgorithmFactory;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringFinderOption;
import com.almondtools.stringsandchars.search.StringMatch;
import com.almondtools.stringsandchars.search.StringSearchAlgorithm;
import com.almondtools.stringsandchars.search.WordSearchAlgorithmFactory;

/**
 * An implementation of the regex pattern search algorithm MultiStringRE.
 * 
 * This algorithm takes one or more regex patterns as input and generates a finder which can find this patterns in documents.
 * 
 * It depends on
 * - a prefix finder (which is an ordinary multi string search algorithm to detect pattern prefixes)
 * - and a pattern matcher (which matches patterns beginning at a detected prefix)
 * 
 * The efficiency of this algorithm depends on the patterns to process:
 * - works better with large min length (min length is the minimum over the mininum length of each searched pattern) 
 * - works better with large alphabets
 * - works better if patterns have limited char combinations in their prefixes (prefixes should not contain dont-cares)
 * - works really, really bad for dont-care-loops in prefixes
 */
public class MultiPrefixRE implements StringSearchAlgorithm {

	private static final int MIN_LENGTH = 2;
	
	private int minLength;
	private StringSearchAlgorithm prefixAlgorithm;
	private Map<String, List<PrefixPatternMatcher>> prefixes;

	public MultiPrefixRE(MultiWordSearchAlgorithmFactory prefixFinder, PatternMatcherFactory patternMatcher, String... patterns) {
		this(prefixFinder, patternMatcher, MIN_LENGTH, asList(patterns));
	}

	public MultiPrefixRE(MultiWordSearchAlgorithmFactory prefixFinder, PatternMatcherFactory patternMatcher, int length, String... patterns) {
		this(prefixFinder, patternMatcher, length, asList(patterns));
	}

	public MultiPrefixRE(MultiWordSearchAlgorithmFactory prefixFinder, PatternMatcherFactory patternMatcher, Collection<String> patterns) {
		this(prefixFinder, patternMatcher, 2, patterns);
	}
	
	public MultiPrefixRE(MultiWordSearchAlgorithmFactory prefixFinder, PatternMatcherFactory patternMatcher, int length, Collection<String> patterns) {
		Map<String, PrefixPatternMatcher> matchers = computeMatchers(patterns, patternMatcher);
		this.minLength = computeMinLength(matchers, length);
		this.prefixes = computePrefixes(matchers, minLength);
		this.prefixAlgorithm = prefixFinder.of(prefixes.keySet());
	}

	private static Map<String, PrefixPatternMatcher> computeMatchers(Collection<String> patterns, PatternMatcherFactory patternMatcher) {
		Map<String, PrefixPatternMatcher> matchers = new LinkedHashMap<>();
		for (String pattern : patterns) {
			matchers.put(pattern, patternMatcher.of(pattern));
		}
		return matchers;
	}

	private static int computeMinLength(Map<String, PrefixPatternMatcher> matchers, int length) {
		int minLength = Integer.MAX_VALUE;
		for (PrefixPatternMatcher matcher : matchers.values()) {
			minLength = min(minLength, matcher.getPatternLength());
		}
		return max(max(length, MIN_LENGTH), minLength);
	}

	private static Map<String, List<PrefixPatternMatcher>> computePrefixes(Map<String, PrefixPatternMatcher> matchers, int length) {
		Map<String, List<PrefixPatternMatcher>> prefixes = new LinkedHashMap<>();
		for (PrefixPatternMatcher matcher : matchers.values()) {
			List<String> newprefixes = matcher.getPrefixes(length);
			for (String newprefix : newprefixes) {
				List<PrefixPatternMatcher> matchersByPrefix = prefixes.get(newprefix);
				if (matchersByPrefix == null) {
					matchersByPrefix = new ArrayList<>();
					prefixes.put(newprefix, matchersByPrefix);
				}
				matchersByPrefix.add(matcher.withPrefix(newprefix));
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

		private StringFinder prefixFinder;
		private boolean longest;
		private CharProvider chars;
		private Queue<StringMatch> buffer;
		private long last;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.prefixFinder = prefixAlgorithm.createFinder(chars, options);
			this.longest = MatchOption.LONGEST_MATCH.in(options);
			this.chars = chars;
			this.buffer = new PriorityQueue<StringMatch>();
			this.last = 0;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(buffer, pos);
			prefixFinder.skipTo(last);
		}

		@Override
		public StringMatch findNext() {
			long firstStart = last;
			long currentStart = last;
			while (!chars.finished() && (buffer.isEmpty() || currentStart == firstStart)) {
				StringMatch match = prefixFinder.findNext();
				if (match == null) {
					break;
				}
				
				if (firstStart == last) {
					firstStart = match.start();
				}
				currentStart = match.start();
				
				List<PrefixPatternMatcher> matchers = prefixes.get(match.text());
				for (PrefixPatternMatcher matcher : matchers) {
					buffer.addAll(matcher.match(chars, longest));
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

	public static class Factory implements WordSearchAlgorithmFactory, MultiWordSearchAlgorithmFactory {

		private MultiWordSearchAlgorithmFactory prefixFinder;
		private PatternMatcherFactory patternMatcher;
		private int length;

		public Factory(MultiWordSearchAlgorithmFactory prefixFinder, PatternMatcherFactory patternMatcher, int length) {
			this.prefixFinder = prefixFinder;
			this.patternMatcher = patternMatcher;
			this.length = length;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new MultiPrefixRE(prefixFinder, patternMatcher, length, pattern);
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new MultiPrefixRE(prefixFinder, patternMatcher, length, patterns);
		}

	}
}
