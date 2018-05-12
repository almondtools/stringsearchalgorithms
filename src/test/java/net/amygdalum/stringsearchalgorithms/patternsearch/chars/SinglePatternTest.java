package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_EMPTY;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.regexparser.RegexParserOption;
import net.amygdalum.stringsearchalgorithms.search.SearchFor;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.chars.AhoCorasick;

public class SinglePatternTest {

	@Rule
	public SinglePatternSearchRule searcher = new SinglePatternSearchRule(
		new BPGlushkov.Factory(RegexParserOption.DOT_ALL),
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovPrefixExtender.Factory(RegexParserOption.DOT_ALL), 4),
		new MultiFactorRE.Factory(new AhoCorasick.Factory(), new GlushkovFactorExtender.Factory(RegexParserOption.DOT_ALL), 2));

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
	@SearchFor("aba")
	public void testRegexOverlappingConcatNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccababacc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aba")));
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
	@SearchFor("ab|ba")
	public void testRegexAlternativesOverlappingNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccababacc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 7, "ab")));
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
	@SearchFor("a*")
	public void testRegexStarNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", NON_OVERLAP);

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
	@SearchFor("a*")
	public void testRegexStarLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 0, ""),
			new StringMatch(1, 1, ""),
			new StringMatch(2, 2, ""),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(7, 7, ""),
			new StringMatch(8, 8, "")));
	}

	@Test
	@SearchFor("a*")
	public void testRegexStarNonEmpty() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", NON_EMPTY);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(3, 5, "aa"),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(4, 5, "a"),
			new StringMatch(4, 6, "aa"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("a*")
	public void testRegexStarLongestNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 0, ""),
			new StringMatch(1, 1, ""),
			new StringMatch(2, 2, ""),
			new StringMatch(3, 6, "aaa"),
			new StringMatch(7, 7, ""),
			new StringMatch(8, 8, "")));
	}

	@Test
	@SearchFor("a*")
	public void testRegexStarLongestNonOverlappingNonEmpty() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", LONGEST_MATCH, NON_OVERLAP, NON_EMPTY);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aaa")));
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
	@SearchFor("a+")
	public void testRegexPlusNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("a+")
	public void testRegexPlusLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aaa")));
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
	@SearchFor("ab?")
	public void testRegexOptionalNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabacc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("ab?")
	public void testRegexOptionalLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabacc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "ab"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("ab?a")
	public void testRegexOptionalInfix() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 6, "aba"),
			new StringMatch(5, 7, "aa")));
	}

	@Test
	@SearchFor("ab*a")
	public void testRegexLoopInfix() throws Exception {
		StringFinder finder = searcher.createSearcher("cccabbaacc");

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 7, "abba"),
			new StringMatch(6, 8, "aa")));
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
	@SearchFor("a{1,2}")
	public void testRegexBoundedLoopNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 4, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(5, 6, "a")));
	}

	@Test
	@SearchFor("a{1,2}")
	public void testRegexBoundedLoopLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("cccaaacc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(3, 5, "aa"),
			new StringMatch(4, 6, "aa")));
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
	@SearchFor("((a|b)*c{1,2})+")
	public void testRegexComplexNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("abaccxaaccccbbcx", NON_OVERLAP);

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
	@SearchFor("((a|b)*c{1,2})+")
	public void testRegexComplexLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("abaccxaaccccbbcx", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 5, "abacc"),
			new StringMatch(6, 15, "aaccccbbc")));
	}

	@Test
	@SearchFor("((a|b)*c{1,2})+")
	public void testRegexComplexLongestNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("abaccxaaccccbbcx", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 5, "abacc"),
			new StringMatch(6, 15, "aaccccbbc")));
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
	@SearchFor("[a-b]+")
	public void testRegexCharClassesNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 3, "a"),
			new StringMatch(3, 4, "b"),
			new StringMatch(6, 7, "a"),
			new StringMatch(9, 10, "b")));
	}

	@Test
	@SearchFor("[a-b]+")
	public void testRegexCharClassesLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(2, 4, "ab"),
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
	@SearchFor("[^a-b]+")
	public void testRegexCompClassesNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc", NON_OVERLAP);

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
	@SearchFor("[^a-b]+")
	public void testRegexCompClassesLongest() throws Exception {
		StringFinder finder = searcher.createSearcher("ccabccaccbcc", LONGEST_MATCH);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 2, "cc"),
			new StringMatch(4, 6, "cc"),
			new StringMatch(7, 9, "cc"),
			new StringMatch(10, 12, "cc")));
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

	@Test
	@SearchFor("[a-b][b-c]")
	public void testRegexOverlappingCharClassesNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("aabbcc", NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(3, 5, "bc")));
	}

	@Test
	@SearchFor("[a-b][b-c]")
	public void testRegexOverlappingCharClassesLongestNonOverlapping() throws Exception {
		StringFinder finder = searcher.createSearcher("aabbcc", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(1, 3, "ab"),
			new StringMatch(3, 5, "bc")));
	}

	@Test
	@SearchFor("gacatagacattttagacataaaagacatagacaa|atagacaacatagacatagacatagacatagacatagacataga")
	public void testLongPattern() throws Exception {
		StringFinder finder = searcher.createSearcher("gcgcgcgcgacatagacattttagacataaaagacatagacaagcgcgcgcatagacaacatagacatagacatagacatagacatagacatagagcgcgcgcgc", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(8, 43, "gacatagacattttagacataaaagacatagacaa"),
			new StringMatch(51, 95, "atagacaacatagacatagacatagacatagacatagacataga")));
	}

	@Test
	@SearchFor("(A|a)nd (the )?(B|b)east")
	public void testKJBPatternOptional() throws Exception {
		StringFinder finder = searcher.createSearcher(
			"Therefore thus saith the Lord GOD; I will also stretch out mine hand upon Edom, and will cut off man and beast from it; and I will make it desolate from Teman; and they of Dedan shall fall by the sword.",
			LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(101, 110, "and beast")));
	}

	@Test
	@SearchFor("(A|a)nd (the ){0,1}(B|b)east")
	public void testKJBPatternOptionalAsLoop() throws Exception {
		StringFinder finder = searcher.createSearcher(
			"Therefore thus saith the Lord GOD; I will also stretch out mine hand upon Edom, and will cut off man and beast from it; and I will make it desolate from Teman; and they of Dedan shall fall by the sword.",
			LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(101, 110, "and beast")));
	}

	@Test
	@SearchFor("(A|a)nd (the )?(B|b)east")
	public void testKJBPatternNonOptional() throws Exception {
		StringFinder finder = searcher.createSearcher(
			"So that the fishes of the sea, and the fowls of the heaven, and the beasts of the field, and all creeping things that creep upon the earth, and all the men that are upon the face of the earth, shall shake at my presence, and the mountains shall be thrown down, and the steep places shall fall, and every wall shall fall to the ground. ",
			LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(60, 73, "and the beast")));
	}

	@Test
	@SearchFor("(a*tgc*|t*acg*)*(cg){1,20}(a|t)*")
	public void testEcoliLongestLeftmostKillingLongMatchUnhidingShortMatch() throws Exception {
		StringFinder finder = searcher.createSearcher("ggggcgattgcccggcagcatgatgtccaggcgattcacaat", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(4, 9, "cgatt"),
			new StringMatch(12, 14, "cg"),
			new StringMatch(31, 36, "cgatt")));
	}

	@Test
	@SearchFor("(a*tgc*|t*acg*)*(cg){1,20}(a|t)*")
	public void testEcoliFactorExtensionToBegin() throws Exception {
		StringFinder finder = searcher.createSearcher("tgcg", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(0, 4, "tgcg")));
	}

	@Test
	@SearchFor("(a*tgc*|t*acg*)*(cg){1,20}(a|t)*")
	public void testEcoliPrefixExtension() throws Exception {
		StringFinder finder = searcher.createSearcher("tgcaacgggcaatatgtctctgtgtggatt", LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(5, 7, "cg")));
	}

	@Test
	@SearchFor("(a*tgc*|t*acg*)*(cg){1,20}(a|t)*")
	public void testEcoliShortPrefixCancelsLong() throws Exception {
		StringFinder finder = searcher.createSearcher("attaccacaggtaacggtgcgggctg",
			LONGEST_MATCH, NON_OVERLAP);

		assertThat(finder.findAll(), contains(
			new StringMatch(13, 21, "acggtgcg")));
	}

}
