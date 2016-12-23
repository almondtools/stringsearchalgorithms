package net.amygdalum.util.tries;

import net.amygdalum.util.map.ByteObjectMap;
import net.amygdalum.util.map.ByteObjectMap.Entry;

public class ByteTrieArrayNode<T> extends ByteTrieInnerNode<T> implements ByteTrieNode<T> {

	private int size;
	private int mask;
	private byte[] bytes;
	private ByteTrieNode<T>[] nodes;
	private byte[] alts;

	public ByteTrieArrayNode(ByteObjectMap<ByteTrieNode<T>> nexts, T attached) {
		super(attached);
		this.size = computeArraySize(nexts);
		this.mask = size - 1;
		this.bytes = bytes(nexts, size, mask);
		this.nodes = nodes(nexts, size, mask);
		this.alts = alts(nexts);
	}

	public static <T> int computeArraySize(ByteObjectMap<ByteTrieNode<T>> nexts) {
		int nextSize = nexts.size();
		int minimumSize = 1;
		while (minimumSize < nextSize) {
			minimumSize <<= 1;
		}
		nextMask: for (int size = minimumSize; size < 256; size <<= 1) {
			boolean[] collision = new boolean[size];
			int mask = size - 1;
			for (Entry<ByteTrieNode<T>> entry : nexts.cursor()) {
				int index = ((int) entry.key) & mask;
				if (collision[index]) {
					continue nextMask;
				} else {
					collision[index] = true;
				}
			}
			return size;
		}
		return Integer.MAX_VALUE;
	}

	private static <T> byte[] bytes(ByteObjectMap<ByteTrieNode<T>> nexts, int size, int mask) {
		byte[] chars = new byte[size];
		for (Entry<ByteTrieNode<T>> entry : nexts.cursor()) {
			int index = ((int) entry.key) & mask;
			chars[index] = entry.key;
		}
		return chars;
	}

	@SuppressWarnings("unchecked")
	private static <T> ByteTrieNode<T>[] nodes(ByteObjectMap<ByteTrieNode<T>> nexts, int size, int mask) {
		ByteTrieNode<T>[] nodes = new ByteTrieNode[size];
		for (Entry<ByteTrieNode<T>> entry : nexts.cursor()) {
			int index = ((int) entry.key) & mask;
			nodes[index] = entry.value;
		}
		return nodes;
	}

	private static <T> byte[] alts(ByteObjectMap<ByteTrieNode<T>> nexts) {
		byte[] bytes = new byte[nexts.size()];
		int i = 0;
		for (Entry<ByteTrieNode<T>> entry : nexts.cursor()) {
			bytes[i] = entry.key;
			i++;
		}
		return bytes;
	}

	@Override
	public ByteTrieNode<T> nextNode(byte b) {
		if (nodes.length == 0) {
			return null;
		}
		int index = ((int) b) & mask;
		if (bytes[index] != b) {
			return null;
		} else {
			return nodes[index];
		}
	}

	@Override
	public byte[] getAlternatives() {
		return alts;
	}

}
