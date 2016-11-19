package net.amygdalum.stringsearchalgorithms.search.chars;

import net.amygdalum.util.map.CharObjectMap;

public class TrieNode<T> {

	private int min;
	private int max;
	private TrieNode<T>[] nexts;
	private CharObjectMap<TrieNode<T>> nextMap;
	private TrieNode<T> fallback;
	private T attached;

	public TrieNode() {
		this.nexts = trieNodes(4);
		this.min = 0;
		this.max = 3;
	}

	@SuppressWarnings("unchecked")
	private static <T> TrieNode<T>[] trieNodes(int len) {
		return new TrieNode[len];
	}

	public void addNext(char c, TrieNode<T> node) {
		if (nexts != null) {
			if (c >= min && c <= max) {
				nexts[(int) (c - min)] = node;
				return;
			}
			int newMin = c < min ? c : min;
			int newMax = c > max ? c : max;
			int newLen = newMax - newMin + 1;
			if (newLen <= 256) {
				TrieNode<T>[] newNexts = trieNodes(newLen);
				int newOffset = min - newMin;
				for (int i = 0; i < nexts.length; i++) {
					newNexts[i + newOffset] = nexts[i];
				}
				newNexts[(int) (c - newMin)] = node;
				min = newMin;
				max = newMax;
				nexts = newNexts;
				return;
			}
		}
		if (nextMap == null) {
			nextMap = new CharObjectMap<>(null);
			for (int i = 0; i < nexts.length; i++) {
				TrieNode<T> nodeToMap = nexts[i];
				char charToMap = (char) (min + i);
				if (nodeToMap != null) {
					nextMap.put(charToMap, nodeToMap);
				}
			}
			nexts = null;
		}
		nextMap.put(c, node);
	}

	public CharObjectMap<TrieNode<T>> getNexts() {
		if (nextMap != null) {
			return nextMap;
		} else {
			CharObjectMap<TrieNode<T>> map = new CharObjectMap<>(null);
			for (int i = 0; i < nexts.length; i++) {
				TrieNode<T> nodeToMap = nexts[i];
				char charToMap = (char) (min + i);
				if (nodeToMap != null) {
					map.put(charToMap, nodeToMap);
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

	public static char[] revert(char[] chars) {
		final int ri = chars.length - 1;
		char[] reversechars = new char[chars.length];
		for (int i = 0; i < reversechars.length; i++) {
			reversechars[i] = chars[ri - i];
		}
		return reversechars;
	}

	public TrieNode<T> extend(char[] chars, T attach) {
		TrieNode<T> node = extend(chars, 0);
		node.setAttached(attach);
		return node;
	}

	public TrieNode<T> extend(char[] chars, int i) {
		if (i >= chars.length) {
			return this;
		}
		TrieNode<T> toExtend = findNodeToExtend(chars[i]);
		return toExtend.extend(chars, i + 1);
	}

	private TrieNode<T> findNodeToExtend(char current) {
		TrieNode<T> node = nextNode(current);
		if (node == null) {
			node = new TrieNode<>();
			addNext(current, node);
		}
		return node;
	}

	public TrieNode<T> nextNode(char c) {
		if (nextMap != null) {
			return nextMap.get(c);
		} else {
			int index = c - min;
			if (index >= nexts.length || index < 0) {
				return null;
			} else {
				return nexts[index];
			}
		}
	}

	public TrieNode<T> nextNode(char[] chars) {
		TrieNode<T> current = this;
		for (char c : chars) {
			current = current.nextNode(c);
			if (current == null) {
				return null;
			}
		}
		return current;
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