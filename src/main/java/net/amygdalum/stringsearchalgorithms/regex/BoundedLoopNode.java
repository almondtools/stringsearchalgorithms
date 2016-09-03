package net.amygdalum.stringsearchalgorithms.regex;


public class BoundedLoopNode implements RegexNode {

	private RegexNode subNode;
	private int from;
	private int to;

	private BoundedLoopNode(RegexNode subNode, int from, int to) {
		this.subNode = subNode;
		this.from = from;
		this.to = to;
	}

	public static BoundedLoopNode bounded(RegexNode node, int from, int to) {
		return new BoundedLoopNode(node, from, to);
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public RegexNode getSubNode() {
		return subNode;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitBoundedLoop(this);
	}

	@Override
	public BoundedLoopNode clone() {
		try {
			BoundedLoopNode clone = (BoundedLoopNode) super.clone();
			clone.subNode = subNode.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(subNode);
		buffer.append('{');
		buffer.append(from);
		if (to != from) {
			buffer.append(',');
			buffer.append(to);
		}
		buffer.append('}');
		return buffer.toString();
	}

}
