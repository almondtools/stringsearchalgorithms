package com.almondtools.stringsandchars.patternsearch;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ConcatNode implements RegexNode {

	private List<RegexNode> subNodes;

	private ConcatNode(List<RegexNode> subNodes) {
		this.subNodes = subNodes;
	}

	public static ConcatNode inSequence(RegexNode... nodes) {
		return inSequence(asList(nodes));
	}

	public static ConcatNode inSequence(List<? extends RegexNode> nodes) {
		List<RegexNode> subNodes = new LinkedList<RegexNode>();
		for (RegexNode node : nodes) {
			if (node instanceof ConcatNode) {
				subNodes.addAll(((ConcatNode) node).getSubNodes());
			} else {
				subNodes.add(node);
			}
		}
		return new ConcatNode(subNodes);
	}
	
	public RegexNode optimize() {
		List<RegexNode> newSubNodes = joinIfPossible(subNodes);
		if (newSubNodes.equals(subNodes)) {
			return this;
		} else if (newSubNodes.size() == 1) {
			return newSubNodes.get(0);
		} else {
			return new ConcatNode(newSubNodes);
		}

	}

	private static List<RegexNode> joinIfPossible(List<RegexNode> nodes) {
		LinkedList<RegexNode> joinedNodes = new LinkedList<RegexNode>();
		for (RegexNode node : nodes) {
			RegexNode last = joinedNodes.isEmpty() ? null : joinedNodes.getLast();
			if (last != null && last instanceof JoinableNode && node instanceof JoinableNode) {
				last = joinChars((JoinableNode) last, (JoinableNode) node);
				joinedNodes.removeLast();
				joinedNodes.add(last);
			} else {
				joinedNodes.add(node);
			}
		}
		return joinedNodes;
	}

	private static StringNode joinChars(JoinableNode node1, JoinableNode node2) {
		return new StringNode(new StringBuilder()
			.append(node1.getLiteralValue())
			.append(node2.getLiteralValue())
			.toString());
	}

	public List<RegexNode> getSubNodes() {
		return subNodes;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitConcat(this);
	}

	@Override
	public ConcatNode clone() {
		try {
			ConcatNode clone = (ConcatNode) super.clone();
			clone.subNodes = new ArrayList<RegexNode>(subNodes.size());
			for (RegexNode subNode : subNodes) {
				clone.subNodes.add(subNode.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		Iterator<RegexNode> subNodeIterator = subNodes.iterator();
		while (subNodeIterator.hasNext()) {
			buffer.append(subNodeIterator.next());
		}
		return buffer.toString();
	}

}
