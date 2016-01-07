package com.almondtools.stringsandchars.regex;

import static com.almondtools.util.text.CharUtils.charToString;

public class SingleCharNode extends DefinedCharNode implements JoinableNode {

	private char value;

	public SingleCharNode(char value) {
		this.value = value;
	}
	
	public char getValue() {
		return value;
	}
	
	@Override
	public char getFrom() {
		return value;
	}
	
	@Override
	public char getTo() {
		return value;
	}

	@Override
	public String getLiteralValue() {
		return String.valueOf(value);
	}
	
	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitSingleChar(this);
	}
	
	@Override
	public SingleCharNode clone() {
		return (SingleCharNode) super.clone();
	}

	@Override
	public String toString() {
		return toInlinedString();
	}
	
	@Override
	public String toInlinedString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(charToString(value));
		return buffer.toString();
	}

}
