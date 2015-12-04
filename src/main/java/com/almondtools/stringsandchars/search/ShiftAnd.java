package com.almondtools.stringsandchars.search;

import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the String Search Algorithm of Shift-And (or Baeza-Yates–Gonnet).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class ShiftAnd implements StringSearchAlgorithm {

	private int patternLength;
	private LongStates longMap;
	private LongArrayStates bitsetMap;

	public ShiftAnd(String pattern) {
		this.patternLength = pattern.length();
		this.longMap = computeLongMap(pattern.toCharArray());
		this.bitsetMap = computeBitSetMap(pattern.toCharArray());
	}

	private static LongStates computeLongMap(char[] pattern) {
		if (pattern.length > 64) {
			return null;
		}
		char minChar = computeMinChar(pattern);
		char maxChar = computeMaxChar(pattern);
		if (maxChar - minChar < 256 || maxChar - minChar < pattern.length * 2) {
			return new QuickCharLong(pattern, minChar, maxChar);
		} else {
			return new SmartCharLong(pattern, minChar, maxChar);
		}
	}

	private static LongArrayStates computeBitSetMap(char[] pattern) {
		if (pattern.length <= 64) {
			return null;
		}
		char minChar = computeMinChar(pattern);
		char maxChar = computeMaxChar(pattern);
		if (maxChar - minChar < 256 || maxChar - minChar < pattern.length * 2) {
			return new QuickCharLongArray(pattern, minChar, maxChar);
		} else {
			return new SmartCharLongArray(pattern, minChar, maxChar);
		}
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		if (longMap != null) {
			return new LongFinder(chars);
		} else {
			return new BitSetFinder(chars);
		}
	}

	private class LongFinder extends AbstractStringFinder {

		private long state;
		private long finalstate;
		private CharProvider chars;

		public LongFinder(CharProvider chars) {
			this.state = 0;
			this.finalstate = 1l << (patternLength - 1);
			this.chars = chars;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long bits = longMap.get(nextChar);

				state = (state << 1 | 1l) & bits;

				if ((state & finalstate) != 0l) {
					return createMatch();
				}
			}
			return null;
		}

		private StringMatch createMatch() {
			long end = chars.current();
			long start = end - patternLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}
	}

	private class BitSetFinder extends AbstractStringFinder {

		private long[] state;
		private long finalstate;
		private CharProvider chars;

		public BitSetFinder(CharProvider chars) {
			this.state = new long[((patternLength - 1) / 64) + 1];
			this.finalstate = 1 << ((patternLength - 1) % 64);
			this.chars = chars;
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long[] bits = bitsetMap.get(nextChar);

				state = next(state, bits);

				if ((state[0] & finalstate) != 0l) {
					return createMatch();
				}
			}
			return null;
		}

		private long[] next(long[] state, long bits[]) {
			for (int i = 0; i < state.length; i++) {
				int j = i + 1;
				long leastBit = j < state.length ? state[j] >>> 63 : 1l;
				state[i] = (state[i] << 1 | leastBit) & bits[i];
			}
			return state;
		}

		private StringMatch createMatch() {
			long end = chars.current();
			long start = end - patternLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	public static class Factory implements WordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new ShiftAnd(pattern);
		}

	}

	private static class QuickCharLong implements LongStates {

		private char minChar;
		private char maxChar;
		private long[] characters;

		public QuickCharLong(char[] pattern, char minChar, char maxChar) {
			this.minChar = minChar;
			this.maxChar = maxChar;
			this.characters = computeBitMap(pattern, this.minChar, this.maxChar);
		}

		private static long[] computeBitMap(char[] pattern, char min, char max) {
			long[] characters = new long[max - min + 1];
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				characters[c - min] |= 1l << i;
			}
			return characters;
		}

		@Override
		public long get(char c) {
			if (c < minChar || c > maxChar) {
				return 0l;
			}
			return characters[c - minChar];
		}

	}

	private static class SmartCharLong implements LongStates {

		private CharLongMap states;

		public SmartCharLong(char[] pattern, char minChar, char maxChar) {
			this.states = computeStates(pattern, minChar, maxChar);
		}

		private static CharLongMap computeStates(char[] pattern, char min, char max) {
			CharLongMap.Builder mapBuilder = new CharLongMap.Builder(0);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				long newState = mapBuilder.get(c) | 1l << i;
				mapBuilder.put(c, newState);
			}
			return mapBuilder.build();
		}

		@Override
		public long get(char c) {
			return states.get(c);
		}

	}

	private static class QuickCharLongArray implements LongArrayStates {

		private char minChar;
		private char maxChar;
		private long[][] characters;
		private long[] zero;

		public QuickCharLongArray(char[] pattern, char minChar, char maxChar) {
			this.minChar = minChar;
			this.maxChar = maxChar;
			this.characters = computeBitMap(pattern, this.minChar, this.maxChar);
			this.zero = computeZero(pattern.length);
		}

		private static long[][] computeBitMap(char[] pattern, char min, char max) {
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

		private static long[] computeZero(int length) {
			return new long[((length - 1) / 64) + 1];
		}

		@Override
		public long[] get(char c) {
			if (c < minChar || c > maxChar) {
				return zero;
			}
			return characters[c - minChar];
		}

	}

	private static class SmartCharLongArray implements LongArrayStates {
		
		private CharLongArrayMap states;

		public SmartCharLongArray(char[] pattern, char minChar, char maxChar) {
			this.states = computeStates(pattern, minChar, maxChar);
		}

		private static CharLongArrayMap computeStates(char[] pattern, char min, char max) {
			long[] zero = computeZero(pattern.length);
			CharLongArrayMap.Builder mapBuilder = new CharLongArrayMap.Builder(zero);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
				long[] newState = mapBuilder.get(c);
				if (newState == zero) {
					newState = computeZero(pattern.length);
				}
				newState[slot] |= 1l << offset;
				mapBuilder.put(c, newState);
			}
			return mapBuilder.build();
		}

		private static long[] computeZero(int length) {
			return new long[((length - 1) / 64) + 1];
		}

		@Override
		public long[] get(char c) {
			return states.get(c);
		}
		
	}
	
}
