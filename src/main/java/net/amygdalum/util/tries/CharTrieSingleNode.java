package net.amygdalum.util.tries;

public class CharTrieSingleNode<T> extends CharTrieLeafNode<T> implements CharTrieNode<T> {

	private char[] chars;
	private T[] attached;
	private TrieProxyNode<T>[] proxies;
	private char[] alt;

	public CharTrieSingleNode(char[] chars, T attached) {
		this(chars, newAttached(attached, chars.length));
	}

	public CharTrieSingleNode(char c, CharTrieTerminalNode<T> next, T attached) {
		this(newChars(c), newAttached(attached, next.getAttached()));
	}

	public CharTrieSingleNode(char c, CharTrieSingleNode<T> next, T attached) {
		this(newChars(c, next.chars), newAttached(attached, next.attached));
	}

	public CharTrieSingleNode(char[] chars, T[] attached) {
		this.chars = chars;
		this.attached = attached;
		this.proxies = proxies(chars.length);
		this.alt = new char[] { chars[0] };
	}

	private static char[] newChars(char next) {
		return new char[] { next };
	}

	private static char[] newChars(char next, char[] follow) {
		char[] chars = new char[follow.length + 1];
		chars[0] = next;
		System.arraycopy(follow, 0, chars, 1, follow.length);
		return chars;
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
		return chars.length;
	}

	@Override
	public CharTrieNode<T> nextNode(char c) {
		if (chars[0] != c) {
			return null;
		}
		return proxy(0);
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars) {
		int numberOfChars = chars.length;
		for (int i = 0; i < numberOfChars; i++) {
			if (i >= this.chars.length) {
				return null;
			} else if (this.chars[i] != chars[i]) {
				return null;
			}
		}
		return proxy(numberOfChars - 1);
	}

	@Override
	public CharTrieNode<T> nextNode(char[] chars, int start) {
		int numberOfChars = chars.length - start;
		for (int i = 0; i < numberOfChars; i++) {
			if (i >= this.chars.length) {
				return null;
			} else if (this.chars[i] != chars[i + start]) {
				return null;
			}
		}
		return proxy(numberOfChars - 1);
	}

	public CharTrieNode<T> proxy(int i) {
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
	public char[] getAlternatives() {
		return alt;
	}

	private static class TrieProxyNode<T> extends CharTrieLeafNode<T> implements CharTrieNode<T> {

		private CharTrieSingleNode<T> base;
		private int offset;
		private char[] alt;

		public TrieProxyNode(CharTrieSingleNode<T> base, int offset) {
			this.base = base;
			this.offset = offset + 1;
			this.alt = this.offset == base.chars.length ? new char[0] : new char[] { base.chars[this.offset] };
		}

		@Override
		public int length() {
			return base.chars.length - offset;
		}

		@Override
		public CharTrieNode<T> nextNode(char c) {
			if (base.chars.length <= offset) {
				return null;
			}
			if (base.chars[offset] != c) {
				return null;
			}
			return base.proxy(offset);
		}

		@Override
		public CharTrieNode<T> nextNode(char[] chars) {
			int numberOfChars = chars.length;
			for (int i = 0; i < numberOfChars; i++) {
				int offi = offset + i;
				if (offi >= base.chars.length) {
					return null;
				} else if (base.chars[offi] != chars[i]) {
					return null;
				}
			}
			return base.proxy(offset + numberOfChars - 1);
		}

		@Override
		public CharTrieNode<T> nextNode(char[] chars, int start) {
			int numberOfChars = chars.length - start;
			for (int i = 0; i < numberOfChars; i++) {
				int offi = offset + i;
				if (offi >= base.chars.length) {
					return null;
				} else if (base.chars[offi] != chars[i + start]) {
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
		public char[] getAlternatives() {
			return alt;
		}

	}

}
