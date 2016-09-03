package net.amygdalum.stringsearchalgorithms.patternsearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.regex.AlternativesNode;
import net.amygdalum.stringsearchalgorithms.regex.AnyCharNode;
import net.amygdalum.stringsearchalgorithms.regex.BoundedLoopNode;
import net.amygdalum.stringsearchalgorithms.regex.CharClassNode;
import net.amygdalum.stringsearchalgorithms.regex.CompClassNode;
import net.amygdalum.stringsearchalgorithms.regex.ConcatNode;
import net.amygdalum.stringsearchalgorithms.regex.EmptyNode;
import net.amygdalum.stringsearchalgorithms.regex.GroupNode;
import net.amygdalum.stringsearchalgorithms.regex.OptionalNode;
import net.amygdalum.stringsearchalgorithms.regex.RangeCharNode;
import net.amygdalum.stringsearchalgorithms.regex.RegexNode;
import net.amygdalum.stringsearchalgorithms.regex.RegexNodeVisitor;
import net.amygdalum.stringsearchalgorithms.regex.SingleCharNode;
import net.amygdalum.stringsearchalgorithms.regex.SpecialCharClassNode;
import net.amygdalum.stringsearchalgorithms.regex.StringNode;
import net.amygdalum.stringsearchalgorithms.regex.UnboundedLoopNode;

public class GlushkovNormalizer implements RegexNodeVisitor<RegexNode> {
	
	public GlushkovNormalizer() {
	}
	
	@Override
	public RegexNode visitAlternatives(AlternativesNode node) {
		List<RegexNode> subNodes = node.getSubNodes();
		List<RegexNode> newNodes = accept(subNodes);

		if (newNodes.equals(subNodes)) {
			return node;
		} else {
			return AlternativesNode.anyOf(newNodes.toArray(new RegexNode[0]));
		}
	}

	@Override
	public RegexNode visitAnyChar(AnyCharNode node) {
		return AlternativesNode.anyOf(node.toCharNodes());
	}

	@Override
	public RegexNode visitCharClass(CharClassNode node) {
		return AlternativesNode.anyOf(node.toCharNodes());
	}

	@Override
	public RegexNode visitCompClass(CompClassNode node) {
		return AlternativesNode.anyOf(node.toCharNodes());
	}

	@Override
	public RegexNode visitConcat(ConcatNode node) {
		List<RegexNode> subNodes = node.getSubNodes();
		List<RegexNode> newNodes = accept(subNodes);

		if (newNodes.equals(subNodes)) {
			return node;
		} else {
			return ConcatNode.inSequence(newNodes.toArray(new RegexNode[0]));
		}
	}

	@Override
	public RegexNode visitEmpty(EmptyNode node) {
		return node;
	}

	@Override
	public RegexNode visitGroup(GroupNode node) {
		RegexNode subNode = node.getSubNode();
		RegexNode newNode = subNode.accept(this);

		if (newNode == subNode) {
			return node;
		} else {
			return new GroupNode(newNode);
		}
	}

	@Override
	public RegexNode visitBoundedLoop(BoundedLoopNode node) {
		RegexNode subNode = node.getSubNode();
		RegexNode newNode = subNode.accept(this);

		List<RegexNode> nodes = new LinkedList<RegexNode>();

		if (node.getFrom() > 0) {
			nodes.add(newNode);
			for (int i = 1; i < node.getFrom(); i++) {
				nodes.add(newNode.clone());
			}
		}
		for (int i = node.getFrom(); i < node.getTo(); i++) {
			nodes.add(OptionalNode.optional(newNode.clone()));
		}

		if (nodes.isEmpty()) {
			return new EmptyNode();
		} else if (nodes.size() == 1) {
			if (nodes.get(0) == subNode) {
				return node;
			} else {
				return ConcatNode.inSequence(ConcatNode.inSequence(nodes.toArray(new RegexNode[0])));
			}
		} else {
			return ConcatNode.inSequence(ConcatNode.inSequence(nodes.toArray(new RegexNode[0])));
		}
	}

	@Override
	public RegexNode visitUnboundedLoop(UnboundedLoopNode node) {
		RegexNode subNode = node.getSubNode();
		RegexNode newNode = subNode.accept(this);

		List<RegexNode> prefix = new LinkedList<RegexNode>();

		if (node.getFrom() > 0) {
			for (int i = 0; i < node.getFrom(); i++) {
				prefix.add(newNode.clone());
			}
		}
		if (prefix.isEmpty()) {
			if (newNode == subNode) {
				return node;
			} else {
				return UnboundedLoopNode.star(newNode);
			}
		} else {
			return ConcatNode.inSequence(ConcatNode.inSequence(prefix.toArray(new RegexNode[0])), UnboundedLoopNode.star(newNode));
		}
	}

	@Override
	public RegexNode visitOptional(OptionalNode node) {
		RegexNode subNode = node.getSubNode();
		RegexNode newNode = subNode.accept(this);

		if (newNode == subNode) {
			return node;
		} else {
			return OptionalNode.optional(newNode);
		}
	}

	@Override
	public RegexNode visitRangeChar(RangeCharNode node) {
		return node;
	}

	@Override
	public RegexNode visitSingleChar(SingleCharNode node) {
		return node;
	}

	@Override
	public RegexNode visitSpecialCharClass(SpecialCharClassNode node) {
		return node;
	}

	@Override
	public RegexNode visitString(StringNode node) {
		char[] chars = node.getValue().toCharArray();
		RegexNode[] nodes = new RegexNode[chars.length];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new SingleCharNode(chars[i]);
		}
		return ConcatNode.inSequence(nodes);
	}

	private List<RegexNode> accept(List<RegexNode> subNodes) {
		List<RegexNode> newNodes = new ArrayList<RegexNode>(subNodes.size());
		for (RegexNode subNode : subNodes) {
			RegexNode newNode = subNode.accept(this);
			newNodes.add(newNode);
		}
		return newNodes;
	}

}
