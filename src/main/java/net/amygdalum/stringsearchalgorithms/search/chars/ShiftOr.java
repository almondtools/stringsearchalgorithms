package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.lang.Math.max;
import static java.util.Arrays.fill;

import java.util.Arrays;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.map.CharLongMap;
import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.text.CharAlphabet;
import net.amygdalum.util.text.CharMapping;

/**
 * An implementation of the String Search Algorithm Shift-Or (or Baeza-Yates–Gonnet).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class ShiftOr implements StringSearchAlgorithm {

	private int patternLength;
	private BitMapStates states;

	public ShiftOr(String pattern) {
		this(pattern, CharMapping.IDENTITY);
	}

	public ShiftOr(String pattern, CharMapping mapping) {
		this.patternLength = pattern.length();
		this.states = computeStates(pattern.toCharArray(), mapping);
	}

	private static BitMapStates computeStates(char[] pattern, CharMapping mapping) {
		CharAlphabet alphabet = CharAlphabet.ranged(pattern, mapping);
		int compactSize = max(256, pattern.length * 2);
		if (alphabet.getRange() < compactSize) {
			if (pattern.length > 64) {
				return new QuickMultiLongStates(pattern, alphabet, mapping);
			} else {
				return new QuickSingleLongStates(pattern, alphabet, mapping);
			}
		} else {
			if (pattern.length > 64) {
				return new SmartMultiLongStates(pattern, mapping);
			} else {
				return new SmartSingleLongStates(pattern, mapping);
			}
		}
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
			this.finalstate = ~(1l << ((patternLength - 1) % 64));
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
			this.state = BitMapStates.ALLBITS;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			state = BitMapStates.ALLBITS;
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long bits = states.single(nextChar);

				state = (state << 1) | bits;

				if ((state | finalstate) != BitMapStates.ALLBITS) {
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
			fill(state, BitMapStates.ALLBITS);
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			fill(state, BitMapStates.ALLBITS);
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished()) {
				char nextChar = chars.next();
				long[] bits = states.all(nextChar);

				state = next(state, bits);

				if ((state[0] | finalstate) != BitMapStates.ALLBITS) {
					return createMatch();
				}
			}
			return null;
		}

		private long[] next(long[] state, long[] bits) {
			for (int i = 0; i < state.length; i++) {
				int j = i + 1;
				long leastBit = j < state.length ? state[j] >>> 63 : 0l;
				state[i] = (state[i] << 1 | leastBit) | bits[i];
			}
			return state;
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory, SupportsCharClasses {

		private CharMapping mapping;

		@Override
		public void enableCharClasses(CharMapping mapping) {
			this.mapping = mapping;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			if (mapping == null) {
				return new ShiftOr(pattern);
			} else {
				return new ShiftOr(pattern, mapping);
			}
		}

	}

	public interface BitMapStates {

		public static final long ALLBITS = ~0l;

		boolean supportsSingle();

		long single(char c);

		long[] all(char c);

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

		public QuickSingleLongStates(char[] pattern, CharAlphabet alphabet, CharMapping mapping) {
			this.minChar = alphabet.minChar();
			this.maxChar = alphabet.maxChar();
			this.characters = computeStates(pattern, mapping, this.minChar, this.maxChar);
		}

		private static long[] computeStates(char[] pattern, CharMapping mapping, char min, char max) {
			long[] characters = new long[max - min + 1];
			Arrays.fill(characters, ALLBITS);
			for (int i = 0; i < pattern.length; i++) {
				for (char c : mapping.map(pattern[i])) {
					characters[c - min] &= ~(1l << i);
				}
			}
			return characters;
		}

		@Override
		public long single(char c) {
			if (c < minChar || c > maxChar) {
				return ALLBITS;
			}
			return characters[c - minChar];
		}

	}

	private static class SmartSingleLongStates extends SingleLongBitMapStates {

		private CharLongMap states;

		public SmartSingleLongStates(char[] pattern, CharMapping mapping) {
			this.states = computeStates(pattern, mapping);
		}

		private static CharLongMap computeStates(char[] pattern, CharMapping mapping) {
			CharLongMap map = new CharLongMap(ALLBITS);
			for (int i = 0; i < pattern.length; i++) {
				for (char c : mapping.map(pattern[i])) {
					long newState = map.get(c) & ~(1l << i);
					map.put(c, newState);
				}
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
			long[] zero = new long[((length - 1) / 64) + 1];
			Arrays.fill(zero, ALLBITS);
			return zero;
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

		public QuickMultiLongStates(char[] pattern, CharAlphabet alphabet, CharMapping mapping) {
			this.minChar = alphabet.minChar();
			this.maxChar = alphabet.maxChar();
			this.characters = computeStates(pattern, mapping, this.minChar, this.maxChar);
			this.zero = computeZero(pattern.length);
		}

		private static long[][] computeStates(char[] pattern, CharMapping mapping, char min, char max) {
			long[][] characters = new long[max - min + 1][];
			for (int c = min; c <= max; c++) {
				characters[c - min] = computeZero(pattern.length);
			}
			for (int i = 0; i < pattern.length; i++) {
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
				for (char c : mapping.map(pattern[i])) {
					characters[c - min][slot] &= ~(1l << offset);
				}
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

		public SmartMultiLongStates(char[] pattern, CharMapping mapping) {
			this.states = computeStates(pattern, mapping);
		}

		private static CharObjectMap<long[]> computeStates(char[] pattern, CharMapping mapping) {
			long[] zero = computeZero(pattern.length);
			CharObjectMap<long[]> map = new CharObjectMap<>(zero);
			for (int i = 0; i < pattern.length; i++) {
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
				for (char c : mapping.map(pattern[i])) {
					long[] newState = map.get(c);
					if (newState == zero) {
						newState = computeZero(pattern.length);
					}
					newState[slot] &= ~(1l << offset);
					map.put(c, newState);
				}
			}
			return map;
		}

		@Override
		public long[] all(char c) {
			return states.get(c);
		}

	}

}
