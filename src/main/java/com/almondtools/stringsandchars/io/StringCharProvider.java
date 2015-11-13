package com.almondtools.stringsandchars.io;

public class StringCharProvider implements CharProvider {

	private static final int NO_MARK = -1;
	
	private String input;
	private int pos;
	private int mark;

	public StringCharProvider(String input, int start) {
		this.input = input;
		this.pos = start;
		this.mark = NO_MARK;
	}

	public void restart() {
		pos = 0;
	}
	
	@Override
	public void finish() {
		pos = input.length();
	}

	@Override
	public char next() {
		char c = input.charAt(pos);
		pos++;
		return c;
	}

	@Override
	public char lookahead() {
		return input.charAt(pos);
	}

	@Override
	public char lookahead(int i) {
		return input.charAt(pos + i);
	}

	@Override
	public char prev() {
		pos--;
		char c = input.charAt(pos);
		return c;
	}

	@Override
	public char lookbehind() {
		return input.charAt(pos-1);
	}

	@Override
	public char lookbehind(int i) {
		return input.charAt(pos-1-i);
	}

	@Override
	public int current() {
		return pos;
	}

	@Override
	public void move(int i) {
		pos = i;
	}
	
	@Override
	public void forward(int i) {
		pos += i;
	}

	@Override
	public boolean finished() {
		return pos >= input.length();
	}
	
	@Override
	public boolean finished(int i) {
		return pos + i >= input.length();
	}

	@Override
	public char at(int i) {
		return input.charAt(i);
	}

	@Override
	public char[] between(int start, int end) {
		char[] between = new char[end - start];
		input.getChars(start, end, between, 0);
		return between;
	}

	@Override
	public String slice(int start, int end) {
		return input.substring(start, end);
	}

	@Override
	public String toString() {
		return input.substring(0, pos) + '|' + input.substring(pos);
	}

	@Override
	public void mark() {
		mark = pos;
	}

	@Override
	public boolean changed() {
		boolean changed = mark != NO_MARK && mark != pos;
		mark = NO_MARK;
		return changed;
	}

}
