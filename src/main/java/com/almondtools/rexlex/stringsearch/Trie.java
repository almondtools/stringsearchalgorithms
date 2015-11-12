package com.almondtools.rexlex.stringsearch;

import java.util.List;

public interface Trie {

	<T> void apply(TrieVisitor<T> visitor, T data);

	List<? extends Trie> getNexts();

	void addNext(TrieNode node);

	Trie nextNode(char c);

	Trie nextNode(char[] c);
	
	boolean isTerminal();

	void setTerminal(int length);

	int length();

}
