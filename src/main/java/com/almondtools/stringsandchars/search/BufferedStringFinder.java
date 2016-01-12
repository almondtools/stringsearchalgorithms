package com.almondtools.stringsandchars.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class BufferedStringFinder extends AbstractStringFinder {

	private Queue<StringMatch> buffer;

	public BufferedStringFinder(StringFinderOption... options) {
		super(options);
		this.buffer = new PriorityQueue<>();
	}

	protected void push(StringMatch match) {
		buffer.add(match);
	}
	
	protected void push(Collection<StringMatch> matches) {
		buffer.addAll(matches);
	}
	
	protected void clear() {
		buffer.clear();
	}

	protected boolean isBufferEmpty() {
		return buffer.isEmpty();
	}

	protected long removeMatchesBefore(long pos) {
		long last = pos;
		Iterator<StringMatch> bufferIterator = buffer.iterator();
		while (bufferIterator.hasNext()) {
			StringMatch next = bufferIterator.next();
			if (next.start() < pos) {
				bufferIterator.remove();
			} else if (next.end() > last) {
				last = next.end();
			}
		}
		return last;
	}

	protected StringMatch leftMost() {
		if (buffer.isEmpty()) {
			return null;
		}
		StringMatch match = buffer.remove();
		while (!buffer.isEmpty()) {
			StringMatch nextMatch = buffer.peek();
			if (nextMatch.start() == match.start() && nextMatch.end() == match.end()) {
				match = buffer.remove();
			} else {
				break;
			}
		}
		return match;
	}

	protected StringMatch longestLeftMost() {
		if (buffer.isEmpty()) {
			return null;
		}
		StringMatch match = buffer.remove();
		while (!buffer.isEmpty()) {
			StringMatch nextMatch = buffer.peek();
			if (nextMatch.start() == match.start()) {
				match = buffer.remove();
			} else if (nextMatch.end() <= match.end()){
				buffer.remove();
			} else {
				break;
			}
		}
		return match;
	}

	protected long lastStartFromBuffer() {
		long start = Long.MAX_VALUE;
		Iterator<StringMatch> bufferIterator = buffer.iterator();
		while (bufferIterator.hasNext()) {
			StringMatch next = bufferIterator.next();
			if (next.start() < start) {
				start = next.start();
			}
		}
		if (start == Long.MAX_VALUE) {
			return -1;
		} else {
			return start;
		}
	}

}

