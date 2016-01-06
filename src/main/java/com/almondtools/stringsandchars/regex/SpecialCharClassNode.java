package com.almondtools.stringsandchars.regex;

import static java.util.Arrays.asList;

import java.util.List;

public class SpecialCharClassNode extends CharNode {

	private char symbol;
	private List<DefinedCharNode> charNodes;

	public SpecialCharClassNode(char symbol, DefinedCharNode... charNodes) {
		this(symbol, asList(charNodes));
	}

	public SpecialCharClassNode(char symbol, List<DefinedCharNode> charNodes) {
		this.symbol = symbol;
		this.charNodes = charNodes;
	}

	@Override
	public List<DefinedCharNode> toCharNodes() {
		return charNodes;
	}

	public SpecialCharClassNode invert() {
		return new SpecialCharClassNode(Character.toUpperCase(symbol), computeComplement(charNodes));
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitSpecialCharClass(this);
	}

	@Override
	public SpecialCharClassNode clone() {
		return (SpecialCharClassNode) super.clone();
	}
	
	@Override
	public String toString() {
		return "\\" + symbol;
	}

}
