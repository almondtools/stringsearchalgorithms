package net.amygdalum.stringsearchalgorithms.io;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharProvider implements CharProvider {

	private static final long NO_MARK = Long.MIN_VALUE;

	private Reader input;
	private int bufferSize;
	private int bufferNumber;
	private char[][] buffers;
	private int currentIndex;
	private int currentPos;
	private int topIndex;
	private int topPos;
	private long absolutePos;
	private long mark;

	public ReaderCharProvider(Reader input, long start, int chunk, int lookaroundBuffers) {
		this.input = input;
		this.bufferSize = chunk;
		this.bufferNumber = lookaroundBuffers + 1;
		this.buffers = new char[bufferNumber][bufferSize];
		this.currentIndex = -1;
		this.currentPos = 0;
		this.topIndex = -1;
		this.topPos = 0;
		this.mark = NO_MARK;
		init(start);
	}

	public final void init(long start) {
		int buffersToRead = (int) (start / (long) bufferSize) + 1;
		read(buffersToRead);
		currentIndex = topIndex;
		currentPos = (int) (start % bufferSize);
		absolutePos = start;
	}

	private int read(int buffersToRead) {
		if (buffersToRead <= 0) {
			return 0;
		}
		try {
			int buffersAlreadyRead = topIndex - currentIndex;
			for (int i = 0; i < buffersAlreadyRead; i++) {
				buffersToRead--;
			}

			int buffersToSkip = buffersToRead - bufferNumber;
			if (buffersToSkip > 0) {
				for (int i = 0; i < buffersToSkip; i++) {
					topPos = input.read(buffers[0]);
					buffersToRead--;
				}
				topIndex = -1;
				currentIndex = -1;
			}

			int freeBuffers = bufferNumber - currentIndex - 1;
			int buffersToShift = buffersToRead - freeBuffers;

			if (buffersToShift > 0) {
				int lastIndexToReplace = bufferNumber - buffersToShift;
				for (int i = 0; i < lastIndexToReplace; i++) {
					char[] temp = buffers[i];
					buffers[i] = buffers[i + buffersToShift];
					buffers[i + buffersToShift] = temp;
				}
				topIndex -= buffersToShift;
			}

			while (buffersToRead > 0) {
				topIndex++;
				topPos = input.read(buffers[topIndex]);
				buffersToRead--;
				if (topPos < bufferSize) {
					break;
				}
			}
			if (buffersToRead > 0) {
				throw new OutOfBufferException();
			} else if (buffersToSkip > 0) {
				return buffersToSkip + bufferNumber;
			} else if (buffersToShift > 0) {
				return buffersToShift;
			} else {
				return 0;
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	private int readAhead(int buffersToRead) {
		if (buffersToRead <= 0) {
			return 0;
		}
		try {
			int freeBuffers = bufferNumber - topIndex - 1;
			int buffersToShift = buffersToRead - freeBuffers;

			if (buffersToShift > currentIndex) {
				throw new OutOfBufferException();
			} else if (buffersToShift > 0) {
				int lastIndexToReplace = bufferNumber - buffersToShift;
				for (int i = 0; i < lastIndexToReplace; i++) {
					char[] temp = buffers[i];
					buffers[i] = buffers[i + buffersToShift];
					buffers[i + buffersToShift] = temp;
				}
				currentIndex -= buffersToShift;
				topIndex -= buffersToShift;
			}

			while (buffersToRead > 0) {
				topIndex++;
				topPos = input.read(buffers[topIndex]);
				buffersToRead--;
				if (topPos < bufferSize) {
					break;
				}
			}
			if (buffersToRead > 0) {
				throw new OutOfBufferException();
			} else {
				return buffersToShift;
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public char next() {
		int expectedIndex = currentIndex;
		while (currentPos >= bufferSize) {
			currentPos -= bufferSize;
			expectedIndex++;
		}
		if (expectedIndex <= topIndex) {
			currentIndex = expectedIndex;
		} else {
			int indexShift = read(expectedIndex - topIndex);
			currentIndex = expectedIndex - indexShift;
		}
		char c = buffers[currentIndex][currentPos];
		currentPos++;
		absolutePos++;
		return c;
	}

	@Override
	public char lookahead() {
		return lookahead(0);
	}

	@Override
	public char lookahead(int pos) {
		int expectedPos = currentPos + pos;
		int expectedIndex = currentIndex;
		while (expectedPos < 0) {
			expectedPos += bufferSize;
			expectedIndex--;
		}
		while (expectedPos >= bufferSize) {
			expectedPos -= bufferSize;
			expectedIndex++;
		}
		if (expectedIndex < 0) {
			throw new OutOfBufferException();
		} else if (expectedIndex > topIndex) {
			int indexShift = readAhead(expectedIndex - topIndex);
			expectedIndex -= indexShift;
		}
		return buffers[expectedIndex][expectedPos];
	}

	@Override
	public char prev() {
		int expectedIndex = currentIndex;
		currentPos--;
		absolutePos--;
		while (currentPos < 0 && expectedIndex > -1) {
			currentPos += bufferSize;
			expectedIndex--;
		}
		if (expectedIndex < 0) {
			throw new OutOfBufferException();
		}
		currentIndex = expectedIndex;
		char c = buffers[currentIndex][currentPos];
		return c;
	}

	@Override
	public char lookbehind() {
		return lookbehind(0);
	}

	@Override
	public char lookbehind(int pos) {
		int expectedPos = currentPos - 1 - pos;
		int expectedIndex = currentIndex;
		while (expectedPos < 0) {
			expectedPos += bufferSize;
			expectedIndex--;
		}
		while (expectedPos >= bufferSize) {
			expectedPos -= bufferSize;
			expectedIndex++;
		}
		if (expectedIndex < 0) {
			throw new OutOfBufferException();
		} else if (expectedIndex > topIndex) {
			int indexShift = readAhead(expectedIndex - topIndex);
			expectedIndex -= indexShift;
		}
		return buffers[expectedIndex][expectedPos];
	}

	@Override
	public long current() {
		return absolutePos;
	}

	@Override
	public void move(long pos) {
		long relativePos = pos - absolutePos;
		long expectedPos = (long) currentPos + relativePos;
		int expectedIndex = currentIndex;
		while (expectedPos < 0) {
			expectedPos += bufferSize;
			expectedIndex--;
		}
		while (expectedPos >= bufferSize) {
			expectedPos -= bufferSize;
			expectedIndex++;
		}

		if (expectedIndex < 0) {
			throw new OutOfBufferException();
		} else if (relativePos > 0) {
			int buffersToRead = expectedIndex - topIndex;
			int indexShift = read(buffersToRead);
			currentIndex = expectedIndex - indexShift;
			currentPos = (int) expectedPos;
			absolutePos = pos;
		} else {
			currentIndex = expectedIndex;
			currentPos = (int) expectedPos;
			absolutePos = pos;
		}
	}

	@Override
	public void forward(int i) {
		move(absolutePos + i);
	}

	@Override
	public void finish() {
		try {
			topPos = -1;
			currentIndex = topIndex;
			input.close();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public boolean finished() {
		return finished(0);
	}

	@Override
	public boolean finished(int i) {
		int expectedPos = currentPos + i;
		int expectedIndex = currentIndex;
		while (expectedPos > bufferSize) {
			expectedPos -= bufferSize;
			expectedIndex++;
		}
		if (expectedIndex > topIndex) {
			int indexShift = readAhead(expectedIndex - topIndex);
			expectedIndex -= indexShift;
		}
		if (expectedIndex == topIndex && expectedPos == topPos && topPos == bufferSize) {
			int indexShift = readAhead(1);
			expectedIndex -= indexShift;
		}

		if (topPos == -1 && expectedIndex >= topIndex) {
			return true;
		} else if (topPos == -1 && expectedIndex == topIndex - 1 && expectedPos == bufferSize) {
			return true;
		}
		return false;
	}

	@Override
	public char at(long pos) {
		long relativePos = pos - absolutePos;
		long expectedPos = (long) currentPos + relativePos;
		int expectedIndex = currentIndex;
		while (expectedPos < 0) {
			expectedPos += bufferSize;
			expectedIndex--;
		}
		while (expectedPos >= bufferSize) {
			expectedPos -= bufferSize;
			expectedIndex++;
		}

		if (expectedIndex < 0) {
			throw new OutOfBufferException();
		}
		if (expectedIndex > topIndex) {
			int indexShift = readAhead(expectedIndex - topIndex);
			expectedIndex -= indexShift;
		}

		return buffers[expectedIndex][(int) expectedPos];
	}

	@Override
	public char[] between(long start, long end) {
		long len = end - start;
		if (len / bufferSize > bufferNumber) {
			throw new OutOfBufferException();
		}

		long relativeStart = start - absolutePos;
		long expectedStartPos = (long) currentPos + relativeStart;
		int expectedStartIndex = currentIndex;
		while (expectedStartPos < 0) {
			expectedStartPos += bufferSize;
			expectedStartIndex--;
		}
		while (expectedStartPos >= bufferSize) {
			expectedStartPos -= bufferSize;
			expectedStartIndex++;
		}

		long relativeEnd = end - absolutePos;
		long expectedEndPos = (long) currentPos + relativeEnd;
		int expectedEndIndex = currentIndex;
		while (expectedEndPos <= 0) {
			expectedEndPos += bufferSize;
			expectedEndIndex--;
		}
		while (expectedEndPos > bufferSize) {
			expectedEndPos -= bufferSize;
			expectedEndIndex++;
		}

		if (expectedStartIndex < 0 || expectedEndIndex < 0) {
			throw new OutOfBufferException();
		} else if (expectedEndIndex - expectedStartIndex >= bufferNumber) {
			throw new OutOfBufferException();
		}
		if (expectedEndIndex > topIndex) {
			int indexShift = readAhead(expectedEndIndex - topIndex);
			expectedStartIndex -= indexShift;
			expectedEndIndex -= indexShift;
		}

		int betweenLen = (int) len;
		char[] between = new char[betweenLen];

		if (betweenLen == 0) {
			//do nothing
		} else if (expectedStartIndex == expectedEndIndex) {
			System.arraycopy(buffers[expectedStartIndex], (int) expectedStartPos, between, 0, (int) (expectedEndPos - expectedStartPos));
		} else {
			int to = 0;

			System.arraycopy(buffers[expectedStartIndex], (int) expectedStartPos, between, to, (int) (bufferSize - expectedStartPos));
			to += bufferSize - expectedStartPos;

			for (int i = expectedStartIndex + 1; i < expectedEndIndex; i++) {
				System.arraycopy(buffers[i], 0, between, to, bufferSize);
				to += bufferSize;
			}

			System.arraycopy(buffers[expectedEndIndex], 0, between, to, (int) expectedEndPos);
			to += expectedEndPos;
		}
		return between;
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
		StringBuilder buffer = new StringBuilder("...");
		if (currentIndex > 0) {
			buffer.append(buffers[currentIndex - 1]);
		}
		buffer.append(buffers[currentIndex], 0, currentPos);
		buffer.append('|');
		if (topIndex > currentIndex) {
			buffer.append(buffers[currentIndex], currentPos, bufferSize - currentPos);
		} else if (topPos > currentPos) {
			buffer.append(buffers[currentIndex], currentPos, topPos - currentPos);
		}
		return buffer.toString();
	}
}
