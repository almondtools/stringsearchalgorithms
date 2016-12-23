package net.amygdalum.util.tries;

import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.map.CharObjectMap.Entry;

public class CharTrieArrayNode<T> extends CharTrieInnerNode<T> implements CharTrieNode<T> {

	private int size;
	private int mask;
	private char[] chars;
	private CharTrieNode<T>[] nodes;
	private char[] alts;

	public CharTrieArrayNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		super(attached);
		this.size = computeArraySize(nexts);
		this.mask = size - 1;
		this.chars = chars(nexts, size, mask);
		this.nodes = nodes(nexts, size, mask);
		this.alts = alts(nexts);
	}

	public static <T> int computeArraySize(CharObjectMap<CharTrieNode<T>> nexts) {
		int nextSize = nexts.size();
		int minimumSize = 1;
		while (minimumSize < nextSize) {
			minimumSize <<= 1;
		}
		nextMask: for (int size = minimumSize; size < 256; size <<= 1) {
			boolean[] collision = new boolean[size];
			int mask = size - 1;
			for (Entry<CharTrieNode<T>> entry : nexts.cursor()) {
				int index = ((int) entry.key) & mask;
				if (collision[index]) {
					continue nextMask;
				} else {
					collision[index] = true;
				}
			}
			return size;
		}
		return -1;
	}

	private static <T> char[] chars(CharObjectMap<CharTrieNode<T>> nexts, int size, int mask) {
		char[] chars = new char[size];
		for (Entry<CharTrieNode<T>> entry : nexts.cursor()) {
			int index = ((int) entry.key) & mask;
			chars[index] = entry.key;
		}
		return chars;
	}

	@SuppressWarnings("unchecked")
	private static <T> CharTrieNode<T>[] nodes(CharObjectMap<CharTrieNode<T>> nexts, int size, int mask) {
		CharTrieNode<T>[] nodes = new CharTrieNode[size];
		for (Entry<CharTrieNode<T>> entry : nexts.cursor()) {
			int index = ((int) entry.key) & mask;
			nodes[index] = entry.value;
		}
		return nodes;
	}

	private static <T> char[] alts(CharObjectMap<CharTrieNode<T>> nexts) {
		char[] chars = new char[nexts.size()];
		int i = 0;
		for (Entry<CharTrieNode<T>> entry : nexts.cursor()) {
			chars[i] = entry.key;
			i++;
		}
		return chars;
	}

	@Override
	public CharTrieNode<T> nextNode(char c) {
		if (nodes.length == 0) {
			return null;
		}
		int index = ((int) c) & mask;
		if (chars[index] != c) {
			return null;
		} else {
			return nodes[index];
		}
	}

	@Override
	public char[] getAlternatives() {
		return alts;
	}

}
