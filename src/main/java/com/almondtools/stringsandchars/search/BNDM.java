package com.almondtools.stringsandchars.search;

import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;
import static java.util.Arrays.fill;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.util.map.CharLongMap;
import com.almondtools.util.map.CharObjectMap;

/**
 * An implementation of the String Search Algorithm BNDM (Backward Nondeterministic Dawg Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BNDM implements StringSearchAlgorithm {

	private int patternLength;
	private BitMapStates states;

	public BNDM(String pattern) {
		this.patternLength = pattern.length();
		this.states = computeStates(pattern.toCharArray());
	}

	private static BitMapStates computeStates(char[] pattern) {
		if (isCompactRange(pattern)) {
			if (pattern.length > 64) {
				return new QuickMultiLongStates(pattern);
			} else {
				return new QuickSingleLongStates(pattern);
			}
		} else {
			if (pattern.length > 64) {
				return new SmartMultiLongStates(pattern);
			} else {
				return new SmartSingleLongStates(pattern);
			}
		}
	}

	private static boolean isCompactRange(char[] pattern) {
		char minChar = computeMinChar(pattern);
		char maxChar = computeMaxChar(pattern);
		return maxChar - minChar < 256 || maxChar - minChar < pattern.length * 2;
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (states.supportsSingle()) {
			return new LongFinder(chars, options);
		} else {
			return new MultiLongFinder(chars, options);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends AbstractStringFinder {

		protected final long finalstate;
		protected final long activeStates;

		protected CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.finalstate = 1l << ((patternLength - 1) % 64);
			this.activeStates = (finalstate - 1) | finalstate;
			this.chars = chars;
		}

		protected StringMatch createMatch() {
			long start = chars.current();
			long end = start + patternLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private class LongFinder extends Finder {

		private long state;

		public LongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.state = activeStates;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
			state = activeStates;
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished(patternLength - 1)) {
				state = activeStates;
				int j = patternLength - 1;
				int last = patternLength;
				while (state != 0l) {
					char currentChar = chars.lookahead(j);
					long single = states.single(currentChar);
					state &= single;
					if ((state & finalstate) != 0l) {
						if (j > 0) {
							last = j;
						} else {
							StringMatch createMatch = createMatch();
							chars.forward(last);
							return createMatch;
						}
					}
					j--;
					state = (state << 1) & activeStates;
				}
				chars.forward(last);
			}
			return null;
		}

	}

	private class MultiLongFinder extends Finder {

		private long[] state;

		public MultiLongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.state = initial(patternLength);
		}

		private long[] initial(int patternLength) {
			return init(new long[((patternLength - 1) / 64) + 1]);
		}

		private long[] init(long[] state) {
			fill(state, -1l);
			state[0] = activeStates;
			return state;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
			init(state);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished(patternLength - 1)) {
				state = initial(patternLength);
				int j = patternLength - 1;
				int last = patternLength;
				while (zero(state)) {
					char currentChar = chars.lookahead(j);
					long[] all = states.all(currentChar);
					state = join(state, all);
					if ((state[0] & finalstate) != 0l) {
						if (j > 0) {
							last = j;
						} else {
							StringMatch createMatch = createMatch();
							chars.forward(last);
							return createMatch;
						}
					}
					j--;
					state = next(state);
				}
				chars.forward(last);
			}
			return null;
		}

		private long[] next(long[] state) {
			for (int i = 0; i < state.length; i++) {
				int j = i + 1;
				long leastBit = j < state.length ? state[j] >>> 63 : 0l;
				state[i] = state[i] << 1 | leastBit;
			}
			state[0] &= activeStates;
			return state;
		}

		private boolean zero(long[] state) {
			for (int i = 0; i < state.length; i++) {
				if (state[i] != 0l) {
					return true;
				}
			}
			return false;
		}

		private long[] join(long[] state, long[] bits) {
			for (int i = 0; i < state.length; i++) {
				state[i] = state[i] & bits[i];
			}
			return state;
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BNDM(pattern);
		}

	}

	private abstract static class SingleLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return true;
		}

		@Override
		public long[] all(char c) {
			return new long[] { single(c) };
		}

	}

	private static class QuickSingleLongStates extends SingleLongBitMapStates {

		private char minChar;
		private char maxChar;
		private long[] characters;

		public QuickSingleLongStates(char[] pattern) {
			this.minChar = computeMinChar(pattern);
			this.maxChar = computeMaxChar(pattern);
			this.characters = computeStates(pattern, this.minChar, this.maxChar);
		}

		private static long[] computeStates(char[] pattern, char min, char max) {
			long[] characters = new long[max - min + 1];
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				characters[c - min] |= 1l << j;
			}
			return characters;
		}

		@Override
		public long single(char c) {
			if (c < minChar || c > maxChar) {
				return 0l;
			}
			return characters[c - minChar];
		}

	}

	private static class SmartSingleLongStates extends SingleLongBitMapStates {

		private CharLongMap states;

		public SmartSingleLongStates(char[] pattern) {
			this.states = computeStates(pattern);
		}

		private static CharLongMap computeStates(char[] pattern) {
			CharLongMap.Builder mapBuilder = new CharLongMap.Builder(0l);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				long newState = mapBuilder.get(c) | (1l << j);
				mapBuilder.put(c, newState);
			}
			return mapBuilder.perfectMinimal();
		}

		@Override
		public long single(char c) {
			return states.get(c);
		}

	}

	private abstract static class MultiLongBitMapStates implements BitMapStates {

		public static long[] computeZero(int length) {
			return new long[((length - 1) / 64) + 1];
		}

		@Override
		public boolean supportsSingle() {
			return false;
		}

		@Override
		public long single(char c) {
			throw new UnsupportedOperationException();
		}

	}

	private static class QuickMultiLongStates extends MultiLongBitMapStates {

		private char minChar;
		private char maxChar;
		private long[][] characters;
		private long[] zero;

		public QuickMultiLongStates(char[] pattern) {
			this.minChar = computeMinChar(pattern);
			this.maxChar = computeMaxChar(pattern);
			this.characters = computeStates(pattern, this.minChar, this.maxChar);
			this.zero = computeZero(pattern.length);
		}

		private static long[][] computeStates(char[] pattern, char min, char max) {
			long[][] characters = new long[max - min + 1][];
			for (int c = min; c <= max; c++) {
				characters[c - min] = computeZero(pattern.length);
			}
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				int slot = ((pattern.length - 1) / 64) - j / 64;
				int offset = j % 64;
				characters[c - min][slot] |= 1l << offset;
			}
			return characters;
		}

		@Override
		public long[] all(char c) {
			if (c < minChar || c > maxChar) {
				return zero;
			}
			return characters[c - minChar];
		}

	}

	private static class SmartMultiLongStates extends MultiLongBitMapStates {

		private CharObjectMap<long[]> states;

		public SmartMultiLongStates(char[] pattern) {
			this.states = computeStates(pattern);
		}

		private static CharObjectMap<long[]> computeStates(char[] pattern) {
			long[] zero = computeZero(pattern.length);
			CharObjectMap.Builder<long[]> mapBuilder = new CharObjectMap.Builder<>(zero);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				int slot = ((pattern.length - 1) / 64) - j / 64;
				int offset = j % 64;
				long[] newState = mapBuilder.get(c);
				if (newState == zero) {
					newState = computeZero(pattern.length);
				}
				newState[slot] |= 1l << offset;
				mapBuilder.put(c, newState);
			}
			return mapBuilder.perfectMinimal();
		}

		@Override
		public long[] all(char c) {
			return states.get(c);
		}

	}

}
