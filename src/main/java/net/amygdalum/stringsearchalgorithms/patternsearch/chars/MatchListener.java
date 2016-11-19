package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;

public interface MatchListener {

	void notify(long start, long end, CharProvider chars);

}
