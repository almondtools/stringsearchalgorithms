package net.amygdalum.util.tries;

public abstract class ByteTrieLeafNode<T> implements ByteTrieNode<T> {

	private ByteTrieNode<T> link;

	public abstract int length();

	@Override
	public void link(ByteTrieNode<T> node) {
		this.link = node;
	}
	
	@Override
	public ByteTrieNode<T> getLink() {
		return link;
	}
	
}
