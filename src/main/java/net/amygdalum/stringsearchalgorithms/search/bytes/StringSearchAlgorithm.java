package net.amygdalum.stringsearchalgorithms.search.bytes;

import net.amygdalum.stringsearchalgorithms.io.ByteProvider;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;

/**
 * a generic interface for string search algorithms (both single- and multi-string-algorithms implement this)
 * 
 *  classes implementing this interface should incorporate the pattern to search.
 */
public interface StringSearchAlgorithm {

	/**
	 * creates a StringFinder instance from the pattern (given by the current instance) and the given document.
	 * @param bytes the given document wrapped into a ByteProvider
	 * @param options the configuration options for the string finder
	 * @return a StringFinder that can iterate over the matches
	 */
	StringFinder createFinder(ByteProvider bytes, StringFinderOption... options);

	/**
	 * @return the length of the given pattern (min length in case of a multi string algorithm)
	 */
	int getPatternLength();

}
