package net.amygdalum.util.tries;

public abstract class CharTrieLeafNode<T> implements CharTrieNode<T> {

	private CharTrieNode<T> link;

	public abstract int length();

	@Override
	public void link(CharTrieNode<T> node) {
		this.link = node;
	}
	
	@Override
	public CharTrieNode<T> getLink() {
		return link;
	}
	
}
