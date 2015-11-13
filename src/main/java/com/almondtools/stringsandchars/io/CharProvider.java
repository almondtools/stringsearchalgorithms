package com.almondtools.stringsandchars.io;

public interface CharProvider {

	char next();
	char lookahead();
	char lookahead(int i);
	char prev();
	char lookbehind();
	char lookbehind(int i);

	/**
	 * This method returns the current positions. 
	 * 
	 * The result of current() must be consistent with move(int),between(int,int) and slice(int,int), e.g. move(current()) must not change the state.
	 * 
	 * There are no consistency constrains to other methods, especially one should NOT assume that moving with next/prev/forward changes current in a defined way (NOT +1/-1/+n).
	 * 
	 * @return the current position
	 */
	int current();
	
	/**
	 * This method moves to the given absolute position.
	 * 
	 * The argument of move(int) must be consistent with current(). That means move(current()) must not change the state.
	 * 
	 * @param i the position to move
	 * @see current()   
	 */
	void move(int i);
	
	/**
	 * This method returns the chars between the given absolute positions.
	 * 
	 * The arguments of between(int,int) must be consistent with current(). 
	 * 
	 * @throws NegativeArrayException if start is after end (which is not necessarily start < end)
	 * @param start the first position
	 * @param end the last position (exclusive)
	 * @see current()   
	 */
	char[] between(int start, int end);

	/**
	 * This method returns the String between the given absolute positions.
	 * 
	 * The arguments of slice(int,int) must be consistent with current(). 
	 * 
	 * @throws NegativeArrayException if start is after end (which is not necessarily start < end)
	 * @param start the first position
	 * @param end the last position (exclusive)
	 * @see current()   
	 */
	String slice(int start, int end);

	void forward(int i);
	void finish();
	boolean finished();
	boolean finished(int i);

	char at(int i);

	void mark();
	boolean changed();

}
