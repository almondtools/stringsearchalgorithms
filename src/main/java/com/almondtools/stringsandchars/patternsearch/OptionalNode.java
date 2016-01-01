package com.almondtools.stringsandchars.patternsearch;

public class OptionalNode implements RegexNode {

	private RegexNode subNode;

	private OptionalNode(RegexNode subNode) {
		this.subNode = subNode;
	}
	
	public static OptionalNode optional(RegexNode node) {
		return new OptionalNode(node);
	}
	
	public RegexNode getSubNode() {
		return subNode;
	}
	
	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitOptional(this);
	}

	@Override
	public OptionalNode clone() {
		try {
			OptionalNode clone = (OptionalNode) super.clone();
			clone.subNode = subNode.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return subNode.toString() + '?';
	}

}
