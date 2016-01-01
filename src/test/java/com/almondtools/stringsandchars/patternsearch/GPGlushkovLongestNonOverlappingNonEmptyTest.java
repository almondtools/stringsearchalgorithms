package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.stringsandchars.search.MatchOption.NON_EMPTY;
import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class GPGlushkovLongestNonOverlappingNonEmptyTest {

	@Test
	public void testRegexConcat() throws Exception {
		StringFinder finder = find("ab", "cccabcc");
		assertThat(finder.findAll(), contains(new StringMatch(3, 5, "ab")));
	}

	@Test
	public void testRegexOverlappingConcat() throws Exception {
		StringFinder finder = find("aba", "cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aba")));
	}

	@Test
	public void testRegexAlternatives() throws Exception {
		StringFinder finder = find("ab|ac", "cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab"),
			new StringMatch(7, 9, "ac")));
	}

	@Test
	public void testRegexAlternativesOverlapping() throws Exception {
		StringFinder finder = find("ab|ba", "cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab")));
	}

	@Test
	public void testRegexStar() throws Exception {
		StringFinder finder = find("a*", "cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aaa")));
	}

	@Test
	public void testRegexPlus() throws Exception {
		StringFinder finder = find("a+", "cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aaa")));
	}

	@Test
	public void testRegexOptional() throws Exception {
		StringFinder finder = find("ab?", "cccabacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexBoundedLoop() throws Exception {
		StringFinder finder = find("a{1,2}", "cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "aa"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexComplex() throws Exception {
		StringFinder finder = find("((a|b)*c{1,2})+", "abaccxaaccccbbcx");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 5, "abacc"),
			new StringMatch(6, 15, "aaccccbbc")
			));
	}

	@Test
	public void testRegexCharClasses() throws Exception {
		StringFinder finder = find("[a-b]+", "ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 4, "ab"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	public void testRegexCompClasses() throws Exception {
		StringFinder finder = find("[^a-b]+", "ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 2, "cc"),
			new StringMatch(4, 6, "cc"),
			new StringMatch(7, 9, "cc"),
			new StringMatch(10, 12, "cc")));
	}

	@Test
	public void testRegexOverlappingCharClasses() throws Exception {
		StringFinder finder = find("[a-b][b-c]", "aabbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(3, 5, "bc")));
	}

	private StringFinder find(String pattern, String in) {
		GPGlushkov algorithm = new GPGlushkov(pattern);
		return algorithm.createFinder(new StringCharProvider(in, 0), LONGEST_MATCH, NON_OVERLAP, NON_EMPTY);
	}

}
