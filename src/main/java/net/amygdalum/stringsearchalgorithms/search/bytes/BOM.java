package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.util.text.ByteEncoding.encode;
import static net.amygdalum.util.text.ByteUtils.revert;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.map.ByteObjectMap.Entry;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.tries.ByteTrie;
import net.amygdalum.util.tries.ByteTrieCursor;
import net.amygdalum.util.tries.ByteTrieTreeCompiler;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the String Search Algorithm BOM (Backward Oracle Matching).
 * 
 * This algorithm takes a single pattern as input and generates a finder which can find this pattern in documents
 */
public class BOM implements StringSearchAlgorithm {

	private ByteTrie<byte[]> trie;
	private int patternLength;

	public BOM(String pattern, Charset charset) {
		byte[] encoded = encode(pattern, charset);
		this.patternLength = encoded.length;
		this.trie = computeTrie(encoded);
	}

	private static ByteTrie<byte[]> computeTrie(byte[] pattern) {
		PreByteTrieNode<byte[]> trie = new PreByteTrieNode<>();
		PreByteTrieNode<byte[]> node = trie.extend(revert(pattern), 0);
		node.setAttached(pattern);
		computeOracle(trie);
		return new ByteTrieTreeCompiler<byte[]>(false)
			.compileAndLink(trie);
	}

	private static void computeOracle(PreByteTrieNode<byte[]> trie) {
		Map<PreByteTrieNode<byte[]>, PreByteTrieNode<byte[]>> oracle = new IdentityHashMap<>();
		PreByteTrieNode<byte[]> init = trie;
		oracle.put(init, null);
		Queue<PreByteTrieNode<byte[]>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreByteTrieNode<byte[]> current = worklist.remove();
			List<PreByteTrieNode<byte[]>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<PreByteTrieNode<byte[]>> process(PreByteTrieNode<byte[]> parent, Map<PreByteTrieNode<byte[]>, PreByteTrieNode<byte[]>> oracle, PreByteTrieNode<byte[]> init) {
		List<PreByteTrieNode<byte[]>> nexts = new ArrayList<>();
		for (Entry<PreByteTrieNode<byte[]>> entry : parent.getNexts().cursor()) {
			byte b = entry.key;
			PreByteTrieNode<byte[]> trie = entry.value;

			PreByteTrieNode<byte[]> down = oracle.get(parent);
			while (down != null && down.nextNode(b) == null) {
				down.addNext(b, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				PreByteTrieNode<byte[]> next = down.nextNode(b);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}

			nexts.add(trie);
		}
		return nexts;
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

	private static class Finder extends AbstractStringFinder {

		private final int lookahead;
		private ByteProvider bytes;
		private ByteTrieCursor<byte[]> cursor;

		public Finder(ByteTrie<byte[]> trie, int patternLength, ByteProvider bytes, StringFinderOption... options) {
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
