package net.amygdalum.stringsearchalgorithms.patternsearch;

import java.util.BitSet;

public interface BitParallelAutomaton {

	char[] supportedChars();
	
	BitSet getInitial();

	boolean isInitial(BitSet state);

	BitSet next(BitSet state, char c);

	boolean isFinal(BitSet state);

	int minLength();

}
