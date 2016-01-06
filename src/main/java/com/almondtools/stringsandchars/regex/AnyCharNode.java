package com.almondtools.stringsandchars.regex;

import static com.almondtools.util.text.CharUtils.after;
import static com.almondtools.util.text.CharUtils.before;
import static java.util.Arrays.asList;

import java.util.List;

public class AnyCharNode extends CharNode {

	private static final DefinedCharNode DOTALL = new RangeCharNode(Character.MIN_VALUE, Character.MAX_VALUE);

	private static final DefinedCharNode[] DEFAULT = new DefinedCharNode[]{
		new RangeCharNode(Character.MIN_VALUE, before('\n')),
		new RangeCharNode(after('\n'), before('\r')),
		new RangeCharNode(after('\r'), before('\u0085')),
		new RangeCharNode(after('\u0085'), before('\u2028')),
		new RangeCharNode(after('\u2029'), Character.MAX_VALUE)
	};

	private List<DefinedCharNode> charNodes;

	private AnyCharNode(DefinedCharNode... charNodes) {
		this(asList(charNodes));
	}

	private AnyCharNode(List<DefinedCharNode> charNodes) {
		this.charNodes = charNodes;
	}
	
	public static AnyCharNode dotAll() {
		return new AnyCharNode(DOTALL);
	}

	public static AnyCharNode dotDefault() {
		return new AnyCharNode(DEFAULT);
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
