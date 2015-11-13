package com.almondtools.stringsandchars.search;

import com.almondtools.stringsandchars.io.CharProvider;

public interface StringSearchAlgorithm {

	StringFinder createFinder(CharProvider chars);

	int getPatternLength();

}
