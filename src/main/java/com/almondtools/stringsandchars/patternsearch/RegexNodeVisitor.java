package com.almondtools.stringsandchars.patternsearch;

public interface RegexNodeVisitor<T> {

	T visitAlternatives(AlternativesNode node);
	T visitAnyChar(AnyCharNode node);
	T visitCharClass(CharClassNode node);
	T visitCompClass(CompClassNode node);
	T visitConcat(ConcatNode node);
	T visitEmpty(EmptyNode node);
	T visitGroup(GroupNode node);
	T visitBoundedLoop(BoundedLoopNode node);
	T visitUnboundedLoop(UnboundedLoopNode node);
	T visitOptional(OptionalNode node);
	T visitRangeChar(RangeCharNode node);
	T visitSingleChar(SingleCharNode node);
	T visitSpecialCharClass(SpecialCharClassNode node);
	T visitString(StringNode node);

}
