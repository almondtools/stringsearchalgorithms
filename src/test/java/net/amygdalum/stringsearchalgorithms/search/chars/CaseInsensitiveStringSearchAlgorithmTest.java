package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;
import static net.amygdalum.stringsearchalgorithms.search.chars.CaseInsensitive.caseInsensitive;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.search.SearchFor;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;

public class CaseInsensitiveStringSearchAlgorithmTest {

	@Rule
	public StringSearchRule searcher = new StringSearchRule(
		caseInsensitive(new ShiftAnd.Factory()),
		caseInsensitive(new KnuthMorrisPratt.Factory()),
		caseInsensitive(new Horspool.Factory()),
		caseInsensitive(new Sunday.Factory()),
		caseInsensitive(new BNDM.Factory()),
		caseInsensitive(new BOM.Factory()));

	@Test
	@SearchFor("a")
	public void testPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abababab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a")));
	}

	@Test
	@SearchFor("A")
	public void testPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abababab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "a"),
			new StringMatch(2, 3, "a"),
			new StringMatch(4, 5, "a"),
			new StringMatch(6, 7, "a")));
	}

	@Test
	@SearchFor("abc")
	public void testPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("Abcababcab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "Abc"),
			new StringMatch(5, 8, "abc")));
	}
	
	@Test
	@SearchFor("aBc")
	public void testPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("AbcabaBCab").findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "Abc"),
			new StringMatch(5, 8, "aBC")));
	}
	
	@Test
	@SearchFor("abcaB")
	public void testPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcAbcabxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab"),
			new StringMatch(6, 11, "abcAb"),
			new StringMatch(9, 14, "Abcab")));
	}
	
	@Test
	@SearchFor("a\u0262bA")
	public void testPatternLargeAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaa\u0262Ba\u0262baaxxx").findAll();
		assertThat(matches, contains(
			new StringMatch(6, 10, "a\u0262Ba"),
			new StringMatch(9, 13, "a\u0262ba")));
	}
	
	@Test
	@SearchFor("aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa BBBB bbbb")
	public void testPatternLargeSize() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx aaaa aaaa bbbb bbbb AAAA aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "aaaa aaaa bbbb bbbb AAAA aaaa bbbb bbbb aaaa aaaa bbbb bbbb aaaa aaaa bbbb bbbb")));
	}
	
	@Test
	@SearchFor("axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxXxxxb")
	public void testPatternLargeSizeAndAlphabet() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxx axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb xxx").findAll();
		assertThat(matches, contains(
			new StringMatch(4, 83, "axxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\u0262xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxb")));
	}
	
	@Test
	@SearchFor("a")
	public void testNonOverlappingPattern1() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("ABABABAB", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 1, "A"),
			new StringMatch(2, 3, "A"),
			new StringMatch(4, 5, "A"),
			new StringMatch(6, 7, "A")));
	}

	@Test
	@SearchFor("abc")
	public void testNonOverlappingPattern2() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("ABCababcab", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(0, 3, "ABC"),
			new StringMatch(5, 8, "abc")));
	}
	
	@Test
	@SearchFor("abCabd")
	public void testNonOverlappingPattern3() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("abcabcabcabDabcabcabd", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 12, "abcabD"),
			new StringMatch(15, 21, "abcabd")));
	}
	
	@Test
	@SearchFor("abcab")
	public void testNonOverlappingPattern4() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxabcabcabcxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "abcab")));
	}
	
	@Test
	@SearchFor("Abcab")
	public void testNonOverlappingPattern5() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaBcabcabcabxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(3, 8, "aBcab"),
			new StringMatch(9, 14, "abcab")));
	}
	
	@Test
	@SearchFor("ab")
	public void testNonOverlappingPattern6() throws Exception {
		List<StringMatch> matches = searcher.createSearcher("xxxaaaABabaaxxx", LONGEST_MATCH, NON_OVERLAP).findAll();
		assertThat(matches, contains(
			new StringMatch(6, 8, "AB"),
			new StringMatch(8, 10, "ab")));
	}
	
	@Test
	@SearchFor("a")
	public void testPatternLength1() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(1));
	}
	
	@Test
	@SearchFor("A")
	public void testPatternLength2() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(1));
	}
	
	@Test
	@SearchFor("ab")
	public void testPatternLength3() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(2));
	}

	@Test
	@SearchFor("ab")
	public void testPatternLength4() throws Exception {
		assertThat(searcher.getAlgorithm().getPatternLength(), equalTo(2));
	}

}
