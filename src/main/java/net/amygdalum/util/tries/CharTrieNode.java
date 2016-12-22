package net.amygdalum.util.tries;

public interface CharTrieNode<T> {

	CharTrieNode<T> nextNode(char c);

	CharTrieNode<T> nextNode(char[] chars);
	
	CharTrieNode<T> nextNode(char[] chars, int start);

	T getAttached();

	char[] getAlternatives();
	
	void link(CharTrieNode<T> node);
	
	CharTrieNode<T> getLink();

}
