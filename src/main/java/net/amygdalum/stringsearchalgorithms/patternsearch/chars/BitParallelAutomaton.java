package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import net.amygdalum.util.bits.BitSet;

public interface BitParallelAutomaton {

	char[] supportedChars();
	
	BitSet getInitial();

	boolean isInitial(BitSet state);

	BitSet next(BitSet state, char c);

	boolean isFinal(BitSet state);

	int minLength();

}
