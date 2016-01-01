package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class GPGlushkovNonOverlappingTest {

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
			new StringMatch(0, 0, ""),
			new StringMatch(1, 1, ""),
			new StringMatch(2, 2, ""),
			new StringMatch(3, 3, ""),
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 4, ""),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 5, ""),
			new StringMatch(5, 6, "a"),
			new StringMatch(6, 6, ""),
			new StringMatch(7, 7, ""),
			new StringMatch(8, 8, "")));
	}

	@Test
	public void testRegexPlus() throws Exception {
		StringFinder finder = find("a+", "cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexOptional() throws Exception {
		StringFinder finder = find("ab?", "cccabacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexBoundedLoop() throws Exception {
		StringFinder finder = find("a{1,2}", "cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexComplex() throws Exception {
		StringFinder finder = find("((a|b)*c{1,2})+", "abaccxaaccccbbcx");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 4, "abac"),
			new StringMatch(4, 5, "c"),
			new StringMatch(6, 9, "aac"),
			new StringMatch(9, 10, "c"),
			new StringMatch(10, 11, "c"),
			new StringMatch(11, 12, "c"),
			new StringMatch(12, 15, "bbc")));
	}

	@Test
	public void testRegexCharClasses() throws Exception {
		StringFinder finder = find("[a-b]+", "ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 3, "a"),
			new StringMatch(3, 4, "b"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	public void testRegexCompClasses() throws Exception {
		StringFinder finder = find("[^a-b]+", "ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 1, "c"),
			new StringMatch(1, 2, "c"),
			new StringMatch(4, 5, "c"),
			new StringMatch(5, 6, "c"),
			new StringMatch(7, 8, "c"),
			new StringMatch(8, 9, "c"),
			new StringMatch(10, 11, "c"),
			new StringMatch(11, 12, "c")));
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
		return algorithm.createFinder(new StringCharProvider(in, 0), NON_OVERLAP);
	}

}
