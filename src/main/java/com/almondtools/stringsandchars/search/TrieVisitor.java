package com.almondtools.stringsandchars.search;

public interface TrieVisitor<T> {

	void visitRoot(TrieRoot trie, T data);
	void visitNode(TrieNode trie, T data);
	
}
