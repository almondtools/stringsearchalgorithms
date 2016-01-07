package com.almondtools.stringsandchars.regex;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

public class CharClassNode extends AbstractCharClassNode implements RegexNode {

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
	public CompClassNode invert(char min, char max) {
		return new CompClassNode(charNodes, computeComplement(charNodes, min, max));
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
			DefinedCharNode next = charNodeIterator.next();
			buffer.append(next.toInlinedString());
		}
		buffer.append(']');
		return buffer.toString();
	}

}
