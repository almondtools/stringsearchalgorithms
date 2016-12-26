package net.amygdalum.util.tries;

import static net.amygdalum.util.tries.CharTrieArrayNode.computeArraySize;

import java.util.HashMap;
import java.util.Map;

import net.amygdalum.util.map.CharObjectMap;
import net.amygdalum.util.map.CharObjectMap.Entry;

public class CharTrieNodeCompiler<T> {

	private boolean compressed;
	private Map<PreCharTrieNode<T>, CharTrieNode<T>> nodes;
	private Map<CharTrieNode<T>, PreCharTrieNode<T>> reverse;

	public CharTrieNodeCompiler(boolean compressed) {
		this.compressed = compressed;
		this.nodes = new HashMap<>();
		this.reverse = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public CharTrieNode<T>[] compileAndLink(PreCharTrieNode<T>[] node) {
		CharTrieNode<T>[] compiled = new CharTrieNode[node.length];
		for (int i = 0; i < compiled.length; i++) {
			compiled[i] = compile(node[i]);
		}
		link();
		return compiled;
	}

	public CharTrieNode<T> compileAndLink(PreCharTrieNode<T> node) {
		CharTrieNode<T> compiled = compile(node);
		link();
		return compiled;
	}

	private CharTrieNode<T> compile(PreCharTrieNode<T> node) {
		if (node == null) {
			return null;
		}
		CharTrieNode<T> compiled = nodes.get(node);
		if (compiled == null) {
			CharObjectMap<CharTrieNode<T>> nexts = compile(node.getNexts());
			T attached = node.getAttached();
			compiled = createNode(nexts, attached);
			if (compiled instanceof CharTrieLeafNode) {
				reverse.put(compiled, node);
			}
			nodes.put(node, compiled);
		}
		return compiled;
	}

	private void link() {
		for (Map.Entry<PreCharTrieNode<T>, CharTrieNode<T>> entry : nodes.entrySet()) {
			PreCharTrieNode<T> key = entry.getKey();
			PreCharTrieNode<T> link = key.getLink();
			if (link != null) {
				CharTrieNode<T> value = resolve(entry.getValue());
				CharTrieNode<T> target = resolve(nodes.get(link));
				value.link(target);
			}
		}
	}

	private CharTrieNode<T> resolve(CharTrieNode<T> node) {
		if (node instanceof TemporaryCharTrieNode) {
			return ((TemporaryCharTrieNode<T>) node).resolve();
		} else {
			return node;
		}
	}

	private CharObjectMap<CharTrieNode<T>> compile(CharObjectMap<PreCharTrieNode<T>> nexts) {
		CharTrieNode<T> defaultValue = compile(nexts.getDefaultValue());
		CharObjectMap<CharTrieNode<T>> compiled = new CharObjectMap<>(defaultValue);
		for (Entry<PreCharTrieNode<T>> entry : nexts.cursor()) {
			char key = entry.key;
			PreCharTrieNode<T> value = entry.value;
			compiled.put(key, compile(value));
		}
		return compiled;
	}

	private CharTrieNode<T> createNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		if (isQualifiedForLeafNode(nexts)) {
			return createTrieLeafNode(nexts, attached);
		} else if (isQualifiedForSingleNode(nexts)) {
			return createTrieSingleNode(nexts, attached);
		} else if (isQualifiedForArrayNode(nexts)) {
			return createTrieArrayNode(nexts, attached);
		} else {
			return createTrieMapNode(nexts, attached);
		}
	}

	private boolean isQualifiedForLeafNode(CharObjectMap<CharTrieNode<T>> nexts) {
		return nexts.size() == 0;
	}

	private CharTrieNode<T> createTrieLeafNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		return new CharTrieTerminalNode<T>(attached);
	}

	private boolean isQualifiedForSingleNode(CharObjectMap<CharTrieNode<T>> nexts) {
		if (compressed && nexts.size() == 1) {
			CharTrieNode<T> next = nexts.cursor().iterator().next().value;
			return next instanceof CharTrieSingleNode || next instanceof CharTrieTerminalNode;
		} else {
			return false;
		}
	}

	private CharTrieNode<T> createTrieSingleNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		Entry<CharTrieNode<T>> next = nexts.cursor().iterator().next();

		char key = next.key;
		CharTrieNode<T> value = next.value;


		CharTrieSingleNode<T> newNode = subsume(key, value, attached);

		PreCharTrieNode<T> toRemap = reverse.get(value);
		TemporaryCharTrieNode<T> target = new TemporaryCharTrieNode<T>(newNode, 0);
		nodes.put(toRemap, target);
		for (PreCharTrieNode<T> node : toRemap.nodes()) {
			if (node == toRemap) {
				continue;
			}
			CharTrieNode<T> followNode = nodes.get(node);
			if (followNode instanceof TemporaryCharTrieNode) {
				((TemporaryCharTrieNode<T>) followNode).shift(newNode);
			}
		}
		return newNode;
	}

	public CharTrieSingleNode<T> subsume(char key, CharTrieNode<T> value, T attached) {
		if (value instanceof CharTrieSingleNode) {
			return new CharTrieSingleNode<T>(key, (CharTrieSingleNode<T>) value, attached);
		} else if (value instanceof CharTrieTerminalNode) {
			return new CharTrieSingleNode<T>(key, (CharTrieTerminalNode<T>) value, attached);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private boolean isQualifiedForArrayNode(CharObjectMap<CharTrieNode<T>> nexts) {
		return computeArraySize(nexts) != -1;
	}

	private CharTrieNode<T> createTrieArrayNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		return new CharTrieArrayNode<T>(nexts, attached);
	}

	private CharTrieNode<T> createTrieMapNode(CharObjectMap<CharTrieNode<T>> nexts, T attached) {
		return new CharTrieMapNode<T>(nexts, attached);
	}

	private static class TemporaryCharTrieNode<T> implements CharTrieNode<T> {

		private CharTrieSingleNode<T> node;
		private int pos;

		public TemporaryCharTrieNode(CharTrieSingleNode<T> node, int pos) {
			this.node = node;
			this.pos = pos;
		}

		public void shift(CharTrieSingleNode<T> node) {
			this.node = node;
			this.pos++;
		}

		public CharTrieNode<T> resolve() {
			return node.proxy(pos);
		}

		@Override
		public CharTrieNode<T> nextNode(char c) {
			return resolve().nextNode(c);
		}

		@Override
		public CharTrieNode<T> nextNode(char[] chars) {
			return resolve().nextNode(chars);
		}

		@Override
		public CharTrieNode<T> nextNode(char[] chars, int start) {
			return resolve().nextNode(chars, start);
		}

		@Override
		public T getAttached() {
			return resolve().getAttached();
		}

		@Override
		public char[] getAlternatives() {
			return resolve().getAlternatives();
		}

		@Override
		public void link(CharTrieNode<T> node) {
			resolve().link(node);
		}

		@Override
		public CharTrieNode<T> getLink() {
			return resolve().getLink();
		}

	}
}
