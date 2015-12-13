package com.almondtools.stringsandchars.search;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrieNode {

	private Map<Character, TrieNode> nexts;
	private TrieNode fallback;
	private String match;

	public TrieNode() {
		this.nexts = new LinkedHashMap<>();
	}
	
	public void addNext(char c, TrieNode node) {
		nexts.put(c, node);
	}
	
	public Map<Character, TrieNode> getNexts() {
		return nexts;
	}

	public void addFallback(TrieNode fallback) {
		this.fallback = fallback;
	}
	
	public TrieNode getFallback() {
		return fallback;
	}
	
	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public TrieNode extendReverse(char[] chars) {
		TrieNode node = extendReverse(chars, 0);
		node.setMatch(new String(chars));
		return node;
	}

	public TrieNode extendReverse(char[] chars, int i) {
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

	public TrieNode extend(char[] chars) {
		TrieNode node = extend(chars, 0);
		node.setMatch(new String(chars));
		return node;
	}	

	public TrieNode extend(char[] chars, int i) {
		if (i >= chars.length) {
			return this;
		}
		TrieNode toExtend = findNodeToExtend(Arrays.copyOf(chars, i + 1));
		return toExtend.extend(chars, i + 1);
	}

	private TrieNode findNodeToExtend(char[] chars) {
		char current = chars[chars.length - 1];
		TrieNode node = nexts.get(current);
		if (node == null) {
			node = new TrieNode();
			nexts.put(current, node);
		}
		return node;
	}

	public TrieNode nextNode(char c) {
		return nexts.get(c);
	}

	public TrieNode nextNode(char[] chars) {
		TrieNode current = this;
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