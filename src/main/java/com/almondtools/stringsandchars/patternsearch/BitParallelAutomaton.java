package com.almondtools.stringsandchars.patternsearch;

import java.util.BitSet;

public interface BitParallelAutomaton {

	BitSet getInitial();

	boolean isInitial(BitSet state);

	BitSet next(BitSet state, char c);

	boolean isFinal(BitSet state);

	int minLength();

}
