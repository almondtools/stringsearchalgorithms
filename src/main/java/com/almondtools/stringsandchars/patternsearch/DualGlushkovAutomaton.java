package com.almondtools.stringsandchars.patternsearch;

import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.almondtools.util.map.BitSetObjectMap;
import com.almondtools.util.map.CharObjectMap;

public class DualGlushkovAutomaton implements BitParallelAutomaton {

	private BitSet initial;
	private BitSet finals;
	private CharObjectMap<BitSet> emittingChar;
	private BitSetObjectMap<BitSet> reachableByState;

	public DualGlushkovAutomaton(BitSet initial, BitSet finals, CharObjectMap<BitSet> emittingChar, BitSetObjectMap<BitSet> reachableByState) {
		this.initial = initial;
		this.finals = finals;
		this.emittingChar = emittingChar;
		this.reachableByState = reachableByState;
	}

	@Override
	public BitSet getInitial() {
		return initial;
	}

	@Override
	public boolean isInitial(BitSet state) {
		return initial.equals(state);
	}

	@Override
	public BitSet next(BitSet state, char c) {
		BitSet result = state;
		BitSet byChar = emittingChar.get(c);

		result = (BitSet) result.clone();
		result.and(byChar);
		result = reachableByState.get(result);
		return result;
	}

	@Override
	public boolean isFinal(BitSet state) {
		BitSet result = (BitSet) finals.clone();
		result.and(state);
		return !result.isEmpty();
	}

	@Override
	public int minLength() {
		int length = 0;
		Set<BitSet> done = new HashSet<>();
		Queue<BitSet> next = new LinkedList<>();
		next.add(getInitial());
		while (!next.isEmpty()) {
			Queue<BitSet> states = next;
			next = new LinkedList<>();
			while(!states.isEmpty()) {
				BitSet current = states.remove();
				if (isFinal(current)) {
					return length;
				}
				done.add(current);
				for (char c : emittingChar.keys()) {
					next.add(next(current, c));
				}
			}
			length++;
		}
		return Integer.MAX_VALUE;
	}

}
