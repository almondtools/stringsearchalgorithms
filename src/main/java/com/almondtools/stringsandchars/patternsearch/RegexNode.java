package com.almondtools.stringsandchars.patternsearch;

public interface RegexNode extends Cloneable {

	<T> T accept(RegexNodeVisitor<T> visitor);

	RegexNode clone();
	
}
