package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.fill;
import static net.amygdalum.stringsearchalgorithms.search.bytes.Encoding.encode;

import java.nio.charset.Charset;
import java.util.Arrays;

import net.amygdalum.stringsearchalgorithms.io.ByteProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.text.ByteString;

/**
 * An implementation of the String Search Algorithm BNDM (Backward Nondeterministic Dawg Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BNDM implements StringSearchAlgorithm {

	private int patternLength;
	private BitMapStates states;

	public BNDM(String pattern, Charset charset) {
		byte[] encoded = encode(pattern, charset);
		this.patternLength = encoded.length;
		this.states = computeStates(encoded);
	}

	private static BitMapStates computeStates(byte[] pattern) {
		if (pattern.length > 64) {
			return new QuickMultiLongStates(pattern);
		} else {
			return new QuickSingleLongStates(pattern);
		}
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (states.supportsSingle()) {
			return new LongFinder(bytes, options);
		} else {
			return new MultiLongFinder(bytes, options);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private abstract class Finder extends AbstractStringFinder {

		protected final long finalstate;
		protected final long activeStates;

		protected ByteProvider bytes;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.finalstate = 1l << ((patternLength - 1) % 64);
			this.activeStates = (finalstate - 1) | finalstate;
			this.bytes = bytes;
		}

		protected StringMatch createMatch() {
			long start = bytes.current();
			long end = start + patternLength;
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	private class LongFinder extends Finder {

		private long state;

		public LongFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
			this.state = activeStates;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			state = activeStates;
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished(patternLength - 1)) {
				state = activeStates;
				int j = patternLength - 1;
				int last = patternLength;
				while (state != 0l) {
					byte currentByte = bytes.lookahead(j);
					long single = states.single(currentByte);
					state &= single;
					if ((state & finalstate) != 0l) {
						if (j > 0) {
							last = j;
						} else {
							StringMatch createMatch = createMatch();
							bytes.forward(last);
							return createMatch;
						}
					}
					j--;
					state = (state << 1) & activeStates;
				}
				bytes.forward(last);
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

		public MultiLongFinder(ByteProvider chars, StringFinderOption... options) {
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
			patternLengths[patternLengths.length - 1] = (patternLength - 1) % 64 + 1;
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
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			segment = 0;
			state = activeStates[segment];
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished(patternLength - 1)) {
				segment = 0;
				state = activeStates[segment];
				int j = patternLengths[segment] - 1;
				int[] last = new int[patternLengths.length];
				System.arraycopy(patternLengths, 0, last, 0, patternLengths.length);
				nextSegment: while (state != 0l) {
					byte currentByte = bytes.lookahead(segment * 64 + j);
					long single = states.select(segment, currentByte);
					state &= single;
					if ((state & finalstate[segment]) != 0l) {
						if (j > 0) {
							last[segment] = j;
						} else if (segment == patternLengths.length - 1) {
							StringMatch createMatch = createMatch();
							bytes.forward(max(last, segment));
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
				bytes.forward(max(last, segment));
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

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BNDM(pattern, charset);
		}

	}

	public interface BitMapStates {

		boolean supportsSingle();

		long single(byte c);

		long select(int i, byte c);

	}

	private abstract static class SingleLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return true;
		}

		@Override
		public long select(int i, byte b) {
			if (i > 0) {
				return 0l;
			}
			return single(b);
		}

	}

	private static class QuickSingleLongStates extends SingleLongBitMapStates {

		private long[] bytes;

		public QuickSingleLongStates(byte[] pattern) {
			this.bytes = computeStates(pattern);
		}

		private static long[] computeStates(byte[] pattern) {
			long[] characters = new long[256];
			for (int i = 0; i < pattern.length; i++) {
				int j = pattern.length - i - 1;
				byte b = pattern[i];
					characters[b & 0xff] |= 1l << j;
			}
			return characters;
		}

		@Override
		public long single(byte b) {
			return bytes[b & 0xff];
		}

	}

	private abstract static class MultiLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return false;
		}

		@Override
		public long single(byte b) {
			throw new UnsupportedOperationException();
		}

	}

	private static class QuickMultiLongStates extends MultiLongBitMapStates {

		private long[][] bytes;

		public QuickMultiLongStates(byte[] pattern) {
			this.bytes = computeStates(pattern);
		}

		private static long[][] computeStates(byte[] pattern) {
			int numberOfSubpatterns = ((pattern.length - 1) / 64) + 1;
			long[][] bytes = new long[numberOfSubpatterns][];
			for (int i = 0; i < bytes.length; i++) {
				int start = i * 64;
				int end = i == bytes.length - 1 ? pattern.length : (i + 1) * 64;
				byte[] subpattern = Arrays.copyOfRange(pattern, start, end);
				bytes[i] = computeSubStates(subpattern);
			}
			return bytes;
		}

		private static long[] computeSubStates(byte[] pattern) {
			long[] characters = new long[256];
			for (int i = 0; i < pattern.length; i++) {
				int j = pattern.length - i - 1;
				byte b = pattern[i];
				characters[b & 0xff] |= 1l << j;
			}
			return characters;
		}

		@Override
		public long select(int i, byte b) {
			return bytes[i][b & 0xff];
		}

	}

}
