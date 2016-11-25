package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.fill;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;

import java.util.Arrays;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.CharLongMap;

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

		protected CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
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

		protected final long finalstate;
		protected final long activeStates;

		private long state;

		public LongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.finalstate = 1l << ((patternLength - 1) % 64);
			this.activeStates = (finalstate - 1) | finalstate;
			this.state = activeStates;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
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

		protected final long finalstate[];
		protected final long activeStates[];

		private long state;
		private int segment;
		private int[] patternLengths;

		public MultiLongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.patternLengths = computePatternLengths();
			this.finalstate = computeFinalStates();
			this.activeStates = computeActiveStates();
			this.segment = 0;
			this.state = activeStates[segment];
		}

		private int[] computePatternLengths() {
			int numberOfSubpatterns = ((patternLength - 1) / 64) + 1;
			int[] patternLengths = new int[numberOfSubpatterns];
			fill(patternLengths, 0, patternLengths.length - 1, 64);
			patternLengths[patternLengths.length - 1] = patternLength % 64;
			return patternLengths;
		}

		private long[] computeFinalStates() {
			long[] finalStates = new long[patternLengths.length];
			for (int i = 0; i < finalStates.length; i++) {
				int patternLength = patternLengths[i];
				finalStates[i] = 1l << ((patternLength - 1) % 64);
			}
			return finalStates;
		}

		private long[] computeActiveStates() {
			long[] activeStates = new long[finalstate.length];
			for (int i = 0; i < activeStates.length; i++) {
				activeStates[i] = (finalstate[i] - 1) | finalstate[i];
			}
			return activeStates;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > chars.current()) {
				chars.move(pos);
			}
			segment = 0;
			state = activeStates[segment];
		}

		@Override
		public StringMatch findNext() {
			while (!chars.finished(patternLength - 1)) {
				segment = 0;
				state = activeStates[segment];
				int j = patternLengths[segment] - 1;
				int[] last = new int[patternLengths.length];
				System.arraycopy(patternLengths, 0, last, 0, patternLengths.length);
				nextSegment: while (state != 0l) {
					char currentChar = chars.lookahead(segment * 64 + j);
					long single = states.select(segment, currentChar);
					state &= single;
					if ((state & finalstate[segment]) != 0l) {
						if (j > 0) {
							last[segment] = j;
						} else if (segment == patternLengths.length - 1) {
							StringMatch createMatch = createMatch();
							chars.forward(max(last, segment));
							return createMatch;
						} else {
							segment++;
							state = activeStates[segment];
							j = patternLengths[segment] - 1;
							continue nextSegment;
						}
					}
					j--;
					state = (state << 1) & activeStates[segment];
				}
				chars.forward(max(last, segment));
			}
			return null;
		}

		private int max(int[] values, int last) {
			int max = 0;
			for (int i = 0; i <= last; i++) {
				int next = values[i];
				if (next > max) {
					max = next;
				}
			}
			return max;
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BNDM(pattern);
		}

	}

	public interface BitMapStates {

		boolean supportsSingle();

		long single(char c);

		long select(int i, char c);

	}

	private abstract static class SingleLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return true;
		}

		@Override
		public long select(int i, char c) {
			if (i > 0) {
				return 0l;
			}
			return single(c);
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
			CharLongMap map = new CharLongMap(0l);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				long newState = map.get(c) | (1l << j);
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

		public QuickMultiLongStates(char[] pattern) {
			this.minChar = computeMinChar(pattern);
			this.maxChar = computeMaxChar(pattern);
			this.characters = computeStates(pattern, this.minChar, this.maxChar);
		}

		private static long[][] computeStates(char[] pattern, char min, char max) {
			int numberOfSubpatterns = ((pattern.length - 1) / 64) + 1;
			long[][] characters = new long[numberOfSubpatterns][];
			for (int i = 0; i < characters.length; i++) {
				int start = i * 64;
				int end = i == characters.length - 1 ? pattern.length : (i + 1) * 64;
				char[] subpattern = Arrays.copyOfRange(pattern, start, end);
				characters[i] = computeSubStates(subpattern, min, max);
			}
			return characters;
		}

		private static long[] computeSubStates(char[] pattern, char min, char max) {
			long[] characters = new long[max - min + 1];
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				characters[c - min] |= 1l << j;
			}
			return characters;
		}

		@Override
		public long select(int i, char c) {
			if (c < minChar || c > maxChar) {
				return 0l;
			}
			return characters[i][c - minChar];
		}

	}

	private static class SmartMultiLongStates extends MultiLongBitMapStates {

		private CharLongMap[] states;

		public SmartMultiLongStates(char[] pattern) {
			this.states = computeStates(pattern);
		}

		private static CharLongMap[] computeStates(char[] pattern) {
			int numberOfSubpatterns = ((pattern.length - 1) / 64) + 1;
			CharLongMap[] characters = new CharLongMap[numberOfSubpatterns];
			for (int i = 0; i < characters.length; i++) {
				int start = i * 64;
				int end = i == characters.length - 1 ? pattern.length : (i + 1) * 64;
				char[] subpattern = Arrays.copyOfRange(pattern, start, end);
				characters[i] = computeSubStates(subpattern);
			}
			return characters;
		}

		private static CharLongMap computeSubStates(char[] pattern) {
			CharLongMap map = new CharLongMap(0l);
			for (int i = 0; i < pattern.length; i++) {
				char c = pattern[i];
				int j = pattern.length - i - 1;
				long newState = map.get(c) | (1l << j);
				map.put(c, newState);
			}
			return map;
		}

		@Override
		public long select(int i, char c) {
			return states[i].get(c);
		}

	}
}
