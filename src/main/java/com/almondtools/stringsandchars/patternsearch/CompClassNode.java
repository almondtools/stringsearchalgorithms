package com.almondtools.stringsandchars.patternsearch;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

public class CompClassNode extends CharNode implements RegexNode {

	private List<DefinedCharNode> charNodes;

	public CompClassNode(DefinedCharNode... charNodes) {
		this(asList(charNodes));
	}

	public CompClassNode(List<DefinedCharNode> charNodes) {
		this.charNodes = charNodes;
	}
	
	@Override
	public List<DefinedCharNode> toCharNodes() {
		return computeComplement(charNodes);
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitCompClass(this);
	}

	@Override
	public CompClassNode clone() {
		return (CompClassNode) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[^");
		Iterator<DefinedCharNode> charNodeIterator = charNodes.iterator();
		while (charNodeIterator.hasNext()) {
			buffer.append(charNodeIterator.next());
		}
		buffer.append(']');
		return buffer.toString();
	}

}
