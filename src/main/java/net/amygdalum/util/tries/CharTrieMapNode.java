package net.amygdalum.util.tries;

import net.amygdalum.util.map.CharObjectMap;

public class CharTrieMapNode<T> extends CharTrieInnerNode<T> implements CharTrieNode<T> {

	private CharObjectMap<CharTrieNode<T>> nexts;
	private char[] alts;
	
	public CharTrieMapNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		super(attached);
		this.nexts = nexts;
		this.alts = nexts.keys();
	}

	@Override
	public CharTrieNode<T> nextNode(char c) {
		return nexts.get(c);
	}

	@Override
	public char[] getAlternatives() {
		return alts;
	}

}
