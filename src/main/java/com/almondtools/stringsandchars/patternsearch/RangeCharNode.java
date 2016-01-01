package com.almondtools.stringsandchars.patternsearch;


public class RangeCharNode extends DefinedCharNode {

	private char from;
	private char to;

	public RangeCharNode(char from, char to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public char getFrom() {
		return from;
	}
	
	@Override
	public char getTo() {
		return to;
	}
	
	public DefinedCharNode optimize() {
		if (from > to) {
			return null;
		} else if (from == to) {
			return new SingleCharNode(from);
		} else {
			return this;
		}
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitRangeChar(this);
	}
	
	@Override
	public RangeCharNode clone() {
		return (RangeCharNode) super.clone();
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		buffer.append(toInlinedString());
		buffer.append(']');
		return buffer.toString();
	}
	
	@Override
	public String toInlinedString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(from);
		buffer.append('-');
		buffer.append(to);
		return buffer.toString();
	}

}
