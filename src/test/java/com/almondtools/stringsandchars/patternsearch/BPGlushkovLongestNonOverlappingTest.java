package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class BPGlushkovLongestNonOverlappingTest {

	@Test
	public void testRegexConcat() throws Exception {
		StringFinder finder = findIn("cccabcc", "ab");
		assertThat(finder.findAll(), contains(new StringMatch(3, 5, "ab")));
	}

	@Test
	public void testRegexOverlappingConcat() throws Exception {
		StringFinder finder = findIn("cccababacc", "aba");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aba")));
	}

	@Test
	public void testRegexAlternatives() throws Exception {
		StringFinder finder = findIn("cccababacc", "ab|ac");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab"),
			new StringMatch(7, 9, "ac")));
	}

	@Test
	public void testRegexAlternativesOverlapping() throws Exception {
		StringFinder finder = findIn("cccababacc", "ab|ba");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab")));
	}

	@Test
	public void testRegexStar() throws Exception {
		StringFinder finder = findIn("cccaaacc", "a*");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 0, ""),
			new StringMatch(1, 1, ""),
			new StringMatch(2, 2, ""),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(7, 7, ""),
			new StringMatch(8, 8, "")));
	}

	@Test
	public void testRegexPlus() throws Exception {
		StringFinder finder = findIn("cccaaacc", "a+");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aaa")));
	}

	@Test
	public void testRegexOptional() throws Exception {
		StringFinder finder = findIn("cccabacc", "ab?");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexBoundedLoop() throws Exception {
		StringFinder finder = findIn("cccaaacc", "a{1,2}");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "aa"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexComplex() throws Exception {
		StringFinder finder = findIn("abaccxaaccccbbcx", "((a|b)*c{1,2})+");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 5, "abacc"),
			new StringMatch(6, 15, "aaccccbbc")
			));
	}

	@Test
	public void testRegexCharClasses() throws Exception {
		StringFinder finder = findIn("ccabccaccbcc", "[a-b]+");

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 4, "ab"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	public void testRegexCompClasses() throws Exception {
		StringFinder finder = findIn("ccabccaccbcc", "[^a-b]+");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 2, "cc"),
			new StringMatch(4, 6, "cc"),
			new StringMatch(7, 9, "cc"),
			new StringMatch(10, 12, "cc")));
	}

	@Test
	public void testRegexOverlappingCharClasses() throws Exception {
		StringFinder finder = findIn("aabbcc", "[a-b][b-c]");

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(3, 5, "bc")));
	}

	private StringFinder findIn(String in, String pattern) {
		BPGlushkov algorithm = new BPGlushkov(pattern);
		return algorithm.createFinder(new StringCharProvider(in, 0), LONGEST_MATCH, NON_OVERLAP);
	}

}
