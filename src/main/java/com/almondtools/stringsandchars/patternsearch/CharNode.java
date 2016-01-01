package com.almondtools.stringsandchars.patternsearch;

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

	public List<DefinedCharNode> computeComplement(List<? extends DefinedCharNode> nodes) {
		Collections.sort(nodes);
		List<DefinedCharNode> remainderNodes = new LinkedList<DefinedCharNode>();
		char current = Character.MIN_VALUE;
		for (DefinedCharNode node : nodes) {
			char from = node.getFrom();
			char to = node.getTo();
			if (current + 1 == from) {
				remainderNodes.add(new SingleCharNode(current));
			} else if (current < from) {
				remainderNodes.add(new RangeCharNode(current, before(from)).optimize());
			}
			current = after(to);
		}
		if (current == Character.MAX_VALUE) {
			remainderNodes.add(new SingleCharNode(current));
		} else if (current == after(Character.MAX_VALUE)) {
			// overflow from previous loop => do nothing
		} else if (current < Character.MAX_VALUE) {
			remainderNodes.add(new RangeCharNode(current, Character.MAX_VALUE).optimize());
		}
		return remainderNodes;
	}

}
