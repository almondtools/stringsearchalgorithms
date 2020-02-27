package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import net.amygdalum.stringsearchalgorithms.search.MatchOption;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.chars.AhoCorasick;
import net.amygdalum.stringsearchalgorithms.search.chars.SetBackwardOracleMatching;
import net.amygdalum.stringsearchalgorithms.search.chars.StringSearchAlgorithm;
import net.amygdalum.stringsearchalgorithms.search.chars.WuManber;
import net.amygdalum.util.io.StringCharProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;


public class LongestMatchMultiPatternTest {
  
  final String text = "Die Krankheit ist seit dem Aufkommen wirksamer Antibiotika selten geworden";
  final List<String> patterns =  Arrays.asList("Antibiotik", "Antibiotika"); // we assume to find only the longest match
  
  @Test
  public void testLongestStringSearchAhoCorasick() {
    StringSearchAlgorithm algorithm = new AhoCorasick(patterns);
    StringFinder finder = algorithm.createFinder(new StringCharProvider(text, 0), MatchOption.LONGEST_MATCH, MatchOption.NON_OVERLAP);
    assertThat(finder.findAll(), containsInAnyOrder(new StringMatch(47, 58, "Antibiotika")));
  }
  
  @Test
  public void testLongestStringSearchWuManber() {
    StringSearchAlgorithm stringSearch = new WuManber(patterns);
    StringFinder finder = stringSearch.createFinder(new StringCharProvider(text, 0), MatchOption.LONGEST_MATCH, MatchOption.NON_OVERLAP);
    assertThat(finder.findAll(), containsInAnyOrder(new StringMatch(47, 58, "Antibiotika")));
  }
  
  @Test
  public void testLongestStringSearchSetBackwardOracleMatching() {
    StringSearchAlgorithm stringSearch = new SetBackwardOracleMatching(patterns);
    StringFinder finder = stringSearch.createFinder(new StringCharProvider(text, 0), MatchOption.LONGEST_MATCH, MatchOption.NON_OVERLAP);
    assertThat(finder.findAll(), containsInAnyOrder(new StringMatch(47, 58, "Antibiotika")));
  }
  
  @Test
  public void testStringSearchSetBackwardOracleMatching() {
    StringSearchAlgorithm stringSearch = new SetBackwardOracleMatching(patterns);
    StringFinder finder = stringSearch.createFinder(new StringCharProvider(text, 0));
    assertThat(finder.findAll(), containsInAnyOrder(
      new StringMatch(47, 57, "Antibiotik"),
      new StringMatch(47, 58, "Antibiotika"))
    );
  }
  
}
