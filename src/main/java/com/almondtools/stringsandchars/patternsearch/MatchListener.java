package com.almondtools.stringsandchars.patternsearch;

import com.almondtools.stringsandchars.io.CharProvider;

public interface MatchListener {

	void notify(long start, long end, CharProvider chars);

}
