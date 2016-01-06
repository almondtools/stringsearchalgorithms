package com.almondtools.stringsandchars.regex;

import static java.util.Arrays.asList;

import java.util.List;

public class SpecialCharClassNode extends AbstractCharClassNode {

	private char symbol;
	private List<DefinedCharNode> charNodes;

	public SpecialCharClassNode(char symbol, DefinedCharNode... charNodes) {
		this(symbol, asList(charNodes));
	}

	public SpecialCharClassNode(char symbol, List<DefinedCharNode> charNodes) {
		this.symbol = symbol;
		this.charNodes = charNodes;
	}
	
	public char getSymbol() {
		return symbol;
	}

	@Override
	public List<DefinedCharNode> toCharNodes() {
		return charNodes;
	}

	@Override
	public SpecialCharClassNode invert(char min, char max) {
		return new SpecialCharClassNode(Character.toUpperCase(symbol), computeComplement(charNodes, min, max));
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
