package com.almondtools.stringsandchars.patternsearch;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

public class CharClassNode extends CharNode implements RegexNode {

	private List<DefinedCharNode> charNodes;

	public CharClassNode(DefinedCharNode... charNodes) {
		this(asList(charNodes));
	}

	public CharClassNode(List<DefinedCharNode> charNodes) {
		this.charNodes = charNodes;
	}

	@Override
	public List<DefinedCharNode> toCharNodes() {
		return charNodes;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitCharClass(this);
	}

	@Override
	public CharClassNode clone() {
		return (CharClassNode) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		Iterator<DefinedCharNode> charNodeIterator = charNodes.iterator();
		while (charNodeIterator.hasNext()) {
			buffer.append(charNodeIterator.next().toInlinedString());
		}
		buffer.append(']');
		return buffer.toString();
	}

}
