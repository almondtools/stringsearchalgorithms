package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static net.amygdalum.stringsearchalgorithms.search.chars.MultiStringSearchAlgorithmMatcher.isMultiStringSearchAlgorithm;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.search.SearchFor;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;

public class MultiStringSearchAlgorithmTest {

	@Rule
	public MultiStringSearchRule searcher = new MultiStringSearchRule(
//		new AhoCorasick.Factory(),
		new SetHorspool.Factory()//,
//		new SetHorspool.Factory(true),
//		new WuManber.Factory(),
//		new SetBackwardOracleMatching.Factory(),
//		new QGramShiftOr.Factory()
		);

	@Test
	@SearchFor({"x"})
	public void testAlgorithmDesign() throws Exception {
		assertThat(searcher.getAlgorithm().getClass(), isMultiStringSearchAlgorithm());
	}

	@Test
	@SearchFor({"a", "b"})
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
	@SearchFor({"ab", "ac"})
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "ab"),
			new StringMatch(2, 4, "ac"),
			new StringMatch(4, 6, "ac"),
			new StringMatch(6, 8, "ab")));
	}

	@Test
	@SearchFor({"abc", "bcd"})
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
	@SearchFor({"bbcc", "ccbb"})
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbccbbccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "bbcc"),
			new StringMatch(2, 6, "ccbb"),
			new StringMatch(4, 8, "bbcc"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({"bbcc", "ccbb"})
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(2, 6, "ccbb"),
			new StringMatch(4, 8, "bbcc"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({"abc", "cd", "defghi", "gh"})
	public void testPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcdghcdefcdefghiabcd").findAll();
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
	@SearchFor({"abcd", "ab", "bc", "cd"})
	public void testSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 2, "ab"),
			new StringMatch(1, 3, "bc"),
			new StringMatch(0, 4, "abcd"),
			new StringMatch(2, 4, "cd")));
	}

	@Test
	@SearchFor({"aaa", "aa", "a"})
	public void testSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaa").findAll();
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

	@Test
	@SearchFor({"a", "b"})
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
	@SearchFor({"ab", "ac"})
	public void testOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "ab"),
			new StringMatch(2, 4, "ac"),
			new StringMatch(4, 6, "ac"),
			new StringMatch(6, 8, "ab")));
	}

	@Test
	@SearchFor({"abc", "bcd"})
	public void testOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcacbcdacabcdaabc", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "abc"),
			new StringMatch(5, 8, "bcd"),
			new StringMatch(10, 13, "abc"),
			new StringMatch(15, 18, "abc")));
	}

	@Test
	@SearchFor({"bbcc", "ccbb"})
	public void testOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "bbcc"),
			new StringMatch(4, 8, "bbcc")));
	}

	@Test
	@SearchFor({"bbcc", "ccbb"})
	public void testOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(2, 6, "ccbb"),
			new StringMatch(6, 10, "ccbb")));
	}

	@Test
	@SearchFor({"abc", "cd", "defghi", "gh"})
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
	@SearchFor({"And God called the firmament Heaven", "Let the waters under the heaven be gathered together unto one place", "And God called the dry land Earth"})
	public void testLongPatternsDifferentLengths() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "And God called the firmament Heaven. And the evening and the morning were the second day.\n"
			+ "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so.\n"
			+ "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 35, "And God called the firmament Heaven"),
			new StringMatch(104, 171, "Let the waters under the heaven be gathered together unto one place"),
			new StringMatch(217, 250, "And God called the dry land Earth")));
	}

	@Test
	@SearchFor({"and wood to fire", "Treasures of wickedness profit nothing", "Then shalt thou enquire", "fire"})
	public void testReverseSubsumptions() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "As coals are to burning coals, and wood to fire; so is a contentious man to kindle strife.").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(31, 47, "and wood to fire"),
			new StringMatch(43, 47, "fire")));
	}

	@Test
	@SearchFor({"abcd", "ab", "bc", "cd"})
	public void testOverlappingSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 4, "abcd")));
	}

	@Test
	@SearchFor({"aaa", "aa", "a"})
	public void testOverlappingSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaa", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 3, "aaa"),
			new StringMatch(3, 4, "a")));
	}

	@Test
	@SearchFor({"aa\u0262ba", "a\u0262baa"})
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaa\u0262ba\u0262baaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(5, 10, "aa\u0262ba"),
			new StringMatch(9, 14, "a\u0262baa")));
	}

	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa")
	public void testPatternMiddleSize1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 48, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa"),
			new StringMatch(24, 68, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa")));
	}

	@Test
	@SearchFor({"1001110000001001000000101101011100011011101110001101110010110110", "1110100011101001110000001011110111000010011110111110110000011111"})
	public void testPatternMiddleSize2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "1001110000001001000000101101011100011011101110001101110010110110"
			+ "1110100011101001110000001011110111000010011110111110110000011111"
			+ "010101111111100001001011011101111").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 64, "1001110000001001000000101101011100011011101110001101110010110110"),
			new StringMatch(64, 128, "1110100011101001110000001011110111000010011110111110110000011111")));
	}

	@Test
	@SearchFor({"GGCTCATA", "CATACATC"})
	public void testPatternMiddleSize3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "CCTTTAAGTAGGACAA"
			+ "GGCTCATACATC"
			+ "GTATTAGCTCAGCATG", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(16, 24, "GGCTCATA")));
	}

	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")
	public void testPatternLargeSize1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")));
	}

	@Test
	@SearchFor({"10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111",
		"10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"})
	public void testPatternLargeSize2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111"
			+ "0101"
			+ "10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"
			+ "010101111111100001001011011101111").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 128, "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111"),
			new StringMatch(132, 260, "10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010")));
	}

	@Test
	@SearchFor({"10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011",
		"10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"})
	public void testPatternLargeSize3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "0101"
			+ "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"
			+ "010101111111100001001011011101111", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(4, 132, "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011")));
	}

	@Test
	@SearchFor("axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")
	public void testPatternLargeSizeAndAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")));
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
