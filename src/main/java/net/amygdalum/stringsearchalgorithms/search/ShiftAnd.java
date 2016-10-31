package net.amygdalum.stringsearchalgorithms.search;

import static java.util.Arrays.fill;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.util.map.CharLongMap;
import net.amygdalum.util.map.CharObjectMap;

/**
 * An implementation of the String Search Algorithm Shift-And (or Baeza-Yates–Gonnet).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class ShiftAnd implements StringSearchAlgorithm {

	private int patternLength;
	private BitMapStates states;

	public ShiftAnd(String pattern) {
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

	public static boolean isCompactRange(char[] pattern) {
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
		protected CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.finalstate = 1l << ((patternLength - 1) % 64);
			this.chars = chars;
		}

		protected StringMatch createMatch() {
			long end = chars.current();
			long start = end - patternLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private class LongFinder extends Finder {

		private long state;

		public LongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.state = 0;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			state = 0;
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long bits = states.single(nextChar);

				state = (state << 1 | 1l) & bits;

				if ((state & finalstate) != 0l) {
					return createMatch();
				}
			}
			return null;
		}

	}

	private class MultiLongFinder extends Finder {

		private long[] state;

		public MultiLongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.state = new long[((patternLength - 1) / 64) + 1];
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			fill(state, 0l);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long[] bits = states.all(nextChar);

				state = next(state, bits);

				if ((state[0] & finalstate) != 0l) {
					return createMatch();
				}
			}
			return null;
		}

		private long[] next(long[] state, long[] bits) {
			for (int i = 0; i < state.length; i++) {
				int j = i + 1;
				long leastBit = j < state.length ? state[j] >>> 63 : 1l;
				state[i] = (state[i] << 1 | leastBit) & bits[i];
			}
			return state;
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new ShiftAnd(pattern);
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
				characters[c - min] |= 1l << i;
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
			CharLongMap map = new CharLongMap(0l);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				long newState = map.get(c) | 1l << i;
				map.put(c, newState);
			}
			return map;
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
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
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
			CharObjectMap<long[]> map = new CharObjectMap<>(zero);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
				long[] newState = map.get(c);
				if (newState == zero) {
					newState = computeZero(pattern.length);
				}
				newState[slot] |= 1l << offset;
				map.put(c, newState);
			}
			return map;
		}

		@Override
		public long[] all(char c) {
			return states.get(c);
		}

	}

}
