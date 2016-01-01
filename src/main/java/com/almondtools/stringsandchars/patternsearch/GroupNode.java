package com.almondtools.stringsandchars.patternsearch;

public class GroupNode implements RegexNode {

	private RegexNode subNode;

	public GroupNode(RegexNode subNode) {
		this.subNode = subNode;
	}

	public RegexNode getSubNode() {
		return subNode;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitGroup(this);
	}

	@Override
	public GroupNode clone() {
		try {
			GroupNode clone = (GroupNode) super.clone();
			clone.subNode = subNode.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return '(' + subNode.toString() + ')';
	}

}
