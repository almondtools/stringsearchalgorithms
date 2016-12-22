package net.amygdalum.util.tries;

public class CharTrieTerminalNode<T> extends CharTrieLeafNode<T> implements CharTrieNode<T> {

	private static final char[] NONE = new char[0];
	
	private T attached;

	public CharTrieTerminalNode(T attached) {
		this.attached = attached;
	}
	
	@Override
	public int length() {
		return 0;
	}

	@Override
	public CharTrieNode<T> nextNode(char c) {
		return null;
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars) {
		return null;
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars, int i) {
		return null;
	}

	@Override
	public T getAttached() {
		return attached;
	}

	@Override
	public char[] getAlternatives() {
		return NONE;
	}

}
