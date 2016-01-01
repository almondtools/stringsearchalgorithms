package com.almondtools.stringsandchars.patternsearch;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CharClassAnalyzer implements RegexNodeVisitor<CharClassAnalyzer> {

	private SortedSet<DefinedCharNode> charClasses;
	
	public CharClassAnalyzer() {
		charClasses = new TreeSet<DefinedCharNode>();
	}
	
	public SortedSet<DefinedCharNode> getCharClasses() {
		return charClasses;
	}
	
	@Override
	public CharClassAnalyzer visitAlternatives(AlternativesNode node) {
		return accept(node.getSubNodes());
	}

	@Override
	public CharClassAnalyzer visitAnyChar(AnyCharNode node) {
		return accept(node.toCharNodes());
	}

	@Override
	public CharClassAnalyzer visitCharClass(CharClassNode node) {
		return accept(node.toCharNodes());
	}

	@Override
	public CharClassAnalyzer visitCompClass(CompClassNode node) {
		return accept(node.toCharNodes());
	}

	@Override
	public CharClassAnalyzer visitConcat(ConcatNode node) {
		return accept(node.getSubNodes());
	}

	@Override
	public CharClassAnalyzer visitEmpty(EmptyNode node) {
		return this;
	}

	@Override
	public CharClassAnalyzer visitGroup(GroupNode node) {
		return node.getSubNode().accept(this);
	}

	@Override
	public CharClassAnalyzer visitBoundedLoop(BoundedLoopNode node) {
		return node.getSubNode().accept(this);
	}

	@Override
	public CharClassAnalyzer visitUnboundedLoop(UnboundedLoopNode node) {
		return node.getSubNode().accept(this);
	}

	@Override
	public CharClassAnalyzer visitOptional(OptionalNode node) {
		return node.getSubNode().accept(this);
	}

	@Override
	public CharClassAnalyzer visitRangeChar(RangeCharNode node) {
		return splitBy(node);
	}

	@Override
	public CharClassAnalyzer visitSingleChar(SingleCharNode node) {
		return splitBy(node);
	}

	private CharClassAnalyzer splitBy(DefinedCharNode node) {
		List<DefinedCharNode> splitNodes = new ArrayList<>();
		Iterator<DefinedCharNode> charClassIterator = charClasses.iterator();
		while (charClassIterator.hasNext()) {
			DefinedCharNode next = charClassIterator.next();
			if (next.cuts(node)) {
				charClassIterator.remove();
				splitNodes.addAll(splitClasses(node, next));
			}
		}
		if (splitNodes.isEmpty()) {
			charClasses.add(node);
		} else {
			charClasses.addAll(splitNodes);
		}
		return this;
	}

	private List<DefinedCharNode> splitClasses(DefinedCharNode node, DefinedCharNode next) {
		if (next.getFrom() < node.getFrom()) {
			DefinedCharNode temp = node;
			node = next;
			next = temp;
		}
		if (node.overlaps(next)) {
			return ranges(
				new RangeCharNode(node.getFrom(), (char) (next.getFrom() + 1)), 
				new RangeCharNode(next.getFrom(), node.getTo()),
				new RangeCharNode((char) (node.getTo() + 1), next.getTo()));
		} else if (node.subsumes(next)) {
			return ranges(
				new RangeCharNode(node.getFrom(), (char) (next.getFrom() + 1)), 
				next,
				new RangeCharNode((char) (next.getTo() + 1), node.getTo()));
		} else if (next.subsumes(node)) {
			return ranges(
				new RangeCharNode(next.getFrom(), (char) (node.getFrom() + 1)), 
				node,
				new RangeCharNode((char) (node.getTo() + 1), next.getTo()));
		} else {
			return asList(node, next);
		}
	}

	private List<DefinedCharNode> ranges(DefinedCharNode... nodes) {
		List<DefinedCharNode> ranges = new ArrayList<>(nodes.length);
		for (DefinedCharNode node : nodes) {
			if (node instanceof RangeCharNode) {
				DefinedCharNode optimized = ((RangeCharNode) node).optimize();
				if (optimized != null) {
					ranges.add(optimized);
				}
			} else {
				ranges.add(node);
			}
		}
		return ranges;
	}

	@Override
	public CharClassAnalyzer visitSpecialCharClass(SpecialCharClassNode node) {
		return accept(node.toCharNodes());
	}

	@Override
	public CharClassAnalyzer visitString(StringNode node) {
		return accept(node.toCharNodes());
	}

	private CharClassAnalyzer accept(List<? extends RegexNode> subNodes) {
		for (RegexNode subNode : subNodes) {
			subNode.accept(this);
		}
		return this;
	}

}
