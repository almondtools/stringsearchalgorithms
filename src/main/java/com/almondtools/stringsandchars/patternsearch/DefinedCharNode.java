package com.almondtools.stringsandchars.patternsearch;

import static java.util.Arrays.asList;

import java.util.List;

public abstract class DefinedCharNode extends CharNode implements Comparable<DefinedCharNode> {

	public abstract char getFrom();

	public abstract char getTo();

	@Override
	public List<DefinedCharNode> toCharNodes() {
		return asList(this);
	}

	public int size() {
		char to = getTo();
		char from = getFrom();
		return to - from + 1;
	}

	public char[] chars() {
		char to = getTo();
		char from = getFrom();
		char[] chars = new char[to - from + 1];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (from + i);
		}
		return chars;
	}

	@Override
	public int compareTo(DefinedCharNode o) {
		int result = getFrom() - o.getFrom();
		if (result == 0) {
			result = getTo() - o.getTo();
		}
		return result;
	}

	public boolean cuts(DefinedCharNode node) {
		if (node.getFrom() == this.getFrom() && node.getTo() == this.getTo()) {
			return false;
		}
		return this.overlaps(node)
			|| this.subsumes(node)
			|| node.subsumes(this);
	}

	public boolean overlaps(DefinedCharNode node) {
		return this.getFrom() < node.getFrom() && node.getFrom() <= this.getTo() && this.getTo() < node.getTo()
			|| node.getFrom() < this.getFrom() && this.getFrom() <= node.getTo() && node.getTo() < this.getTo();
	}

	public boolean subsumes(DefinedCharNode node) {
		return this.getFrom() <= node.getFrom()
			&& node.getTo() <= this.getTo();
	}

	public abstract String toInlinedString();

}
