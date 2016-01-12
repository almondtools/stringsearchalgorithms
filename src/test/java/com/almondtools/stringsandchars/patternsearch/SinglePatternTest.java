package com.almondtools.stringsandchars.patternsearch;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.almondtools.stringsandchars.search.AhoCorasick;
import com.almondtools.stringsandchars.search.SearchFor;
import com.almondtools.stringsandchars.search.StringFinder;
import com.almondtools.stringsandchars.search.StringMatch;

public class SinglePatternTest {

	@Rule
	public SinglePatternSearchRule searcher = new SinglePatternSearchRule(
		new BPGlushkov.Factory(),
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovPrefixExtender.Factory(), 2),
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovFactorExtender.Factory(), 2));

	@Test
	@SearchFor("ab")
	public void testRegexConcat() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabcc");
		assertThat(finder.findAll(), contains(new StringMatch(3, 5, "ab")));
	}

	@Test
	@SearchFor("aba")
	public void testRegexOverlappingConcat() throws Exception {
		StringFinder finder = searcher.createSearcher("cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aba"),
			new StringMatch(5, 8, "aba")));
	}

	@Test
	@SearchFor("ab|ac")
	public void testRegexAlternatives() throws Exception {
		StringFinder finder = searcher.createSearcher("cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab"),
			new StringMatch(7, 9, "ac")));
	}

	@Test
	@SearchFor("ab|ba")
	public void testRegexAlternativesOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccababacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(4, 6, "ba"),
			new StringMatch(5, 7, "ab"),
			new StringMatch(6, 8, "ba")));
	}

	@Test
	@SearchFor("a*")
	public void testRegexStar() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 0, ""),
			new StringMatch(1, 1, ""),
			new StringMatch(2, 2, ""),
			new StringMatch(3, 3, ""),
			new StringMatch(3, 4, "a"),
			new StringMatch(3, 5, "aa"),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(4, 4, ""),
			new StringMatch(4, 5, "a"),
			new StringMatch(4, 6, "aa"),
			new StringMatch(5, 5, ""),
			new StringMatch(5, 6, "a"),
			new StringMatch(6, 6, ""),
			new StringMatch(7, 7, ""),
			new StringMatch(8, 8, "")));
	}

	@Test
	@SearchFor("a+")
	public void testRegexPlus() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(3, 5, "aa"),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(4, 5, "a"),
			new StringMatch(4, 6, "aa"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("ab?")
	public void testRegexOptional() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("a{1,2}")
	public void testRegexBoundedLoop() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(3, 5, "aa"),
			new StringMatch(4, 5, "a"),
			new StringMatch(4, 6, "aa"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("((a|b)*c{1,2})+")
	public void testRegexComplex() throws Exception {
		StringFinder finder = searcher.createSearcher("abaccxaaccccbbcx");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 4, "abac"),
			new StringMatch(0, 5, "abacc"),
			new StringMatch(1, 4, "bac"),
			new StringMatch(1, 5, "bacc"),
			new StringMatch(2, 4, "ac"),
			new StringMatch(2, 5, "acc"),
			new StringMatch(3, 4, "c"),
			new StringMatch(3, 5, "cc"),
			new StringMatch(4, 5, "c"),
			new StringMatch(6, 9, "aac"),
			new StringMatch(6, 10, "aacc"),
			new StringMatch(6, 11, "aaccc"),
			new StringMatch(6, 12, "aacccc"),
			new StringMatch(6, 15, "aaccccbbc"),
			new StringMatch(7, 9, "ac"),
			new StringMatch(7, 10, "acc"),
			new StringMatch(7, 11, "accc"),
			new StringMatch(7, 12, "acccc"),
			new StringMatch(7, 15, "accccbbc"),
			new StringMatch(8, 9, "c"),
			new StringMatch(8, 10, "cc"),
			new StringMatch(8, 11, "ccc"),
			new StringMatch(8, 12, "cccc"),
			new StringMatch(8, 15, "ccccbbc"),
			new StringMatch(9, 10, "c"),
			new StringMatch(9, 11, "cc"),
			new StringMatch(9, 12, "ccc"),
			new StringMatch(9, 15, "cccbbc"),
			new StringMatch(10, 11, "c"),
			new StringMatch(10, 12, "cc"),
			new StringMatch(10, 15, "ccbbc"),
			new StringMatch(11, 12, "c"),
			new StringMatch(11, 15, "cbbc"),
			new StringMatch(12, 15, "bbc"),
			new StringMatch(13, 15, "bc"),
			new StringMatch(14, 15, "c")));
	}

	@Test
	@SearchFor("[a-b]+")
	public void testRegexCharClasses() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 3, "a"),
			new StringMatch(2, 4, "ab"),
			new StringMatch(3, 4, "b"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	@SearchFor("[^a-b]+")
	public void testRegexCompClasses() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 1, "c"),
			new StringMatch(0, 2, "cc"),
			new StringMatch(1, 2, "c"),
			new StringMatch(4, 5, "c"),
			new StringMatch(4, 6, "cc"),
			new StringMatch(5, 6, "c"),
			new StringMatch(7, 8, "c"),
			new StringMatch(7, 9, "cc"),
			new StringMatch(8, 9, "c"),
			new StringMatch(10, 11, "c"),
			new StringMatch(10, 12, "cc"),
			new StringMatch(11, 12, "c")));
	}

	@Test
	@SearchFor("[a-b][b-c]")
	public void testRegexOverlappingCharClasses() throws Exception {
		StringFinder finder = searcher.createSearcher("aabbcc");

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(2, 4, "bb"),
			new StringMatch(3, 5, "bc")));
	}

}
