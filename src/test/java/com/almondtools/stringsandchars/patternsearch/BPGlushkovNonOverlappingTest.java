package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class BPGlushkovNonOverlappingTest {

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
		StringFinder finder = findIn("cccaaacc", "a+");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexOptional() throws Exception {
		StringFinder finder = findIn("cccabacc", "ab?");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexBoundedLoop() throws Exception {
		StringFinder finder = findIn("cccaaacc", "a{1,2}");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	public void testRegexComplex() throws Exception {
		StringFinder finder = findIn("abaccxaaccccbbcx", "((a|b)*c{1,2})+");

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
		StringFinder finder = findIn("ccabccaccbcc", "[a-b]+");

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 3, "a"),
			new StringMatch(3, 4, "b"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	public void testRegexCompClasses() throws Exception {
		StringFinder finder = findIn("ccabccaccbcc", "[^a-b]+");

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
		StringFinder finder = findIn("aabbcc", "[a-b][b-c]");

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(3, 5, "bc")));
	}

	private StringFinder findIn(String in, String pattern) {
		BPGlushkov algorithm = new BPGlushkov(pattern);
		return algorithm.createFinder(new StringCharProvider(in, 0), NON_OVERLAP);
	}

}
