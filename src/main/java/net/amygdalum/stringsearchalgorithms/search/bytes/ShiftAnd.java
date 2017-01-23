package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.fill;
import static net.amygdalum.util.text.ByteUtils.encode;

import java.nio.charset.Charset;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteString;

/**
 * An implementation of the String Search Algorithm Shift-And (or Baeza-Yates–Gonnet).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class ShiftAnd implements StringSearchAlgorithm {

	private int patternLength;
	private BitMapStates states;

	public ShiftAnd(String pattern, Charset charset) {
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
		protected ByteProvider bytes;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.finalstate = 1l << ((patternLength - 1) % 64);
			this.bytes = bytes;
		}

		protected StringMatch createMatch() {
			long end = bytes.current();
			long start = end - patternLength;
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	private class LongFinder extends Finder {

		private long state;

		public LongFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
			this.state = 0;
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			state = 0;
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte nextByte = bytes.next();
				long bits = states.single(nextByte);

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

		public MultiLongFinder(ByteProvider bytes, StringFinderOption... options) {
			super(bytes, options);
			this.state = new long[((patternLength - 1) / 64) + 1];
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			fill(state, 0l);
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte nextByte = bytes.next();
				long[] bits = states.all(nextByte);

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

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new ShiftAnd(pattern, charset);
		}

	}

	public interface BitMapStates {

		boolean supportsSingle();

		long single(byte b);

		long[] all(byte b);

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
				bytes[b & 0xff] |= 1l << i;
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
				int slot = ((pattern.length - 1) / 64) - i / 64;
				int offset = i % 64;
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
