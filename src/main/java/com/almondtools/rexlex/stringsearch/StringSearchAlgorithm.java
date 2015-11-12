package com.almondtools.rexlex.stringsearch;

import com.almondtools.rexlex.io.CharProvider;

public interface StringSearchAlgorithm {

	StringFinder createFinder(CharProvider chars);

	int getPatternLength();

}
