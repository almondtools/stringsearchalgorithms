package com.almondtools.stringsandchars.search;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.MultiStringSearchRule.SearchFor;

public class MultiStringSearchAlgorithmTest {

	@Rule
	public MultiStringSearchRule searcher = new MultiStringSearchRule();

	@Test
	@SearchFor({"a","b"})
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abacacab")).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(1, 2, "b"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a"),
			new StringMatch(7, 8, "b")));
	}
	
	@Test
	@SearchFor({"ab","ac"})
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abacacab")).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "ab"),
			new StringMatch(2, 4, "ac"),
			new StringMatch(4, 6, "ac"),
			new StringMatch(6, 8, "ab")));
	}
	
	@Test
	@SearchFor({"abc","bcd"})
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcacbcdacabcdaabc")).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "bcd"),
			new StringMatch(10, 13, "abc"),
			new StringMatch(11, 14, "bcd"),
			new StringMatch(15, 18, "abc")));
	}
	
	@Test
	@SearchFor({"abc","cd","defghi","gh"})
	public void testPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcdghcdefcdefghiabcd")).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 3, "abc"),
			new StringMatch(2, 4, "cd"),
			new StringMatch(4, 6, "gh"),
			new StringMatch(6, 8, "cd"),
			new StringMatch(10, 12, "cd"),
			new StringMatch(11, 17, "defghi"),
			new StringMatch(14, 16, "gh"),
			new StringMatch(17, 20, "abc"),
			new StringMatch(19, 21, "cd")));
	}
	
	@Test
	@SearchFor({"abcd","ab","bc","cd"})
	public void testSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("abcd")).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 2, "ab"),
			new StringMatch(1, 3, "bc"),
			new StringMatch(0, 4, "abcd"),
			new StringMatch(2, 4, "cd")));
	}
	
	@Test
	@SearchFor({"aaa","aa","a"})
	public void testSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(chars("aaaa")).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 1, "a"),
			new StringMatch(0, 2, "aa"),
			new StringMatch(0, 3, "aaa"),
			new StringMatch(1, 2, "a"),
			new StringMatch(1, 3, "aa"),
			new StringMatch(1, 4, "aaa"),
			new StringMatch(2, 3, "a"),
			new StringMatch(2, 4, "aa"),
			new StringMatch(3, 4, "a")));
	}
	
	private StringCharProvider chars(String input) {
		return new StringCharProvider(input,0);
	}

}
