package net.amygdalum.stringsearchalgorithms.regex;

import java.util.Iterator;
import java.util.List;

public class CompClassNode extends AbstractCharClassNode implements RegexNode {

	private List<DefinedCharNode> charNodes;
	private List<DefinedCharNode> invertedNodes;

	public CompClassNode(List<DefinedCharNode> charNodes, List<DefinedCharNode> invertedNodes) {
		this.charNodes = charNodes;
		this.invertedNodes = invertedNodes;
	}
	
	@Override
	public List<DefinedCharNode> toCharNodes() {
		return invertedNodes;
	}
	
	@Override
	public CharClassNode invert(char min, char max) {
		return new CharClassNode(charNodes);
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
			DefinedCharNode next = charNodeIterator.next();
			buffer.append(next.toInlinedString());
		}
		buffer.append(']');
		return buffer.toString();
	}

}
