package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.util.Arrays.fill;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;
import static net.amygdalum.util.text.CharUtils.maxLength;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.MatchOption;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.text.CharMapping;
import net.amygdalum.util.text.QGramAlphabet;
import net.amygdalum.util.text.QGramMapping;
import net.amygdalum.util.text.StringSet;
import net.amygdalum.util.text.StringUtils;

/**
 * An implementation of the String Search Algorithm Shift-Or (or Baeza-Yates–Gonnet).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class QGramShiftOr implements StringSearchAlgorithm {

	private int minLength;
	private int maxLength;
	private QGramMapping qmapping;
	private StringSet patterns;
	private BitMapStates states;

	public QGramShiftOr(Collection<String> patterns) {
		this(patterns, bestMapping(patterns), CharMapping.IDENTITY);
	}

	public QGramShiftOr(Collection<String> patterns, CharMapping mapping) {
		this(patterns, bestMapping(patterns), mapping);
	}

	public QGramShiftOr(Collection<String> patterns, QGramMapping qmapping, CharMapping mapping) {
		List<char[]> charpatterns = StringUtils.toCharArray(patterns);
		this.minLength = minLength(charpatterns);
		this.maxLength = maxLength(charpatterns);
		this.qmapping = qmapping;
		this.patterns = new StringSet(charpatterns);
		this.states = computeStates(charpatterns, qmapping, mapping, minLength, maxLength);
	}

	public static QGramMapping bestMapping(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		char minChar = computeMinChar(charpatterns);
		char maxChar = computeMaxChar(charpatterns);
		int range = maxChar - minChar + 1; 

		int bits = 1;
		for (int i = range; i > 0; i >>= 2) {
			bits++;
		}
		
		int q = minLength(charpatterns);
		if (q > 3) {
			q = 3;
		}
		
		return new QGramMapping(q, bits);
	}

	private static BitMapStates computeStates(List<char[]> patterns, QGramMapping qmapping, CharMapping mapping, int minLength, int maxLength) {
		QGramAlphabet alphabet = QGramAlphabet.of(patterns, qmapping);
		if (maxLength > 64) {
			return new RelaxedMultiLongStates(patterns, alphabet, mapping, maxLength);
		} else {
			return new RelaxedSingleLongStates(patterns, alphabet, mapping, maxLength);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (states.supportsSingle()) {
			if (MatchOption.LONGEST_MATCH.in(options)) {
				return new LongLongestFinder(chars, options);
			} else {
				return new LongNextFinder(chars, options);
			}
		} else {
			if (MatchOption.LONGEST_MATCH.in(options)) {
				return new MultiLongLongestFinder(chars, options);
			} else {
				return new MultiLongNextFinder(chars, options);
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends BufferedStringFinder {

		protected int q;
		protected int q1;
		protected CharProvider chars;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.q = qmapping.getQ();
			this.q1 = qmapping.getQ() - 1;
			this.chars = chars;
		}

		protected int nextQGram(char[] qgram) {
			for (int i = 0; i < qgram.length; i++) {
				qgram[i] = chars.lookahead(i);
			}
			chars.next();
			return qmapping.map(qgram);
		}

		protected StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

		protected boolean firstMatchOutOfSubsumptionRange() {
			long lastStart = lastStartFromBuffer();
			return chars.current() > lastStart + maxLength;
		}

	}

	private abstract class LongFinder extends Finder {

		protected final long finalstate;
		protected long state;

		public LongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.finalstate = computeFinalState();
			this.state = BitMapStates.ALLBITS;
		}

		private long computeFinalState() {
			long finalstate = BitMapStates.ALLBITS;
			for (int len : patterns.containedLengths()) {
				int lenQ = len - q;
				finalstate &= ~(1l << lenQ);
			}
			return finalstate;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			if (last > chars.current()) {
				chars.move(last);
				state = BitMapStates.ALLBITS;
			} else {
				long diff = chars.current() - pos;
				if (diff < maxLength) {
					state |= BitMapStates.ALLBITS << diff;
				}
			}
		}

		protected final List<StringMatch> verifyMatches() {
			List<StringMatch> matches = new LinkedList<>();
			for (int len : patterns.containedLengths()) {
				int lenQ = len - q;
				if ((state | ~(1l << lenQ)) != BitMapStates.ALLBITS) {
					long end = chars.current() + q1;
					long start = end - len;
					char[] found = chars.between(start, end);
					if (patterns.contains(found)) {
						matches.add(0, createMatch(start, end));
					}
				}
			}
			return matches;
		}

		protected boolean isFinalState() {
			return (state | finalstate) != BitMapStates.ALLBITS;
		}

		protected boolean isZeroState() {
			return state == BitMapStates.ALLBITS;
		}

	}

	private class LongNextFinder extends LongFinder {

		public LongNextFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			char[] qgram = qmapping.newQGram();
			while (!chars.finished(q1)) {
				int nextQGram = nextQGram(qgram);
				long bits = states.single(nextQGram);

				state = (state << 1) | bits;

				if (isFinalState()) {
					push(verifyMatches());
					if (!isBufferEmpty()) {
						return leftMost();
					}
				}
			}
			return null;
		}

	}

	private class LongLongestFinder extends LongFinder {

		public LongLongestFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			char[] qgram = qmapping.newQGram();
			while (!chars.finished(q1)) {
				int nextQGram = nextQGram(qgram);
				long bits = states.single(nextQGram);

				state = (state << 1) | bits;

				if (isFinalState()) {
					push(verifyMatches());
				}
				if (!isBufferEmpty()) {
					if (isZeroState()) {
						break;
					} else if (firstMatchOutOfSubsumptionRange()) {
						break;
					}
				}
			}
			return longestLeftMost();
		}

	}

	private abstract class MultiLongFinder extends Finder {

		protected final long[] finalstate;
		protected long[] state;

		public MultiLongFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
			this.finalstate = computeFinalState();
			this.state = MultiLongBitMapStates.computeZero(maxLength);
		}

		private long[] computeFinalState() {
			long[] finalstate = MultiLongBitMapStates.computeZero(maxLength);
			for (int len : patterns.containedLengths()) {
				int lenQ = len - q;
				int slot = ((maxLength - 1) / 64) - lenQ / 64;
				int offset = lenQ % 64;
				finalstate[slot] &= ~(1l << offset);
			}
			return finalstate;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			if (last > chars.current()) {
				chars.move(last);
				fill(state, BitMapStates.ALLBITS);
			} else {
				long diff = chars.current() - pos;
				if (diff < maxLength) {
					for (int i = state.length - 1; i >= 0 && diff > 0; i--) {
						if (diff < 64) {
							state[i] |= BitMapStates.ALLBITS << diff;
						} else {
							state[i] = BitMapStates.ALLBITS;
						}
						diff -= 64;
					}
				}
			}
		}

		protected final List<StringMatch> verifyMatches() {
			List<StringMatch> matches = new LinkedList<>();
			for (int len : patterns.containedLengths()) {
				int lenQ = len - q;
				int lastElement = maxLength - 1;
				int allslots = lastElement / 64;
				int slotsFromBeginning = lenQ / 64;
				int slot = allslots - slotsFromBeginning;
				int offset = lenQ % 64;
				if ((state[slot] | ~(1l << offset)) != BitMapStates.ALLBITS) {
					long end = chars.current() + q1;
					long start = end - len;
					char[] found = chars.between(start, end);
					if (patterns.contains(found)) {
						matches.add(0, createMatch(start, end));
					}
				}
			}
			return matches;
		}

		protected long[] next(long[] state, long[] bits) {
			for (int i = 0; i < state.length; i++) {
				int j = i + 1;
				long leastBit = j < state.length ? state[j] >>> 63 : 0l;
				state[i] = (state[i] << 1 | leastBit) | bits[i];
			}
			return state;
		}

		protected boolean isFinalState() {
			for (int i = 0; i < state.length; i++) {
				if ((state[i] | finalstate[i]) != BitMapStates.ALLBITS) {
					return true;
				}
			}
			return false;
		}

		protected boolean isZeroState() {
			for (int i = 0; i < state.length; i++) {
				if (state[i] != BitMapStates.ALLBITS) {
					return false;
				}
			}
			return true;
		}

	}

	private class MultiLongNextFinder extends MultiLongFinder {

		public MultiLongNextFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			char[] qgram = qmapping.newQGram();
			while (!chars.finished(q1)) {
				int nextQGram = nextQGram(qgram);
				long[] bits = states.all(nextQGram);

				state = next(state, bits);

				if (isFinalState()) {
					push(verifyMatches());
					if (!isBufferEmpty()) {
						return leftMost();
					}
				}
			}
			return null;
		}

	}

	private class MultiLongLongestFinder extends MultiLongFinder {

		public MultiLongLongestFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			char[] qgram = qmapping.newQGram();
			while (!chars.finished(q1)) {
				int nextQGram = nextQGram(qgram);
				long[] bits = states.all(nextQGram);

				state = next(state, bits);

				if (isFinalState()) {
					push(verifyMatches());
				}
				if (!isBufferEmpty()) {
					if (isZeroState()) {
						break;
					} else if (firstMatchOutOfSubsumptionRange()) {
						break;
					}
				}

			}
			return longestLeftMost();
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory, SupportsCharClasses {

		private CharMapping mapping;

		@Override
		public void enableCharClasses(CharMapping mapping) {
			this.mapping = mapping;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			if (mapping == null) {
				return new QGramShiftOr(patterns);
			} else {
				return new QGramShiftOr(patterns, mapping);
			}
		}

	}

	public interface BitMapStates {

		public static final long ALLBITS = ~0l;

		boolean supportsSingle();

		long single(int qc);

		long[] all(int qc);

	}

	private abstract static class SingleLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return true;
		}

		@Override
		public long[] all(int qc) {
			return new long[] { single(qc) };
		}

	}

	private static class RelaxedSingleLongStates extends SingleLongBitMapStates {

		private int minQGram;
		private int maxQGram;
		private long[] characters;

		public RelaxedSingleLongStates(List<char[]> pattern, QGramAlphabet alphabet, CharMapping mapping, int maxLength) {
			this.minQGram = alphabet.minQGram();
			this.maxQGram = alphabet.maxQGram();
			this.characters = computeStates(pattern, alphabet, mapping, maxLength);
		}

		private static long[] computeStates(List<char[]> patterns, QGramAlphabet alphabet, CharMapping mapping, int maxLength) {
			QGramMapping qmapping = alphabet.getMapping();
			int min = alphabet.minQGram();
			int max = alphabet.maxQGram();
			long[] characters = new long[max - min + 1];
			Arrays.fill(characters, ALLBITS);
			for (char[] pattern : patterns) {
				int i = 0;
				for (int[] qcs : qmapping.iterate(pattern, mapping)) {
					for (int qc : qcs) {
						characters[qc - min] &= ~(1l << i);
					}
					i++;
				}

			}
			return characters;
		}

		@Override
		public long single(int c) {
			if (c < minQGram || c > maxQGram) {
				return ALLBITS;
			}
			return characters[c - minQGram];
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
		public long single(int c) {
			throw new UnsupportedOperationException();
		}

	}

	private static class RelaxedMultiLongStates extends MultiLongBitMapStates {

		private int minQGram;
		private int maxQGram;
		private long[][] characters;
		private long[] zero;

		public RelaxedMultiLongStates(List<char[]> patterns, QGramAlphabet alphabet, CharMapping mapping, int maxLength) {
			this.minQGram = alphabet.minQGram();
			this.maxQGram = alphabet.maxQGram();
			this.characters = computeStates(patterns, alphabet, mapping, maxLength);
			this.zero = computeZero(maxLength);
		}

		private static long[][] computeStates(List<char[]> patterns, QGramAlphabet alphabet, CharMapping mapping, int maxLength) {
			QGramMapping qmapping = alphabet.getMapping();
			int min = alphabet.minQGram();
			int max = alphabet.maxQGram();
			long[][] characters = new long[max - min + 1][];
			for (int c = min; c <= max; c++) {
				characters[c - min] = computeZero(maxLength);
			}
			for (char[] pattern : patterns) {
				int i = 0;
				for (int[] qcs : qmapping.iterate(pattern, mapping)) {
					int lastElement = maxLength - 1;
					int neededSlots = lastElement / 64;
					int slotsFromBeginning = i / 64;
					int slot = neededSlots - slotsFromBeginning;
					int offset = i % 64;
					for (int qc : qcs) {
						characters[qc - min][slot] &= ~(1l << offset);
					}
					i++;
				}
			}
			return characters;
		}

		@Override
		public long[] all(int qc) {
			if (qc < minQGram || qc > maxQGram) {
				return zero;
			}
			return characters[qc - minQGram];
		}

	}

}
