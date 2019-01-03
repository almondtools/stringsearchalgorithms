package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.asList;
import static net.amygdalum.util.text.ByteEncoding.encode;
import static net.amygdalum.util.text.ByteUtils.revert;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteAutomaton;
import net.amygdalum.util.text.ByteConnectionAdaptor;
import net.amygdalum.util.text.ByteDawg;
import net.amygdalum.util.text.ByteNode;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.ByteTask;
import net.amygdalum.util.text.ByteWordSet;
import net.amygdalum.util.text.ByteWordSetBuilder;
import net.amygdalum.util.text.linkeddawg.LinkedByteDawgCompiler;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle
 * Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which
 * can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private ByteWordSet<byte[]> trie;
	private int patternLength;

	public BOM(String pattern, Charset charset) {
		byte[] encoded = encode(pattern, charset);
		this.patternLength = encoded.length;
		this.trie = computeTrie(encoded);
	}

	private static ByteWordSet<byte[]> computeTrie(byte[] pattern) {
		ByteWordSetBuilder<byte[], ByteDawg<byte[]>> builder = new ByteWordSetBuilder<>(new LinkedByteDawgCompiler<byte[]>());

		builder.extend(revert(pattern), pattern);
		builder.work(new BuildOracle());

		return builder.build();
	}

	@Override
	public int getPatternLength() {
		return patternLength;
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		return new Finder(trie, patternLength, bytes, options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public static class BuildOracle implements ByteTask<byte[]> {
		private Map<ByteNode<byte[]>, ByteNode<byte[]>> oracle;
		private ByteNode<byte[]> init;

		public BuildOracle() {
			oracle = new IdentityHashMap<>();
		}

		@Override
		public List<ByteNode<byte[]>> init(ByteNode<byte[]> root) {
			this.init = root;
			return asList(root);
		}

		@Override
		public List<ByteNode<byte[]>> process(ByteNode<byte[]> node) {
			List<ByteNode<byte[]>> nexts = new ArrayList<>();
			for (byte b : node.getAlternatives()) {
				ByteNode<byte[]> current = node.nextNode(b);

				ByteNode<byte[]> down = oracle.get(node);
				while (down != null) {
					ByteNode<byte[]> next = down.nextNode(b);
					if (next != null) {
						oracle.put(current, next);
						break;
					}
					addNextNode(down, b, current);
					down = oracle.get(down);
				}
				if (down == null) {
					oracle.put(current, init);
				}

				nexts.add(current);
			}
			return nexts;
		}

		@SuppressWarnings("unchecked")
		private void addNextNode(ByteNode<byte[]> node, byte b, ByteNode<byte[]> next) {
			((ByteConnectionAdaptor<byte[]>) node).addNextNode(b, next);
		}

	}

	private static class Finder extends AbstractStringFinder {

		private final int lookahead;
		private ByteProvider bytes;
		private ByteAutomaton<byte[]> cursor;

		public Finder(ByteWordSet<byte[]> trie, int patternLength, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.lookahead = patternLength - 1;
			this.bytes = bytes;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished(lookahead)) {
				cursor.reset();
				int j = lookahead;
				boolean success = true;
				while (j >= 0 && success) {
					success = cursor.accept(bytes.lookahead(j));
					j--;
				}
				if (success && j < 0) {
					byte[] pattern = cursor.iterator().next();
					long start = bytes.current();
					long end = start + pattern.length;
					StringMatch match = createMatch(start, end);

					bytes.next();
					return match;
				}
				if (j <= 0) {
					bytes.next();
				} else {
					bytes.forward(j + 2);
				}
			}
			return null;
		}

		private StringMatch createMatch(long start, long end) {
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	public static class Factory implements StringSearchAlgorithmFactory {

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			return new BOM(pattern, charset);
		}

	}

}
