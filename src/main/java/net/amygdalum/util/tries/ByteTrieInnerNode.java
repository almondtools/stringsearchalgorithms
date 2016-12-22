package net.amygdalum.util.tries;

public abstract class ByteTrieInnerNode<T> implements ByteTrieNode<T> {

	private ByteTrieNode<T> link;
	private T attached;

	public ByteTrieInnerNode(T attached) {
		this.attached = attached;
	}

	@Override
	public void link(ByteTrieNode<T> node) {
		this.link = node;
	}
	
	@Override
	public ByteTrieNode<T> getLink() {
		return link;
	}
	
	@Override
	public T getAttached() {
		return attached;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes) {
		ByteTrieNode<T> current = this;
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			if (current instanceof ByteTrieLeafNode) {
				current = current.nextNode(bytes, i);
				return current;
			} else {
				current = current.nextNode(b);
				if (current == null) {
					return null;
				}
			}
		}
		return current;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes, int start) {
		ByteTrieNode<T> current = this;
		for (int i = start; i < bytes.length; i++) {
			byte b = bytes[i];
			if (current instanceof ByteTrieLeafNode) {
				current = current.nextNode(bytes, i);
				return current;
			} else {
				current = current.nextNode(b);
				if (current == null) {
					return null;
				}
			}
		}
		return current;
	}

}
