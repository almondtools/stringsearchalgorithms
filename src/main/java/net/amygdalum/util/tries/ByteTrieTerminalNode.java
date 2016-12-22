package net.amygdalum.util.tries;

public class ByteTrieTerminalNode<T> extends ByteTrieLeafNode<T> implements ByteTrieNode<T> {

	private static final byte[] NONE = new byte[0];
	
	private T attached;

	public ByteTrieTerminalNode(T attached) {
		this.attached = attached;
	}
	
	@Override
	public int length() {
		return 0;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte b) {
		return null;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes) {
		return null;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes, int i) {
		return null;
	}

	@Override
	public T getAttached() {
		return attached;
	}

	@Override
	public byte[] getAlternatives() {
		return NONE;
	}

}
