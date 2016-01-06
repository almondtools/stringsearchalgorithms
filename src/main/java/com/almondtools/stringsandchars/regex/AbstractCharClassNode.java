package com.almondtools.stringsandchars.regex;


public abstract class AbstractCharClassNode extends CharNode {

	public abstract AbstractCharClassNode invert(char min, char max);

}
