package net.amygdalum.stringsearchalgorithms.search;

import java.util.List;

/**
 * an interface for generic string search.
 * StringFinders are generated from a StringSearchAlgorithm together with a Char/ByteProvider (which is the document to search in).
 */
public interface StringFinder {

	/**
	 * @return the next match in the given document, the order of the matches is determined by the specific implementation
	 */
	StringMatch findNext();
	/**
	 * @return all matches in the given document, dependent on the specific implementation some matches may be skipped
	 */
	List<StringMatch> findAll();
	/**
	 * skips the document to a specific position (ignoring any match before this position)
	 * @param pos the new position to start with
	 */
	void skipTo(long pos);
}
