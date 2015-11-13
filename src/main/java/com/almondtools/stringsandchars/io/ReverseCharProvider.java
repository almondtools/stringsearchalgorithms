package com.almondtools.stringsandchars.io;


public class ReverseCharProvider implements CharProvider {

	private CharProvider chars;

	public ReverseCharProvider(CharProvider chars) {
		this.chars = chars;
	}

	@Override
	public char next() {
		return chars.prev();
	}

	@Override
	public char lookahead() {
		return chars.lookbehind();
	}

	@Override
	public char lookahead(int i) {
		return chars.lookbehind(i);
	}

	@Override
	public char prev() {
		return chars.next();
	}

	@Override
	public char lookbehind() {
		return chars.lookahead();
	}

	@Override
	public char lookbehind(int i) {
		return chars.lookahead(i);
	}

	@Override
	public long current() {
		return chars.current();
	}

	@Override
	public void move(long i) {
		chars.move(i);
	}
	
	@Override
	public void forward(int i) {
		chars.forward(-i);
	}
	
	@Override
	public void finish() {
		chars.move(0);
	}

	@Override
	public char at(long i) {
		return chars.at(i);
	}

	@Override
	public boolean finished() {
		return chars.current() == 0;
	}

	@Override
	public boolean finished(int i) {
		return chars.finished(i);
	}
	
	@Override
	public char[] between(long start, long end) {
		char[] between = chars.between(end, start);
		final int reverseStart = between.length - 1;
		for(int i = 0; i < between.length / 2; i++) {
		    char temp = between[i];
		    between[i] = between[reverseStart - i ];
		    between[reverseStart - i ] = temp;
		}
		return between;
	}

	@Override
	public String slice(long start, long end) {
		return new StringBuilder(chars.slice(end, start)).reverse().toString();
	}

	@Override
	public void mark() {
		chars.mark();
	}

	@Override
	public boolean changed() {
		return chars.changed();
	}
	
	@Override
	public String toString() {
		return new StringBuilder(chars.toString()).reverse().toString();
	}

}
