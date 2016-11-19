package net.amygdalum.stringsearchalgorithms.io;

import net.amygdalum.util.text.ByteString;

public class ReverseByteProvider implements ByteProvider {

	private ByteProvider bytes;

	public ReverseByteProvider(ByteProvider bytes) {
		this.bytes = bytes;
	}

	@Override
	public byte next() {
		return bytes.prev();
	}

	@Override
	public byte lookahead() {
		return bytes.lookbehind();
	}

	@Override
	public byte lookahead(int i) {
		return bytes.lookbehind(i);
	}

	@Override
	public byte prev() {
		return bytes.next();
	}

	@Override
	public byte lookbehind() {
		return bytes.lookahead();
	}

	@Override
	public byte lookbehind(int i) {
		return bytes.lookahead(i);
	}

	@Override
	public long current() {
		return bytes.current();
	}

	@Override
	public void move(long i) {
		bytes.move(i);
	}
	
	@Override
	public void forward(int i) {
		bytes.forward(-i);
	}
	
	@Override
	public void finish() {
		bytes.move(0);
	}

	@Override
	public byte at(long i) {
		return bytes.at(i);
	}

	@Override
	public boolean finished() {
		return bytes.current() == 0;
	}

	@Override
	public boolean finished(int i) {
		return bytes.current() <= i;
	}
	
	@Override
	public byte[] between(long start, long end) {
		byte[] between = bytes.between(end, start);
		final int reverseStart = between.length - 1;
		for(int i = 0; i < between.length / 2; i++) {
		    byte temp = between[i];
		    between[i] = between[reverseStart - i ];
		    between[reverseStart - i ] = temp;
		}
		return between;
	}

	@Override
	public ByteString slice(long start, long end) {
		return bytes.slice(end, start).revert();
	}

	@Override
	public void mark() {
		bytes.mark();
	}

	@Override
	public boolean changed() {
		return bytes.changed();
	}
	
	@Override
	public String toString() {
		return new StringBuilder(bytes.toString()).reverse().toString();
	}

}
