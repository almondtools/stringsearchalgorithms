package com.almondtools.stringsandchars.patternsearch;

import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.StringMatch;

public interface FactorExtender {

	int getPatternLength();

	List<String> getBestFactors(int max);

	List<StringMatch> extendFactor(CharProvider chars, boolean longest);

	FactorExtender forFactor(String factor);

}
