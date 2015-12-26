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
		return input.charAt(pos);
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
	public long current() {
		return pos;
	}

	@Override
	public void move(long i) {
		pos = (int) i;
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
	public char at(long i) {
		return input.charAt((int) i);
	}

	@Override
	public char[] between(long start, long end) {
		char[] between = new char[(int) (end - start)];
		input.getChars((int) start, (int) end, between, 0);
		return between;
	}

	@Override
	public String slice(long start, long end) {
		return input.substring((int) start, (int) end);
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
