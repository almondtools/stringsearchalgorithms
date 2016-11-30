package net.amygdalum.stringsearchalgorithms.search.bytes;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static net.amygdalum.stringsearchalgorithms.search.bytes.StringSearchAlgorithmMatcher.isByteStringSearchAlgorithm;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.search.SearchFor;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;


public class StringSearchAlgorithmTest {

	@Rule
	public StringSearchRule searcher = new StringSearchRule(
		new ShiftAnd.Factory(),
		new ShiftOr.Factory(),
		new KnuthMorrisPratt.Factory(),
		new Horspool.Factory(),
		new Sunday.Factory(),
		new BNDM.Factory(),
		new BOM.Factory());

	@Test
	@SearchFor("x")
	public void testAlgorithmDesign() throws Exception {
		assertThat(searcher.getAlgorithm().getClass(), isByteStringSearchAlgorithm());
	}
	
	@Test
	@SearchFor("a")
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abababab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "a"),
			new StringMatch(4, 6, "a"),
			new StringMatch(8, 10, "a"),
			new StringMatch(12, 14, "a")));
	}

	@Test
	@SearchFor("abc")
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcababcab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 6, "abc"),
			new StringMatch(10, 16, "abc")));
	}
	
	@Test
	@SearchFor("abcabd")
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcabcabcabdabcabcabd").findAll();
		assertThat(matches, contains(
			new StringMatch(12, 24, "abcabd"),
			new StringMatch(30, 42, "abcabd")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcabcxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(6, 16, "abcab"),
			new StringMatch(12, 22, "abcab")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcabcabxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(6, 16, "abcab"),
			new StringMatch(12, 22, "abcab"),
			new StringMatch(18, 28, "abcab")));
	}
	
	@Test
	@SearchFor("ab")
	public void testPattern6() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaababaaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(12, 16, "ab"),
			new StringMatch(16, 20, "ab")));
	}
	
	@Test
	@SearchFor("a\u0262ba")
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaa\u0262ba\u0262baaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(12, 20, "a\u0262ba"),
			new StringMatch(18, 26, "a\u0262ba")));
	}
	
	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")
	public void testPatternLargeSize1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(8, 166, "aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")));
	}
	
	@Test
	@SearchFor("10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111")
	public void testPatternLargeSize2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher(""
			+ "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111"
			+ "010101111111100001001011011101111").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 256, "10011100000010010000001011010111000110111011100011011100101101101110100011101001110000001011110111000010011110111110110000011111")));
	}

	@Test
	@SearchFor("axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")
	public void testPatternLargeSizeAndAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(8, 166, "axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")));
	}
	
	@Test
	@SearchFor("a")
	public void testNonOverlappingPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abababab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 2, "a"),
			new StringMatch(4, 6, "a"),
			new StringMatch(8, 10, "a"),
			new StringMatch(12, 14, "a")));
	}

	@Test
	@SearchFor("abc")
	public void testNonOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcababcab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 6, "abc"),
			new StringMatch(10, 16, "abc")));
	}
	
	@Test
	@SearchFor("abcabd")
	public void testNonOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcabcabcabdabcabcabd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(12, 24, "abcabd"),
			new StringMatch(30, 42, "abcabd")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testNonOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcabcxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 16, "abcab")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testNonOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcabcabxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 16, "abcab"),
			new StringMatch(18, 28, "abcab")));
	}
	
	@Test
	@SearchFor("ab")
	public void testNonOverlappingPattern6() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaababaaxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(12, 16, "ab"),
			new StringMatch(16, 20, "ab")));
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
