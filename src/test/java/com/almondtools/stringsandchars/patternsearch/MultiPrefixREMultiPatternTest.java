package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.AhoCorasick;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;


public class MultiPrefixREMultiPatternTest {

	@Test
	public void testRegexComplex1() throws Exception {
		StringFinder finder = findIn("xxxccaaxbdddccccdxaaxbbaaaxxd", "a+","b+","c+","d+");
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "cc"),
			new StringMatch(5, 7, "aa"),
			new StringMatch(8, 9, "b"),
			new StringMatch(9, 12, "ddd"),
			new StringMatch(12, 16, "cccc"),
			new StringMatch(16, 17, "d"),
			new StringMatch(18, 20, "aa"),
			new StringMatch(21, 23, "bb"),
			new StringMatch(23, 26, "aaa"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	public void testRegexComplex2() throws Exception {
		StringFinder finder = findIn("xxxccaaxbdddccccdxaaxbbaaaxxd", "c{1,2}(a|d)+","d+c{3}?","(b|d)+c");
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 7, "ccaa"),
			new StringMatch(8, 13, "bdddc"),
			new StringMatch(14, 17, "ccd"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	public void testRegexComplex3() throws Exception {
		StringFinder finder = findIn("xxxccaaxbdddccccdxaaxbbaaaxxd", "[a-b]*","[c-d]{3,}");
		assertThat(finder.findAll(), contains(
			new StringMatch(5, 7, "aa"),
			new StringMatch(8, 9, "b"),
			new StringMatch(9, 17, "dddccccd"),
			new StringMatch(18, 20, "aa"),
			new StringMatch(21, 26, "bbaaa")));
	}

	@Test
	public void testRegexComplex4() throws Exception {
		StringFinder finder = findIn("xxxccaaxbdddccccdxaaxbbaaaxxd", "d*","ddd*c");
		assertThat(finder.findAll(), contains(
			new StringMatch(9, 13, "dddc"),
			new StringMatch(16, 17, "d"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	public void testRegexComplex5() throws Exception {
		StringFinder finder = findIn("xxxccaaxbdddccccdxaaxbbaaaxxd", "(b|d)*(c|a){3}","cd");
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "cca"),
			new StringMatch(8, 15, "bdddccc"),
			new StringMatch(15, 17, "cd"),
			new StringMatch(21, 26, "bbaaa")));
	}

	private StringFinder findIn(String in, String... pattern) {
		MultiFactorRE algorithm = new MultiFactorRE(new AhoCorasick.Factory(), new GlushkovPrefixExtender.Factory(), pattern);
		return algorithm.createFinder(new StringCharProvider(in, 0), LONGEST_MATCH, NON_OVERLAP);
	}

}
