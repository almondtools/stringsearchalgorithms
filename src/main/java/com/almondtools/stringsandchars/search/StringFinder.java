package com.almondtools.stringsandchars.search;

import java.util.List;

/**
 * an interface for generic string search.
 * StringFinders are generated from a StringSearchAlgorithm together with a CharProvider (which is the document to search in).
 */
public interface StringFinder {

	/**
	 * @return the next match in the given document
	 */
	StringMatch findNext();
	/**
	 * @return all matches in the given document
	 */
	List<StringMatch> findAll();
	/**
	 * skips the document to a specific position (ignoring any match before this position)
	 * @param pos the new position to start with
	 */
	void skipTo(long pos);
}
