package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import java.util.List;
import java.util.SortedSet;

import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;

/**
 * FactorExtenders can extend factors of a pattern found in a character source to the complete pattern (if such exists). 
 */
public interface FactorExtender {

	/**
	 * @return the pattern that will be extended with this extender
	 */
	String getPattern();

	/**
	 * @return the minimum match length the pattern would match
	 */
	int getPatternLength();

	/**
	 * computes the factors considered best for the given pattern and a given maximum length. Each such factor must not be longer than max.
	 * 
	 * @param max the maximum length that the result factors should comply to
	 * @return the factors
	 */
	List<String> getBestFactors(int max);

	/**
	 * extends a given factor found in a character source to a pattern match in this character source (if such exists)
	 * 
	 * @param chars a CharProvider with position just behind the current factor
	 * @param longest true if this method should return the longest patterns including this factor, otherwise are returned
	 * @return the matches that include the param factor
	 */
	SortedSet<StringMatch> extendFactor(CharProvider chars, boolean longest);

	/**
	 * initializes this FactorExtender with the matched factor. Each factor returned by getBestFactors should be a valid input param. Also
	 * "" (empty string) should init the extender in such a way that any best factor could be matched
	 * 
	 * @param factor the factor this matcher will be able to extend
	 * @return the initialized factor extender
	 */
	FactorExtender forFactor(String factor);

	/**
	 * @param factor the factor to compare to the pattern
	 * @return true if the pattern can contain the given factor, false otherwise
	 */
	boolean hasFactor(String factor);

}
