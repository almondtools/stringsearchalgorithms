package com.almondtools.stringsandchars.search;

public class TrieTrie {
	private Trie trie;
	private Trie parent;
	
	public TrieTrie(Trie trie, Trie parent) {
		this.trie = trie;
		this.parent = parent;
	}
	
	public Trie getTrie() {
		return trie;
	}
	
	public Trie getParent() {
		return parent;
	}
}