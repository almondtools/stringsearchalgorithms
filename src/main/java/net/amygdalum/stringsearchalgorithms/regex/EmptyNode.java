package net.amygdalum.stringsearchalgorithms.regex;

public class EmptyNode implements RegexNode, JoinableNode {

	public EmptyNode() {
	}
	
	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitEmpty(this);
	}

	@Override
	public String getLiteralValue() {
		return "";
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
