package com.almondtools.rexlex.stringsearch;

import java.util.ArrayList;
import java.util.List;

public class TrieRoot implements Trie {

	private List<TrieNode> nexts;
	private boolean terminal;

	public TrieRoot() {
		this.nexts = new ArrayList<TrieNode>();
		this.terminal = false;
	}
	
	@Override
	public void addNext(TrieNode node) {
		nexts.add(node);
	}
	
	@Override
	public List<? extends Trie> getNexts() {
		return nexts;
	}
	
	@Override
	public boolean isTerminal() {
		return terminal;
	}
	
	@Override
	public void setTerminal(int length) {
		this.terminal = true;
	}

	@Override
	public int length() {
		return 0;
	}
	
	public void extendReverse(char[] chars) {
		extendReverse(chars, true);
	}
	
	public void extendReverse(char[] chars, boolean terminate) {
		extend(revert(chars), terminate);
	}

	public static char[] revert(char[] chars) {
		final int ri = chars.length - 1;
		char[] reversechars = new char[chars.length];
		for (int i = 0; i < reversechars.length; i++) {
			reversechars[i] = chars[ri - i];
		}
		return reversechars;
	}

	public void extend(char[] chars) {
		extend(chars, true);
	}
	
	public void extend(char[] chars, boolean terminate) {
		boolean terminateNode = terminate && chars.length == 1;
		TrieNode toExtend = findNodeToExtend(chars[0], 1, terminateNode);
		toExtend.extend(chars, 1, terminate);
	}

	private TrieNode findNodeToExtend(char current, int length, boolean terminal) {
		for (TrieNode node : nexts) {
			if (node.c == current) {
				if (terminal) {
					node.setTerminal(length);
				}
				return node;
			}
		}
		TrieNode node = new TrieNode(current, length, terminal);
		nexts.add(node);
		return node;
	}

	@Override
	public TrieNode nextNode(char c) {
		for (TrieNode node : nexts) {
			if (node.c == c) {
				return node;
			}
		}
		return null;
	}
	
	@Override
	public Trie nextNode(char[] chars) {
		Trie current = this;
		for (char c : chars) {
			current = current.nextNode(c);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	@Override
	public <T> void apply(TrieVisitor<T> visitor, T data) {
		visitor.visitRoot(this, data);
	}

	@Override
	public String toString() {
		return "--->";
	}

}
