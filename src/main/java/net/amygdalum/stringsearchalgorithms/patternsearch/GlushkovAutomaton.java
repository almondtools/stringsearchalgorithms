package net.amygdalum.stringsearchalgorithms.patternsearch;

import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.map.BitSetObjectMap;
import net.amygdalum.util.map.CharObjectMap;

public class GlushkovAutomaton implements BitParallelAutomaton {

	private BitSet initial;
	private BitSet finals;
	private CharObjectMap<BitSet> reachableByChar;
	private BitSetObjectMap<BitSet> reachableByState;

	public GlushkovAutomaton(BitSet initial, BitSet finals, CharObjectMap<BitSet> reachableByChar, BitSetObjectMap<BitSet> reachableByState) {
		this.initial = initial;
		this.finals = finals;
		this.reachableByChar = reachableByChar;
		this.reachableByState = reachableByState;
	}

	@Override
	public char[] supportedChars() {
		return reachableByChar.keys();
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
		BitSet result = reachableByState.get(state);
		BitSet byChar = reachableByChar.get(c);

		result = (BitSet) result.clone();
		result.and(byChar);
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
			states.removeAll(done);
			next = new LinkedList<>();
			while(!states.isEmpty()) {
				BitSet current = states.remove();
				if (isFinal(current)) {
					return length;
				}
				done.add(current);
				for (char c : reachableByChar.keys()) {
					next.add(next(current, c));
				}
			}
			length++;
		}
		return length;
	}

}
