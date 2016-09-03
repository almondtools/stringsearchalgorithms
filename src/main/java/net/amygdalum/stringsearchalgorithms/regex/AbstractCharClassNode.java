package net.amygdalum.stringsearchalgorithms.regex;


public abstract class AbstractCharClassNode extends CharNode {

	public abstract AbstractCharClassNode invert(char min, char max);

}
