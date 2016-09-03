package net.amygdalum.stringsearchalgorithms.regex;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AlternativesNode implements RegexNode {

	private List<RegexNode> subNodes;

	private AlternativesNode(List<RegexNode> subNodes) {
		this.subNodes = subNodes;
	}

	public static AlternativesNode anyOf(RegexNode... nodes) {
		return anyOf(asList(nodes));
	}

	public static AlternativesNode anyOf(List<? extends RegexNode> nodes) {
		List<RegexNode> subNodes = new LinkedList<RegexNode>();
		for (RegexNode node : nodes) {
			if (node instanceof AlternativesNode) {
				subNodes.addAll(((AlternativesNode) node).getSubNodes());
			} else {
				subNodes.add(node);
			}
		}
		return new AlternativesNode(subNodes);
	}

	public RegexNode simplify() {
		if (subNodes.isEmpty()) {
			return new EmptyNode();
		} else if (subNodes.size() == 1) {
			return subNodes.get(0);
		} else {
			return this;
		}
	}

	public List<RegexNode> getSubNodes() {
		return subNodes;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitAlternatives(this);
	}

	@Override
	public AlternativesNode clone() {
		try {
			AlternativesNode clone = (AlternativesNode) super.clone();
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
		if (subNodeIterator.hasNext()) {
			buffer.append(subNodeIterator.next());
		}
		while (subNodeIterator.hasNext()) {
			buffer.append('|');
			buffer.append(subNodeIterator.next());
		}
		return buffer.toString();
	}

}
