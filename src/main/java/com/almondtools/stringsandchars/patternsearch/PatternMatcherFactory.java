package com.almondtools.stringsandchars.patternsearch;


public interface PatternMatcherFactory {

	PrefixPatternMatcher of(String pattern);

}
