package com.almondtools.rexlex.io;

public interface CharProvider {

	char next();
	char lookahead();
	char lookahead(int i);
	char prev();
	char lookbehind();
	char lookbehind(int i);

	int current();
	void move(int i);
	void forward(int i);
	void finish();
	boolean finished();
	boolean finished(int i);

	char at(int i);
	char[] between(int start, int end);
	String slice(int start, int end);

	void mark();
	boolean changed();

}
