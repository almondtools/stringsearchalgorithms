package net.amygdalum.util.tries;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.map.CharObjectMap.Entry;

public class PreCharTrieNode<T> {

	private CharObjectMap<PreCharTrieNode<T>> nextMap;
	private PreCharTrieNode<T> link;
	private T attached;

	public PreCharTrieNode() {
		nextMap = new CharObjectMap<>(null);
	}

	public void reset() {
		nextMap = new CharObjectMap<>(null);
	}

	public void addNext(char c, PreCharTrieNode<T> node) {
		nextMap.put(c, node);
	}

	public CharObjectMap<PreCharTrieNode<T>> getNexts() {
		return nextMap;
	}

	public void link(PreCharTrieNode<T> link) {
		this.link = link;
	}

	public PreCharTrieNode<T> getLink() {
		return link;
	}

	public T getAttached() {
		return attached;
	}

	public void setAttached(T attached) {
		this.attached = attached;
	}

	public PreCharTrieNode<T> extend(char[] chars, T attach) {
		PreCharTrieNode<T> node = extend(chars, 0);
		node.setAttached(attach);
		return node;
	}

	public PreCharTrieNode<T> extend(char[] chars, int i) {
		if (i >= chars.length) {
			return this;
		}
		PreCharTrieNode<T> toExtend = findNodeToExtend(chars[i]);
		return toExtend.extend(chars, i + 1);
	}

	private PreCharTrieNode<T> findNodeToExtend(char current) {
		PreCharTrieNode<T> node = nextNode(current);
		if (node == null) {
			node = new PreCharTrieNode<>();
			addNext(current, node);
		}
		return node;
	}

	public PreCharTrieNode<T> nextNode(char c) {
		return nextMap.get(c);
	}

	public PreCharTrieNode<T> nextNode(char[] chars) {
		PreCharTrieNode<T> current = this;
		for (char c : chars) {
			current = current.nextNode(c);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	public Set<PreCharTrieNode<T>> nodes() {
		Set<PreCharTrieNode<T>> nodes = new LinkedHashSet<>();

		Queue<PreCharTrieNode<T>> worklist = new LinkedList<>();
		worklist.add(this);

		while (!worklist.isEmpty()) {
			PreCharTrieNode<T> current = worklist.remove();
			boolean added = nodes.add(current);
			if (added) {
				for (Entry<PreCharTrieNode<T>> entry : current.getNexts().cursor()) {
					PreCharTrieNode<T> node = entry.value;
					worklist.add(node);
				}
			}
		}

		return nodes;
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