package net.amygdalum.util.tries;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.map.ByteObjectMap;
import net.amygdalum.util.map.ByteObjectMap.Entry;

public class PreByteTrieNode<T> {

	private ByteObjectMap<PreByteTrieNode<T>> nextMap;
	private PreByteTrieNode<T> link;
	private T attached;

	public PreByteTrieNode() {
		nextMap = new ByteObjectMap<>(null);
	}

	public void reset() {
		nextMap = new ByteObjectMap<>(null);
	}

	public void addNext(byte b, PreByteTrieNode<T> node) {
		nextMap.put(b, node);
	}

	public ByteObjectMap<PreByteTrieNode<T>> getNexts() {
		return nextMap;
	}

	public void link(PreByteTrieNode<T> link) {
		this.link = link;
	}

	public PreByteTrieNode<T> getLink() {
		return link;
	}

	public T getAttached() {
		return attached;
	}

	public void setAttached(T attached) {
		this.attached = attached;
	}

	public PreByteTrieNode<T> extend(byte[] bytes, T attach) {
		PreByteTrieNode<T> node = extend(bytes, 0);
		node.setAttached(attach);
		return node;
	}

	public PreByteTrieNode<T> extend(byte[] bytes, int i) {
		if (i >= bytes.length) {
			return this;
		}
		PreByteTrieNode<T> toExtend = findNodeToExtend(bytes[i]);
		return toExtend.extend(bytes, i + 1);
	}

	private PreByteTrieNode<T> findNodeToExtend(byte current) {
		PreByteTrieNode<T> node = nextNode(current);
		if (node == null) {
			node = new PreByteTrieNode<>();
			addNext(current, node);
		}
		return node;
	}

	public PreByteTrieNode<T> nextNode(byte b) {
		return nextMap.get(b);
	}

	public PreByteTrieNode<T> nextNode(byte[] bytes) {
		PreByteTrieNode<T> current = this;
		for (byte b : bytes) {
			current = current.nextNode(b);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	public Set<PreByteTrieNode<T>> nodes() {
		Set<PreByteTrieNode<T>> nodes = new LinkedHashSet<>();

		Queue<PreByteTrieNode<T>> worklist = new LinkedList<>();
		worklist.add(this);

		while (!worklist.isEmpty()) {
			PreByteTrieNode<T> current = worklist.remove();
			boolean added = nodes.add(current);
			if (added) {
				for (Entry<PreByteTrieNode<T>> entry : current.getNexts().cursor()) {
					PreByteTrieNode<T> node = entry.value;
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