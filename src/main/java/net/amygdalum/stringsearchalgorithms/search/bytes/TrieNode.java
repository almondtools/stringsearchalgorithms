package net.amygdalum.stringsearchalgorithms.search.bytes;

import java.util.LinkedHashSet;
import java.util.Set;

import net.amygdalum.util.map.ByteObjectMap;

public class TrieNode<T> {

	private int min;
	private int max;
	private TrieNode<T>[] nexts;
	private ByteObjectMap<TrieNode<T>> nextMap;
	private TrieNode<T> fallback;
	private T attached;

	public TrieNode() {
		this.nexts = trieNodes(4);
		this.min = 0;
		this.max = 3;
	}

	public void reset() {
		this.nexts = trieNodes(4);
		this.min = 0;
		this.max = 3;
	}

	@SuppressWarnings("unchecked")
	private static <T> TrieNode<T>[] trieNodes(int len) {
		return new TrieNode[len];
	}

	public void addNext(byte b, TrieNode<T> node) {
		if (nexts != null) {
			if (b >= min && b <= max) {
				nexts[(int) (b - min)] = node;
				return;
			}
			int newMin = b < min ? b : min;
			int newMax = b > max ? b : max;
			int newLen = newMax - newMin + 1;
			if (newLen <= 256) {
				TrieNode<T>[] newNexts = trieNodes(newLen);
				int newOffset = min - newMin;
				for (int i = 0; i < nexts.length; i++) {
					newNexts[i + newOffset] = nexts[i];
				}
				newNexts[(int) (b - newMin)] = node;
				min = newMin;
				max = newMax;
				nexts = newNexts;
				return;
			}
		}
		if (nextMap == null) {
			nextMap = new ByteObjectMap<>(null);
			for (int i = 0; i < nexts.length; i++) {
				TrieNode<T> nodeToMap = nexts[i];
				byte byteToMap = (byte) (min + i);
				if (nodeToMap != null) {
					nextMap.put(byteToMap, nodeToMap);
				}
			}
			nexts = null;
		}
		nextMap.put(b, node);
	}

	public ByteObjectMap<TrieNode<T>> getNexts() {
		if (nextMap != null) {
			return nextMap;
		} else {
			ByteObjectMap<TrieNode<T>> map = new ByteObjectMap<>(null);
			for (int i = 0; i < nexts.length; i++) {
				TrieNode<T> nodeToMap = nexts[i];
				byte byteToMap = (byte) (min + i);
				if (nodeToMap != null) {
					map.put(byteToMap, nodeToMap);
				}
			}
			return map;
		}
	}

	public void addFallback(TrieNode<T> fallback) {
		this.fallback = fallback;
	}

	public TrieNode<T> getFallback() {
		return fallback;
	}

	public T getAttached() {
		return attached;
	}

	public void setAttached(T attached) {
		this.attached = attached;
	}

	public TrieNode<T> extend(byte[] bytes, T attach) {
		TrieNode<T> node = extend(bytes, 0);
		node.setAttached(attach);
		return node;
	}

	public TrieNode<T> extend(byte[] bytes, int i) {
		if (i >= bytes.length) {
			return this;
		}
		TrieNode<T> toExtend = findNodeToExtend(bytes[i]);
		return toExtend.extend(bytes, i + 1);
	}

	private TrieNode<T> findNodeToExtend(byte current) {
		TrieNode<T> node = nextNode(current);
		if (node == null) {
			node = new TrieNode<>();
			addNext(current, node);
		}
		return node;
	}

	public TrieNode<T> nextNode(byte b) {
		if (nextMap != null) {
			return nextMap.get(b);
		} else {
			int index = b - min;
			if (index >= nexts.length || index < 0) {
				return null;
			} else {
				return nexts[index];
			}
		}
	}

	public TrieNode<T> nextNode(byte[] bytes) {
		TrieNode<T> current = this;
		for (byte b : bytes) {
			current = current.nextNode(b);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	public Set<TrieNode<T>> nodes() {
		Set<TrieNode<T>> nodes = new LinkedHashSet<>();
		colllectNodes(nodes);
		return nodes;
	}

	private void colllectNodes(Set<TrieNode<T>> nodes) {
		if (nodes.contains(this)) {
			return;
		}
		nodes.add(this);
		for (ByteObjectMap<TrieNode<T>>.Entry entry : getNexts().cursor()) {
			TrieNode<T> node = entry.value;
			node.colllectNodes(nodes);
		}
	}

	@Override
	public String toString() {
		if (attached != null) {
			return '[' + attached.toString() + ']';
		} else {
			return "[]";
		}
	}

}