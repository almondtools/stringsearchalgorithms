package net.amygdalum.stringsearchalgorithms.search.bytes;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static net.amygdalum.stringsearchalgorithms.search.bytes.MultiStringSearchAlgorithmMatcher.isMultiStringSearchAlgorithm;
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
		new AhoCorasick.Factory(),
		new SetHorspool.Factory(),
		new WuManber.Factory(),
		new SetBackwardOracleMatching.Factory());

	@Test
	@SearchFor({"x"})
	public void testAlgorithmDesign() throws Exception {
		assertThat(searcher.getAlgorithm().getClass(), isMultiStringSearchAlgorithm());
	}
	
	@Test
	@SearchFor({"a","b"})
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "a"),
			new StringMatch(2, 4, "b"),
			new StringMatch(4, 6, "a"),
			new StringMatch(8, 10, "a"),
			new StringMatch(12, 14, "a"),
			new StringMatch(14, 16, "b")));
	}
	
	@Test
	@SearchFor({"ab","ac"})
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "ab"),
			new StringMatch(4, 8, "ac"),
			new StringMatch(8, 12, "ac"),
			new StringMatch(12, 16, "ab")));
	}
	
	@Test
	@SearchFor({"abc","bcd"})
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcacbcdacabcdaabc").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 6, "abc"),
			new StringMatch(10, 16, "bcd"),
			new StringMatch(20, 26, "abc"),
			new StringMatch(22, 28, "bcd"),
			new StringMatch(30, 36, "abc")));
	}
	
	@Test
	@SearchFor({"bbcc","ccbb"})
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbccbbccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 8, "bbcc"),
			new StringMatch(4, 12, "ccbb"),
			new StringMatch(8, 16, "bbcc"),
			new StringMatch(12, 20, "ccbb")));
	}
	
	@Test
	@SearchFor({"bbcc","ccbb"})
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 12, "ccbb"),
			new StringMatch(8, 16, "bbcc"),
			new StringMatch(12, 20, "ccbb")));
	}
	
	@Test
	@SearchFor({"abc","cd","defghi","gh"})
	public void testPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcdghcdefcdefghiabcd").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 6, "abc"),
			new StringMatch(4, 8, "cd"),
			new StringMatch(8, 12, "gh"),
			new StringMatch(12, 16, "cd"),
			new StringMatch(20, 24, "cd"),
			new StringMatch(22, 34, "defghi"),
			new StringMatch(28, 32, "gh"),
			new StringMatch(34, 40, "abc"),
			new StringMatch(38, 42, "cd")));
	}
	
	@Test
	@SearchFor({"And God called the firmament Heaven","Let the waters under the heaven be gathered together unto one place","And God called the dry land Earth"})
	public void testDifferentLengths() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "And God called the firmament Heaven. And the evening and the morning were the second day.\n"
			+ "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so.\n"
			+ "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 70, "And God called the firmament Heaven"),
			new StringMatch(208, 342, "Let the waters under the heaven be gathered together unto one place"),
			new StringMatch(434, 500, "And God called the dry land Earth")));
	}
	
	@Test
	@SearchFor({"abcd","ab","bc","cd"})
	public void testSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 4, "ab"),
			new StringMatch(2, 6, "bc"),
			new StringMatch(0, 8, "abcd"),
			new StringMatch(4, 8, "cd")));
	}
	
	@Test
	@SearchFor({"aaa","aa","a"})
	public void testSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaa").findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 2, "a"),
			new StringMatch(0, 4, "aa"),
			new StringMatch(0, 6, "aaa"),
			new StringMatch(2, 4, "a"),
			new StringMatch(2, 6, "aa"),
			new StringMatch(2, 8, "aaa"),
			new StringMatch(4, 6, "a"),
			new StringMatch(4, 8, "aa"),
			new StringMatch(6, 8, "a")));
	}
	
	@Test
	@SearchFor({"ab", "abc"})
	public void testLongestMatchOnSubsumingPatternsInIncreasingLengthOrder() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abc", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(new StringMatch(0, 6, "abc")));
	}

	@Test
	@SearchFor({"abc", "ab"})
	public void testLongestMatchOnSubsumingPatternsInDecreasingLengthOrder() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abc", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(new StringMatch(0, 6, "abc")));
	}

	@Test
	@SearchFor({"a","b"})
	public void testOverlappingPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "a"),
			new StringMatch(2, 4, "b"),
			new StringMatch(4, 6, "a"),
			new StringMatch(8, 10, "a"),
			new StringMatch(12, 14, "a"),
			new StringMatch(14, 16, "b")));
	}
	
	@Test
	@SearchFor({"ab","ac"})
	public void testOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abacacab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 4, "ab"),
			new StringMatch(4, 8, "ac"),
			new StringMatch(8, 12, "ac"),
			new StringMatch(12, 16, "ab")));
	}
	
	@Test
	@SearchFor({"abc","bcd"})
	public void testOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcacbcdacabcdaabc", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 6, "abc"),
			new StringMatch(10, 16, "bcd"),
			new StringMatch(20, 26, "abc"),
			new StringMatch(30, 36, "abc")));
	}
	
	@Test
	@SearchFor({"bbcc","ccbb"})
	public void testOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("bbccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 8, "bbcc"),
			new StringMatch(8, 16, "bbcc")));
	}
	
	@Test
	@SearchFor({"bbcc","ccbb"})
	public void testOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abccbbccbb", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(4, 12, "ccbb"),
			new StringMatch(12, 20, "ccbb")));
	}
	
	@Test
	@SearchFor({"abc","cd","defghi","gh"})
	public void testOverlappingPatternDifferentLengthSubsumingAndOverlapping() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcdghcdefcdefghiabcd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 6, "abc"),
			new StringMatch(8, 12, "gh"),
			new StringMatch(12, 16, "cd"),
			new StringMatch(20, 24, "cd"),
			new StringMatch(28, 32, "gh"),
			new StringMatch(34, 40, "abc")));
	}
	
	@Test
	@SearchFor({"abcd","ab","bc","cd"})
	public void testOverlappingSubsumingPatterns1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 8, "abcd")));
	}
	
	@Test
	@SearchFor({"aaa","aa","a"})
	public void testOverlappingSubsumingPatterns2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("aaaa", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, containsInAnyOrder(
			new StringMatch(0, 6, "aaa"),
			new StringMatch(6, 8, "a")));
	}
	
	
	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa")
	public void testPatternMiddleSize1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(8, 96, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa"),
			new StringMatch(48, 136, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa")));
	}

	@Test
	@SearchFor({"1001110000001001000000101101011100011011101110001101110010110110","1110100011101001110000001011110111000010011110111110110000011111"})
	public void testPatternMiddleSize2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
            + "1001110000001001000000101101011100011011101110001101110010110110"
            + "1110100011101001110000001011110111000010011110111110110000011111"
			+ "010101111111100001001011011101111").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 128, "1001110000001001000000101101011100011011101110001101110010110110"),
			new StringMatch(128, 256, "1110100011101001110000001011110111000010011110111110110000011111")));
	}

	@Test
	@SearchFor({"GGCTCATA","CATACATC"})
	public void testPatternMiddleSize3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "CCTTTAAGTAGGACAA"
			+ "GGCTCATACATC"
			+ "GTATTAGCTCAGCATG", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(32, 48, "GGCTCATA")));
	}
	
	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")
	public void testPatternLargeSize1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(8, 166, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")));
	}

	@Test
	@SearchFor({"10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111","10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"})
	public void testPatternLargeSize2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111"
			+ "0101"
			+ "10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"
			+ "010101111111100001001011011101111").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 256, "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111"),
			new StringMatch(264, 520, "10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010")));
	}

	@Test
	@SearchFor({"10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011","10011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"})
	public void testPatternLargeSize3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "0101"
			+ "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011010111001010111111110000100101101110111100010111101110000100111101111101100000111110101011111111000010"
			+ "010101111111100001001011011101111", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(8, 264, "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011100000010010000001011")));
	}

	@Test
	@SearchFor({"aa\u0262ba", "a\u0262baa"})
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaa\u0262ba\u0262baaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(10, 20, "aa\u0262ba"),
			new StringMatch(18, 28, "a\u0262baa")));
	}
	
	@Test
	@SearchFor("a")
	public void testPatternLength1() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(2));
	}
	
	@Test
	@SearchFor("ab")
	public void testPatternLength2() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(4));
	}
	
	@Test
	@SearchFor("abcab")
	public void testPatternLength5() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(10));
	}

}
