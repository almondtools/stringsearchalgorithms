package com.almondtools.stringsandchars.search;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * a generic interface for string search algorithms (both single- and multi-string-algorithms implement this)
 * 
 *  classes implementing this interface should incorporate the pattern to search.
 */
public interface StringSearchAlgorithm {

	/**
	 * creates a StringFinder instance from the pattern (given by the current instance) and the given document.
	 * @param chars the given document wrapped into a CharProvider
	 * @return a StringFinder that can iterate over the matches
	 */
	StringFinder createFinder(CharProvider chars);

	/**
	 * @return the length of the given pattern (min length in case of a multi string algorithm)
	 */
	int getPatternLength();

}
