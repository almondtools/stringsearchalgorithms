package net.amygdalum.stringsearchalgorithms.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class ReaderBufferCharProvider implements CharProvider {

	private static final long NO_MARK = Long.MIN_VALUE;

	private Reader input;
	private int chunk;
	private int capacity;
	private CharBuffer buffer;
	private long absolutePos;
	private long mark;
	private int pos;

	private int state;

	public ReaderBufferCharProvider(Reader input, long start, int buffer, int reverseBuffers) {
		this.input = input;
		this.chunk = buffer;
		this.capacity = buffer * (reverseBuffers + 1);
		this.buffer = CharBuffer.allocate(capacity);
		this.mark = NO_MARK;
		init(start);
	}

	public final void init(long start) {
		long shift = ((start / chunk) + 1) * chunk;
		long skip = 0;
		while (shift > capacity) {
			shift -= chunk;
			skip += chunk;
		}
		if (skip > 0) {
			skip(skip);
		}
		read();
		pos = (int) (start - skip);
		absolutePos = start;
	}

	public void read() {
		buffer.position(0);
		buffer.limit(capacity);
		try {
			state = input.read(buffer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		buffer.flip();
	}

	public void shift(int shift) {
		buffer.position(shift);
		buffer.compact();
		try {
			state = input.read(buffer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		buffer.flip();
		pos -= shift;
	}

	public void skip(long skip) {
		try {
			input.skip(skip);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public char next() {
		if (pos >= buffer.limit()) {
			shift(chunk);
		}
		char c = buffer.charAt(pos);
		pos++;
		absolutePos++;
		return c;
	}

	@Override
	public char lookahead() {
		return buffer.charAt(pos);
	}

	@Override
	public char lookahead(int i) {
		return buffer.charAt(pos + i);
	}

	@Override
	public char prev() {
		pos--;
		absolutePos--;
		if (pos < 0) {
			throw new OutOfBufferException();
		}
		return buffer.charAt(pos);
	}

	@Override
	public char lookbehind() {
		return buffer.charAt(pos - 1);
	}

	@Override
	public char lookbehind(int i) {
		return buffer.charAt(pos - 1 - i);
	}

	@Override
	public long current() {
		return absolutePos;
	}

	@Override
	public void move(long i) {
		long rel = i - absolutePos;
		if (pos + rel < 0) {
			int rev = pos + 1;
			pos = -1;
			absolutePos -= rev;
			throw new OutOfBufferException();
		}
		int shift = 0;
		while (pos - shift + rel > buffer.limit()) {
			shift += chunk;
		}
		int skip = 0;
		while (shift > capacity) {
			shift -= chunk;
			skip += chunk;
		}
		if (skip > 0) {
			skip(skip);
		}
		shift(shift);
		pos += rel;
		absolutePos += rel;
	}

	@Override
	public void forward(int i) {
		move(absolutePos + i);
	}

	@Override
	public void finish() {
		try {
			pos = buffer.limit();
			state = -1;
			input.close();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public boolean finished() {
		if (buffer.limit() - pos > 0) {
			return false;
		} else if (buffer.limit() - pos == 0 && state == -1) {
			return true;
		}
		shift(chunk);
		return buffer.limit() - pos <= 0 && state == -1;
	}

	@Override
	public boolean finished(int i) {
		if (buffer.limit() - pos > i) {
			return false;
		} else if (buffer.limit() - pos <= i && state == -1) {
			return true;
		}
		if ((pos % chunk) + i >= capacity) {
			throw new OutOfBufferException();
		}
		int shift = 0;
		while (pos - shift + i >= capacity) {
			shift += chunk;
		}
		if (shift > 0 || buffer.limit() < capacity) {
			shift(shift);
		}
		return buffer.limit() - pos <= i && state == -1;
	}

	@Override
	public char at(long i) {
		long rel = i - absolutePos;
		if (pos + rel < 0) {
			int rev = pos + 1;
			pos = -1;
			absolutePos -= rev;
			throw new OutOfBufferException();
		}
		if ((pos % chunk) + rel >= capacity) {
			throw new OutOfBufferException();
		}
		int shift = 0;
		while (pos - shift + rel >= capacity) {
			shift += chunk;
		}
		if (shift > 0 || buffer.limit() < capacity) {
			shift(shift);
		}
		return buffer.charAt((int) (pos + rel));
	}

	@Override
	public char[] between(long start, long end) {
		if (end - start + (start % chunk) > capacity) {
			throw new OutOfBufferException();
		}
		//capacity is sufficient

		long startRel = start - absolutePos;
		long endRel = end - absolutePos;
		if (pos + startRel < 0) {
			int rev = pos + 1;
			pos = -1;
			absolutePos -= rev;
			throw new OutOfBufferException();
		}
		//startRel is reachable

		int shift = 0;
		while (pos - shift + endRel >= buffer.limit()) {
			shift += chunk;
		}
		if (shift > pos) {
			throw new OutOfBufferException();
		}
		if (shift > 0 || buffer.limit() < capacity) {
			shift(shift);
		}
		//endRel is reachable

		int startSeq = (int) (pos + startRel);
		int endSeq = (int) (pos + endRel);

		char[] cs = new char[endSeq - startSeq];
		buffer.subSequence(startSeq, endSeq).get(cs);
		return cs;
	}

	@Override
	public String slice(long start, long end) {
		return new String(between(start, end));
	}

	@Override
	public void mark() {
		mark = absolutePos;
	}

	@Override
	public boolean changed() {
		boolean changed = mark != NO_MARK && mark != absolutePos;
		mark = NO_MARK;
		return changed;
	}

	@Override
	public String toString() {
		return "..." + buffer.subSequence(0, pos) + '|' + buffer.subSequence(pos, buffer.limit());
	}

}
