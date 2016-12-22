package net.amygdalum.util.tries;

public interface ByteTrieNode<T> {

	ByteTrieNode<T> nextNode(byte b);

	ByteTrieNode<T> nextNode(byte[] bytes);
	
	ByteTrieNode<T> nextNode(byte[] bytes, int start);

	T getAttached();

	byte[] getAlternatives();
	
	void link(ByteTrieNode<T> node);
	
	ByteTrieNode<T> getLink();

}
