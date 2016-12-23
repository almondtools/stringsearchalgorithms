package net.amygdalum.util.tries;

public class ByteTrieSingleNode<T> extends ByteTrieLeafNode<T> implements ByteTrieNode<T> {

	private byte[] bytes;
	private T[] attached;
	private TrieProxyNode<T>[] proxies;
	private byte[] alt;

	public ByteTrieSingleNode(byte[] bytes, T attached) {
		this(bytes, newAttached(attached, bytes.length));
	}

	public ByteTrieSingleNode(byte b, ByteTrieTerminalNode<T> next, T attached) {
		this(newBytes(b), newAttached(attached, next.getAttached()));
	}

	public ByteTrieSingleNode(byte b, ByteTrieSingleNode<T> next, T attached) {
		this(newBytes(b, next.bytes), newAttached(attached, next.attached));
	}

	public ByteTrieSingleNode(byte[] bytes, T[] attached) {
		this.bytes = bytes;
		this.attached = attached;
		this.proxies = proxies(bytes.length);
		this.alt = new byte[] { bytes[0] };
	}

	private static byte[] newBytes(byte next) {
		return new byte[] { next };
	}

	private static byte[] newBytes(byte next, byte[] follow) {
		byte[] bytes = new byte[follow.length + 1];
		bytes[0] = next;
		System.arraycopy(follow, 0, bytes, 1, follow.length);
		return bytes;
	}

	@SuppressWarnings("unchecked")
	private static <T> TrieProxyNode<T>[] proxies(int length) {
		return new TrieProxyNode[length];
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newAttached(T next, T follow) {
		T[] attached = (T[]) new Object[2];
		attached[0] = next;
		attached[1] = follow;
		return attached;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newAttached(T next, T[] follow) {
		T[] attached = (T[]) new Object[follow.length + 1];
		attached[0] = next;
		System.arraycopy(follow, 0, attached, 1, follow.length);
		return attached;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newAttached(T last, int size) {
		T[] attached = (T[]) new Object[size + 1];
		attached[size] = last;
		return attached;
	}

	@Override
	public int length() {
		return bytes.length;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte b) {
		if (bytes[0] != b) {
			return null;
		}
		return proxy(0);
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes) {
		int numberOfChars = bytes.length;
		for (int i = 0; i < numberOfChars; i++) {
			if (i >= this.bytes.length) {
				return null;
			} else if (this.bytes[i] != bytes[i]) {
				return null;
			}
		}
		return proxy(numberOfChars - 1);
	}

	@Override
	public ByteTrieNode<T> nextNode(byte[] bytes, int start) {
		int numberOfBytes = bytes.length - start;
		for (int i = 0; i < numberOfBytes; i++) {
			if (i >= this.bytes.length) {
				return null;
			} else if (this.bytes[i] != bytes[i + start]) {
				return null;
			}
		}
		return proxy(numberOfBytes - 1);
	}

	public ByteTrieNode<T> proxy(int i) {
		if (proxies[i] == null) {
			proxies[i] = new TrieProxyNode<T>(this, i);
		}
		return proxies[i];
	}

	@Override
	public T getAttached() {
		return attached[0];
	}

	@Override
	public byte[] getAlternatives() {
		return alt;
	}

	private static class TrieProxyNode<T> extends ByteTrieLeafNode<T> implements ByteTrieNode<T> {

		private ByteTrieSingleNode<T> base;
		private int offset;
		private byte[] alt;

		public TrieProxyNode(ByteTrieSingleNode<T> base, int offset) {
			this.base = base;
			this.offset = offset + 1;
			this.alt = this.offset == base.bytes.length ? new byte[0] : new byte[] { base.bytes[this.offset] };
		}

		@Override
		public int length() {
			return base.bytes.length - offset;
		}

		@Override
		public ByteTrieNode<T> nextNode(byte b) {
			if (base.bytes.length <= offset) {
				return null;
			}
			if (base.bytes[offset] != b) {
				return null;
			}
			return base.proxy(offset);
		}

		@Override
		public ByteTrieNode<T> nextNode(byte[] bytes) {
			int numberOfChars = bytes.length;
			for (int i = 0; i < numberOfChars; i++) {
				int offi = offset + i;
				if (offi >= base.bytes.length) {
					return null;
				} else if (base.bytes[offi] != bytes[i]) {
					return null;
				}
			}
			return base.proxy(offset + numberOfChars - 1);
		}

		@Override
		public ByteTrieNode<T> nextNode(byte[] bytes, int start) {
			int numberOfChars = bytes.length - start;
			for (int i = 0; i < numberOfChars; i++) {
				int offi = offset + i;
				if (offi >= base.bytes.length) {
					return null;
				} else if (base.bytes[offi] != bytes[i + start]) {
					return null;
				}
			}
			return base.proxy(offset + numberOfChars - 1);
		}

		@Override
		public T getAttached() {
			return base.attached[offset];
		}

		@Override
		public byte[] getAlternatives() {
			return alt;
		}

	}

}
