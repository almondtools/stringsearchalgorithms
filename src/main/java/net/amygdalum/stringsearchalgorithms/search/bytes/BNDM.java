package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.fill;
import static net.amygdalum.stringsearchalgorithms.search.bytes.Encoding.encode;

import java.nio.charset.Charset;

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

		private long[] state;

		public MultiLongFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
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
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			init(state);
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished(patternLength - 1)) {
				state = initial(patternLength);
				int j = patternLength - 1;
				int last = patternLength;
				while (zero(state)) {
					byte currentByte = bytes.lookahead(j);
					long[] all = states.all(currentByte);
					state = join(state, all);
					if ((state[0] & finalstate) != 0l) {
						if (j > 0) {
							last = j;
						} else {
							StringMatch createMatch = createMatch();
							bytes.forward(last);
							return createMatch;
						}
					}
					j--;
					state = next(state);
				}
				bytes.forward(last);
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

	private abstract static class SingleLongBitMapStates implements BitMapStates {

		@Override
		public boolean supportsSingle() {
			return true;
		}

		@Override
		public long[] all(byte b) {
			return new long[] { single(b) };
		}

	}

	private static class QuickSingleLongStates extends SingleLongBitMapStates {

		private long[] bytes;

		public QuickSingleLongStates(byte[] pattern) {
			this.bytes = computeStates(pattern);
		}

		private static long[] computeStates(byte[] pattern) {
			long[] bytes = new long[256];
			for (int i = 0; i < pattern.length; i++) {
				byte b = pattern[i];
				int j = pattern.length - i - 1;
				bytes[b & 0xff] |= 1l << j;
			}
			return bytes;
		}

		@Override
		public long single(byte b) {
			return bytes[b & 0xff];
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
			long[][] bytes = new long[256][];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = computeZero(pattern.length);
			}
			for (int i = 0; i < pattern.length; i++) {
				byte b = pattern[i];
				int j = pattern.length - i - 1;
				int slot = ((pattern.length - 1) / 64) - j / 64;
				int offset = j % 64;
				bytes[b & 0xff][slot] |= 1l << offset;
			}
			return bytes;
		}

		@Override
		public long[] all(byte b) {
			return bytes[b & 0xff];
		}

	}

}
