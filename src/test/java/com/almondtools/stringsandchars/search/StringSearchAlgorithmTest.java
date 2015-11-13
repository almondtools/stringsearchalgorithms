package com.almondtools.stringsandchars.search;

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
	public void testOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("xxxabcabcabcxxx")).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab"),
			new StringMatch(6, 11, "abcab")));
	}
	
	private StringCharProvider chars(String input) {
		return new StringCharProvider(input,0);
	}

}
