package com.almondtools.stringsandchars.search;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.StringSearchRule.SearchFor;

public class StringSearchAlgorithmTest {

	@Rule
	public StringSearchRule searcher = new StringSearchRule();

	@Test
	@SearchFor("a")
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abababab")).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a")));
	}

	@Test
	@SearchFor("abc")
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcababcab")).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "abc")));
	}
	
	@Test
	@SearchFor("abcabd")
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcabcabcabdabcabcabd")).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 12, "abcabd"),
			new StringMatch(15, 21, "abcabd")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxabcabcabcxxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab"),
			new StringMatch(6, 11, "abcab")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxabcabcabcabxxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab"),
			new StringMatch(6, 11, "abcab"),
			new StringMatch(9, 14, "abcab")));
	}
	
	@Test
	@SearchFor("ab")
	public void testPattern6() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxaaaababaaxxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 8, "ab"),
			new StringMatch(8, 10, "ab")));
	}
	
	@Test
	@SearchFor("a\u0262ba")
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxaaaa\u0262ba\u0262baaxxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 10, "a\u0262ba"),
			new StringMatch(9, 13, "a\u0262ba")));
	}
	
	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")
	public void testPatternLargeSize() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")));
	}
	
	@Test
	@SearchFor("axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")
	public void testPatternLargeSizeAndAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxx axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb xxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")));
	}
	
	@Test
	@SearchFor("a")
	public void testNonOverlappingPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abababab"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a")));
	}

	@Test
	@SearchFor("abc")
	public void testNonOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcababcab"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "abc")));
	}
	
	@Test
	@SearchFor("abcabd")
	public void testNonOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcabcabcabdabcabcabd"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 12, "abcabd"),
			new StringMatch(15, 21, "abcabd")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testNonOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxabcabcabcxxx"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testNonOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxabcabcabcabxxx"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab"),
			new StringMatch(9, 14, "abcab")));
	}
	
	@Test
	@SearchFor("ab")
	public void testNonOverlappingPattern6() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxaaaababaaxxx"), LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 8, "ab"),
			new StringMatch(8, 10, "ab")));
	}
	
	private StringCharProvider chars(String input) {
		return new StringCharProvider(input,0);
	}

}
