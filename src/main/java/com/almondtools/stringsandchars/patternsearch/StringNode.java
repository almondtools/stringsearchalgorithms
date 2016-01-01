package com.almondtools.stringsandchars.patternsearch;

import java.util.ArrayList;
import java.util.List;

public class StringNode implements RegexNode, JoinableNode {

	private String value;

	public StringNode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String getLiteralValue() {
		return value;
	}
	
	public List<CharNode> toCharNodes() {
		List<CharNode> charNodes = new ArrayList<>(value.length());
		for (char c : value.toCharArray()) {
			charNodes.add(new SingleCharNode(c));
		}
		return charNodes;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitString(this);
	}

	@Override
	public StringNode clone() {
		try {
			return (StringNode) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
