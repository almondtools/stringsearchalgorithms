package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.List;

public class TrieNode implements Trie {
	public char c;
	private List<TrieNode> nexts;
	private boolean terminal;
	private int length;

	public TrieNode(char c, int length, boolean terminal) {
		this.c = c;
		this.nexts = new ArrayList<TrieNode>();
		this.length = terminal ? length : -1;
		this.terminal = terminal;
	}
	
	@Override
	public int length() {
		return length;
	}
	
	public char getChar() {
		return c;
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
		this.length = length;
		this.terminal = true;
	}

	public void extend(char[] chars, int i) {
		extend(chars, i, true);
	}
	
	public void extend(char[] chars, int i, boolean terminate) {
		if (i >= chars.length) {
			return;
		}
		boolean terminateNode = terminate && i == chars.length - 1;
		TrieNode toExtend = findNodeToExtend(chars[i], i + 1, terminateNode);
		toExtend.extend(chars, i + 1, terminate);
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
		visitor.visitNode(this, data);
	}
	
	@Override
	public String toString() {
		return "-" + c + "->";
	}

}