package com.almondtools.stringsandchars.regex;

import static com.almondtools.util.text.CharUtils.after;
import static com.almondtools.util.text.CharUtils.before;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class CharNode implements RegexNode {

	public abstract List<DefinedCharNode> toCharNodes();

	@Override
	public CharNode clone() {
		try {
			return (CharNode) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public static List<DefinedCharNode> computeComplement(List<? extends DefinedCharNode> nodes, char min, char max) {
		Collections.sort(nodes);
		List<DefinedCharNode> remainderNodes = new LinkedList<DefinedCharNode>();
		char current = min;
		for (DefinedCharNode node : nodes) {
			char from = node.getFrom();
			char to = node.getTo();
			if (current + 1 == from) {
				remainderNodes.add(new SingleCharNode(current));
			} else if (current < from) {
				remainderNodes.add(new RangeCharNode(current, before(from)).simplify());
			}
			current = after(to);
		}
		if (current == max) {
			remainderNodes.add(new SingleCharNode(current));
		} else if (current == after(max)) {
			// overflow from previous loop => do nothing
		} else if (current < max) {
			remainderNodes.add(new RangeCharNode(current, max).simplify());
		}
		return remainderNodes;
	}

}
