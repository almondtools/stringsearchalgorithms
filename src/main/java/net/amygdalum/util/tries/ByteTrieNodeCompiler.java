package net.amygdalum.util.tries;

import java.util.HashMap;
import java.util.Map;

import net.amygdalum.util.map.ByteObjectMap;
import net.amygdalum.util.map.ByteObjectMap.Entry;

public class ByteTrieNodeCompiler<T> {

	private Map<PreByteTrieNode<T>, ByteTrieNode<T>> nodes;
	private Map<ByteTrieNode<T>, PreByteTrieNode<T>> reverse;

	public ByteTrieNodeCompiler() {
		this.nodes = new HashMap<>();
		this.reverse = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public ByteTrieNode<T>[] compileAndLink(PreByteTrieNode<T>[] node) {
		ByteTrieNode<T>[] compiled = new ByteTrieNode[node.length];
		for (int i = 0; i < compiled.length; i++) {
			compiled[i] = compile(node[i]);
		}
		link();
		return compiled;
	}

	public ByteTrieNode<T> compileAndLink(PreByteTrieNode<T> node) {
		ByteTrieNode<T> compiled = compile(node);
		link();
		return compiled;
	}

	public ByteTrieNode<T> compile(PreByteTrieNode<T> node) {
		if (node == null) {
			return null;
		}
		ByteTrieNode<T> compiled = nodes.get(node);
		if (compiled == null) {
			ByteObjectMap<ByteTrieNode<T>> nexts = compile(node.getNexts());
			T attached = node.getAttached();
			compiled = createNode(nexts, attached);
			if (compiled instanceof ByteTrieLeafNode) {
				reverse.put(compiled, node);
			}
			nodes.put(node, compiled);
		}
		return compiled;
	}

	public void link() {
		for (Map.Entry<PreByteTrieNode<T>, ByteTrieNode<T>> entry : nodes.entrySet()) {
			PreByteTrieNode<T> key = entry.getKey();
			PreByteTrieNode<T> link = key.getLink();
			if (link != null) {
				ByteTrieNode<T> value = resolve(entry.getValue());
				ByteTrieNode<T> target = resolve(nodes.get(link));
				value.link(target);
			}
		}
	}

	private ByteTrieNode<T> resolve(ByteTrieNode<T> node) {
		if (node instanceof TemporaryByteTrieNode) {
			return ((TemporaryByteTrieNode<T>) node).resolve();
		} else {
			return node;
		}
	}

	public ByteObjectMap<ByteTrieNode<T>> compile(ByteObjectMap<PreByteTrieNode<T>> nexts) {
		ByteTrieNode<T> defaultValue = compile(nexts.getDefaultValue());
		ByteObjectMap<ByteTrieNode<T>> compiled = new ByteObjectMap<>(defaultValue);
		for (Entry<PreByteTrieNode<T>> entry : nexts.cursor()) {
			byte key = entry.key;
			PreByteTrieNode<T> value = entry.value;
			compiled.put(key, compile(value));
		}
		return compiled;
	}

	private ByteTrieNode<T> createNode(ByteObjectMap<ByteTrieNode<T>> nexts, T attached) {
		if (isQualifiedForLeafNode(nexts)) {
			return createTrieLeafNode(nexts, attached);
		} else if (isQualifiedForSingleNode(nexts)) {
			return createTrieSingleNode(nexts, attached);
		} else {
			return createTrieArrayNode(nexts, attached);
		}
	}

	private boolean isQualifiedForLeafNode(ByteObjectMap<ByteTrieNode<T>> nexts) {
		return nexts.size() == 0;
	}

	private ByteTrieNode<T> createTrieLeafNode(ByteObjectMap<ByteTrieNode<T>> nexts, T attached) {
		return new ByteTrieTerminalNode<T>(attached);
	}

	private boolean isQualifiedForSingleNode(ByteObjectMap<ByteTrieNode<T>> nexts) {
		if (nexts.size() == 1) {
			ByteTrieNode<T> next = nexts.cursor().iterator().next().value;
			return next instanceof ByteTrieSingleNode || next instanceof ByteTrieTerminalNode;
		} else {
			return false;
		}
	}

	private ByteTrieNode<T> createTrieSingleNode(ByteObjectMap<ByteTrieNode<T>> nexts, T attached) {
		Entry<ByteTrieNode<T>> next = nexts.cursor().iterator().next();

		byte key = next.key;
		ByteTrieNode<T> value = next.value;


		ByteTrieSingleNode<T> newNode = subsume(key, value, attached);

		PreByteTrieNode<T> toRemap = reverse.get(value);
		TemporaryByteTrieNode<T> target = new TemporaryByteTrieNode<T>(newNode, 0);
		nodes.put(toRemap, target);
		for (PreByteTrieNode<T> node : toRemap.nodes()) {
			if (node == toRemap) {
				continue;
			}
			ByteTrieNode<T> followNode = nodes.get(node);
			if (followNode instanceof TemporaryByteTrieNode) {
				((TemporaryByteTrieNode<T>) followNode).shift(newNode);
			}
		}
		return newNode;
	}

	public ByteTrieSingleNode<T> subsume(byte key, ByteTrieNode<T> value, T attached) {
		if (value instanceof ByteTrieSingleNode) {
			return new ByteTrieSingleNode<T>(key, (ByteTrieSingleNode<T>) value, attached);
		} else if (value instanceof ByteTrieTerminalNode) {
			return new ByteTrieSingleNode<T>(key, (ByteTrieTerminalNode<T>) value, attached);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private ByteTrieNode<T> createTrieArrayNode(ByteObjectMap<ByteTrieNode<T>> nexts, T attached) {
		return new ByteTrieArrayNode<T>(nexts, attached);
	}

	private static class TemporaryByteTrieNode<T> implements ByteTrieNode<T> {

		private ByteTrieSingleNode<T> node;
		private int pos;

		public TemporaryByteTrieNode(ByteTrieSingleNode<T> node, int pos) {
			this.node = node;
			this.pos = pos;
		}

		public void shift(ByteTrieSingleNode<T> node) {
			this.node = node;
			this.pos++;
		}

		public ByteTrieNode<T> resolve() {
			return node.proxy(pos);
		}

		@Override
		public ByteTrieNode<T> nextNode(byte b) {
			return resolve().nextNode(b);
		}

		@Override
		public ByteTrieNode<T> nextNode(byte[] bytes) {
			return resolve().nextNode(bytes);
		}

		@Override
		public ByteTrieNode<T> nextNode(byte[] bytes, int start) {
			return resolve().nextNode(bytes, start);
		}

		@Override
		public T getAttached() {
			return resolve().getAttached();
		}

		@Override
		public byte[] getAlternatives() {
			return resolve().getAlternatives();
		}

		@Override
		public void link(ByteTrieNode<T> node) {
			resolve().link(node);
		}

		@Override
		public ByteTrieNode<T> getLink() {
			return resolve().getLink();
		}

	}
}
