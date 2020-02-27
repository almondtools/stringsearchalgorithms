package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static net.amygdalum.stringsearchalgorithms.search.chars.CaseInsensitive.caseInsensitive;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.search.SearchFor;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;

public class CaseInsensitiveMultiStringSearchAlgorithmTest {

	@Rule
	public MultiStringSearchRule searcher = new MultiStringSearchRule(
		caseInsensitive(new AhoCorasick.Factory()),
		caseInsensitive(new SetHorspool.Factory()),
		caseInsensitive(new WuManber.Factory()),
		caseInsensitive(new SetBackwardOracleMatching.Factory()));

	@Test
	@SearchFor({ "a", "B" })
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(1, 2, "b"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a"),
			new StringMatch(7, 8, "b")));
	}

	@Test
	@SearchFor({ "ab", "Ac" })
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aBAcacab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "aB"),
			new StringMatch(2, 4, "Ac"),
			new StringMatch(4, 6, "ac"),
			new StringMatch(6, 8, "ab")));
	}

	@Test
	@SearchFor({ "abC", "Bcd" })
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcacbcdacabcdaabc").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "bcd"),
			new StringMatch(10, 13, "abc"),
			new StringMatch(11, 14, "bcd"),
			new StringMatch(15, 18, "abc")));
	}

	@Test
	@SearchFor({ "bbcc", "ccbb" })
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbCCBBccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "bbCC"),
			new StringMatch(2, 6, "CCBB"),
			new StringMatch(4, 8, "BBcc"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({ "bBCc", "cCbB" })
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(2, 6, "ccbb"),
			new StringMatch(4, 8, "bbcc"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({ "abc", "cd", "Defghi", "gh" })
	public void testPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcdghcdefcdefGhiabcd").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 3, "abc"),
			new StringMatch(2, 4, "cd"),
			new StringMatch(4, 6, "gh"),
			new StringMatch(6, 8, "cd"),
			new StringMatch(10, 12, "cd"),
			new StringMatch(11, 17, "defGhi"),
			new StringMatch(14, 16, "Gh"),
			new StringMatch(17, 20, "abc"),
			new StringMatch(19, 21, "cd")));
	}

	@Test
	@SearchFor({ "aBcd", "ab", "bc", "cd" })
	public void testSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 2, "ab"),
			new StringMatch(1, 3, "bc"),
			new StringMatch(0, 4, "abcd"),
			new StringMatch(2, 4, "cd")));
	}

	@Test
	@SearchFor({ "aaa", "aa", "a" })
	public void testSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaA").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 1, "a"),
			new StringMatch(0, 2, "aa"),
			new StringMatch(0, 3, "aaa"),
			new StringMatch(1, 2, "a"),
			new StringMatch(1, 3, "aa"),
			new StringMatch(1, 4, "aaA"),
			new StringMatch(2, 3, "a"),
			new StringMatch(2, 4, "aA"),
			new StringMatch(3, 4, "A")));
	}

	@Test
	@SearchFor({ "a", "b" })
	public void testOverlappingPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(1, 2, "b"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a"),
			new StringMatch(7, 8, "b")));
	}

	@Test
	@SearchFor({ "ab", "ac" })
	public void testOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "ab"),
			new StringMatch(2, 4, "ac"),
			new StringMatch(4, 6, "ac"),
			new StringMatch(6, 8, "ab")));
	}

	@Test
	@SearchFor({ "abc", "bcd" })
	public void testOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcacbcdacabcdaabc", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "bcd"),
			new StringMatch(10, 13, "abc"),
			new StringMatch(15, 18, "abc")));
	}

	@Test
	@SearchFor({ "bbcc", "ccbb" })
	public void testOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "bbcc"),
			new StringMatch(4, 8, "bbcc")));
	}

	@Test
	@SearchFor({ "bbcc", "ccbb" })
	public void testOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(2, 6, "ccbb"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({ "abc", "cd", "defghi", "gh" })
	public void testOverlappingPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcdghcdefcdefghiabcd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 3, "abc"),
			new StringMatch(4, 6, "gh"),
			new StringMatch(6, 8, "cd"),
			new StringMatch(10, 12, "cd"),
			new StringMatch(14, 16, "gh"),
			new StringMatch(17, 20, "abc")));
	}

	@Test
	@SearchFor({ "abcd", "ab", "bc", "cd" })
	public void testOverlappingSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 4, "abcd")));
	}

	@Test
	@SearchFor({ "aaa", "aa", "a" })
	public void testOverlappingSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaa", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 3, "aaa"),
			new StringMatch(3, 4, "a")));
	}

	@Test
	@SearchFor({ "aa\u0262ba", "a\u0262baA" })
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaa\u0262ba\u0262baaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(5, 10, "aa\u0262ba"),
			new StringMatch(9, 14, "a\u0262baa")));
	}

	@Test
	@SearchFor("a")
	public void testPatternLength1() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(1));
	}

	@Test
	@SearchFor("ab")
	public void testPatternLength2() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(2));
	}

	@Test
	@SearchFor("abcab")
	public void testPatternLength5() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(5));
	}

}
