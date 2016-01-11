package com.almondtools.stringsandchars.patternsearch;

import java.util.List;
import java.util.SortedSet;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.StringMatch;

public interface FactorExtender {

	String getPattern();

	int getPatternLength();

	List<String> getBestFactors(int max);

	SortedSet<StringMatch> extendFactor(CharProvider chars, boolean longest);

	FactorExtender forFactor(String factor);

	boolean hasFactor(String factor);

}
