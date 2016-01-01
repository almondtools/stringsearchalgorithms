package com.almondtools.stringsandchars.patternsearch;

public class EmptyNode implements RegexNode {

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitEmpty(this);
	}

	@Override
	public EmptyNode clone() {
		try {
			return (EmptyNode) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "";
	}

}
