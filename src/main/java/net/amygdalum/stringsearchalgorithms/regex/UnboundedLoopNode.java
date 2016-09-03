package net.amygdalum.stringsearchalgorithms.regex;

public class UnboundedLoopNode implements RegexNode {

	private RegexNode subNode;
	private int from;

	private UnboundedLoopNode(RegexNode subNode, int from) {
		this.subNode = subNode;
		this.from = from;
	}

	public static UnboundedLoopNode star(RegexNode node) {
		return new UnboundedLoopNode(node, 0);
	}

	public static UnboundedLoopNode plus(RegexNode node) {
		return new UnboundedLoopNode(node, 1);
	}

	public static UnboundedLoopNode unbounded(RegexNode node, int from) {
		return new UnboundedLoopNode(node, from);
	}
	
	public int getFrom() {
		return from;
	}
	
	public RegexNode getSubNode() {
		return subNode;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitUnboundedLoop(this);
	}
	
	@Override
	public UnboundedLoopNode clone() {
		try {
			UnboundedLoopNode clone = (UnboundedLoopNode) super.clone();
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
		if (from == 0) {
			buffer.append('*');
		} else if (from == 1) {
			buffer.append('+');
		} else {
			buffer.append('{');
			buffer.append(from);
			buffer.append(',');
			buffer.append('}');
		}
		return buffer.toString();
	}

}
