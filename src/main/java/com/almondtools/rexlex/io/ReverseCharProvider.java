package com.almondtools.rexlex.io;

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
	public int current() {
		return chars.current();
	}

	@Override
	public void move(int i) {
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
	public char at(int i) {
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
	public char[] between(int start, int end) {
		return chars.between(end, start);
	}

	@Override
	public String slice(int start, int end) {
		return chars.slice(end, start);
	}

	@Override
	public void mark() {
		chars.mark();
	}

	@Override
	public boolean changed() {
		return chars.changed();
	}

}
