package com.almondtools.stringsandchars.patternsearch;

import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.StringMatch;

public interface PrefixPatternMatcher {

	int getPatternLength();

	List<String> getPrefixes(int max);

	List<StringMatch> match(CharProvider chars, boolean longest);

	PrefixPatternMatcher withPrefix(String prefix);

}
