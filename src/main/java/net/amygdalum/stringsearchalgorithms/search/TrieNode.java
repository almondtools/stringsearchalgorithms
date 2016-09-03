package net.amygdalum.stringsearchalgorithms.search;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrieNode<T> {

	private Map<Character, TrieNode<T>> nexts;
	private TrieNode<T> fallback;
	private String match;
	private T attached;

	public TrieNode() {
		this.nexts = new LinkedHashMap<>();
	}

	public void addNext(char c, TrieNode<T> node) {
		nexts.put(c, node);
	}

	public Map<Character, TrieNode<T>> getNexts() {
		return nexts;
	}

	public void addFallback(TrieNode<T> fallback) {
		this.fallback = fallback;
	}

	public TrieNode<T> getFallback() {
		return fallback;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public T getAttached() {
		return attached;
	}

	public void setAttached(T attached) {
		this.attached = attached;
	}

	public TrieNode<T> extendReverse(char[] chars) {
		TrieNode<T> node = extendReverse(chars, 0);
		node.setMatch(new String(chars));
		return node;
	}

	public TrieNode<T> extendReverse(char[] chars, int i) {
		return extend(revert(chars), i);
	}

	public static char[] revert(char[] chars) {
		final int ri = chars.length - 1;
		char[] reversechars = new char[chars.length];
		for (int i = 0; i < reversechars.length; i++) {
			reversechars[i] = chars[ri - i];
		}
		return reversechars;
	}

	public TrieNode<T> extend(char[] chars) {
		TrieNode<T> node = extend(chars, 0);
		node.setMatch(new String(chars));
		return node;
	}

	public TrieNode<T> extend(char[] chars, int i) {
		if (i >= chars.length) {
			return this;
		}
		TrieNode<T> toExtend = findNodeToExtend(Arrays.copyOf(chars, i + 1));
		return toExtend.extend(chars, i + 1);
	}

	private TrieNode<T> findNodeToExtend(char[] chars) {
		char current = chars[chars.length - 1];
		TrieNode<T> node = nexts.get(current);
		if (node == null) {
			node = new TrieNode<>();
			nexts.put(current, node);
		}
		return node;
	}

	public TrieNode<T> nextNode(char c) {
		return nexts.get(c);
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
		if (match != null) {
			return '[' + match + ']';
		} else {
			return "[]";
		}
	}

}