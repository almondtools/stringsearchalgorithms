package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.stringsandchars.search.MatchOption.NON_EMPTY;
import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.almondtools.stringsandchars.search.AhoCorasick;
import com.almondtools.stringsandchars.search.SearchFor;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class MultiPatternTest {

	@Rule
	public MultiPatternSearchRule searcher = new MultiPatternSearchRule(
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovPrefixExtender.Factory(), 2),
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovFactorExtender.Factory(), 2)
		);

	@Test
	@SearchFor({ "a+", "b+", "c+", "d+" })
	public void testRegexComplex1() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP);
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
	@SearchFor({ "c{1,2}(a|d)+", "d+c{3}?", "(b|d)+c" })
	public void testRegexComplex2() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP);
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 7, "ccaa"),
			new StringMatch(8, 13, "bdddc"),
			new StringMatch(14, 17, "ccd"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	@SearchFor({ "[a-b]*", "[c-d]{3,}" })
	public void testRegexComplex3() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP, NON_EMPTY);
		assertThat(finder.findAll(), contains(
			new StringMatch(5, 7, "aa"),
			new StringMatch(8, 9, "b"),
			new StringMatch(9, 17, "dddccccd"),
			new StringMatch(18, 20, "aa"),
			new StringMatch(21, 26, "bbaaa")));
	}

	@Test
	@SearchFor({ "d*", "ddd*c" })
	public void testRegexComplex4() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP, NON_EMPTY);
		assertThat(finder.findAll(), contains(
			new StringMatch(9, 13, "dddc"),
			new StringMatch(16, 17, "d"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	@SearchFor({ "(b|d)*(c|a){3}", "cd" })
	public void testRegexComplex5() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP);
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "cca"),
			new StringMatch(8, 15, "bdddccc"),
			new StringMatch(15, 17, "cd"),
			new StringMatch(21, 26, "bbaaa")));
	}

	@Test
	@SearchFor({ "d", "d*cc" })
	public void testRegexComplex6() throws Exception {
		StringFinder finder = searcher.createSearcher("xxxccaaxbdddccccdxaaxbbaaaxxd", LONGEST_MATCH, NON_OVERLAP);
		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "cc"),
			new StringMatch(9, 14, "dddcc"),
			new StringMatch(14, 16, "cc"),
			new StringMatch(16, 17, "d"),
			new StringMatch(28, 29, "d")));
	}

	@Test
	@SearchFor({ "gacatagacattttagacataaaagacatagacaa", "atagacaacatagacatagacatagacatagacatagacataga" })
	public void testLongPatterns() throws Exception {
		StringFinder finder = searcher.createSearcher("gcgcgcgcgacatagacattttagacataaaagacatagacaagcgcgcgcatagacaacatagacatagacatagacatagacatagacatagagcgcgcgcgc", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(8, 43, "gacatagacattttagacataaaagacatagacaa"),
			new StringMatch(51, 95, "atagacaacatagacatagacatagacatagacatagacataga")));
	}

}
