package net.amygdalum.stringsearchalgorithms.regex;

import static java.util.Arrays.asList;

import java.util.List;

public class AnyCharNode extends CharNode {

	private List<DefinedCharNode> charNodes;

	private AnyCharNode(DefinedCharNode... charNodes) {
		this(asList(charNodes));
	}

	private AnyCharNode(List<DefinedCharNode> charNodes) {
		this.charNodes = charNodes;
	}

	public static AnyCharNode dotAll(char min, char max) {
		return new AnyCharNode(new RangeCharNode(min, max));
	}

	public static AnyCharNode dotDefault(char min, char max) {
		return new AnyCharNode(computeDefault(min, max));
	}

	private static DefinedCharNode[] computeDefault(char min, char max) {
		return computeComplement(asList(new SingleCharNode('\n'),
			new SingleCharNode('\r'),
			new SingleCharNode('\u0085'),
			new RangeCharNode('\u2028', '\u2029')), min, max).toArray(new DefinedCharNode[0]);
	}

	@Override
	public List<DefinedCharNode> toCharNodes() {
		return charNodes;
	}

	@Override
	public <T> T accept(RegexNodeVisitor<T> visitor) {
		return visitor.visitAnyChar(this);
	}

	@Override
	public AnyCharNode clone() {
		return (AnyCharNode) super.clone();
	}

	@Override
	public String toString() {
		return ".";
	}

}
