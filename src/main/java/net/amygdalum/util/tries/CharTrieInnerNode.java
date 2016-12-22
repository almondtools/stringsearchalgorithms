package net.amygdalum.util.tries;

public abstract class CharTrieInnerNode<T> implements CharTrieNode<T> {

	private CharTrieNode<T> link;
	private T attached;

	public CharTrieInnerNode(T attached) {
		this.attached = attached;
	}

	@Override
	public void link(CharTrieNode<T> node) {
		this.link = node;
	}
	
	@Override
	public CharTrieNode<T> getLink() {
		return link;
	}
	
	@Override
	public T getAttached() {
		return attached;
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars) {
		CharTrieNode<T> current = this;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (current instanceof CharTrieLeafNode) {
				current = current.nextNode(chars, i);
				return current;
			} else {
				current = current.nextNode(c);
				if (current == null) {
					return null;
				}
			}
		}
		return current;
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars, int start) {
		CharTrieNode<T> current = this;
		for (int i = start; i < chars.length; i++) {
			char c = chars[i];
			if (current instanceof CharTrieLeafNode) {
				current = current.nextNode(chars, i);
				return current;
			} else {
				current = current.nextNode(c);
				if (current == null) {
					return null;
				}
			}
		}
		return current;
	}

}
