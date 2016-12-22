package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.copyOfRange;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.ByteUtils.revert;
import static net.amygdalum.util.text.StringUtils.toByteArray;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.amygdalum.stringsearchalgorithms.io.ByteProvider;
import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.map.ByteObjectMap.Entry;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.tries.ByteTrieNode;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private ByteTrieNode<List<byte[]>> trie;
	private int minLength;

	public SetBackwardOracleMatching(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.minLength = minLength(bytepatterns);
		this.trie = computeTrie(bytepatterns, minLength);
	}

	private static ByteTrieNode<List<byte[]>> computeTrie(List<byte[]> bytepatterns, int length) {
		PreByteTrieNode<List<byte[]>> trie = new PreByteTrieNode<>();
		for (byte[] pattern : bytepatterns) {
			byte[] prefix = copyOfRange(pattern, 0, length);
			trie.extend(revert(prefix), 0);
		}
		computeOracle(trie);
		computeTerminals(trie, bytepatterns, length);
		return trie.compile();
	}

	private static void computeOracle(PreByteTrieNode<List<byte[]>> trie) {
		Map<PreByteTrieNode<List<byte[]>>, PreByteTrieNode<List<byte[]>>> oracle = new IdentityHashMap<>();
		PreByteTrieNode<List<byte[]>> init = trie;
		oracle.put(init, null);
		Queue<PreByteTrieNode<List<byte[]>>> worklist = new LinkedList<>();
		worklist.add(trie);
		while (!worklist.isEmpty()) {
			PreByteTrieNode<List<byte[]>> current = worklist.remove();
			List<PreByteTrieNode<List<byte[]>>> nexts = process(current, oracle, init);
			worklist.addAll(nexts);
		}
	}

	private static List<PreByteTrieNode<List<byte[]>>> process(PreByteTrieNode<List<byte[]>> parent, Map<PreByteTrieNode<List<byte[]>>, PreByteTrieNode<List<byte[]>>> oracle, PreByteTrieNode<List<byte[]>> init) {
		List<PreByteTrieNode<List<byte[]>>> nexts = new ArrayList<>();
		for (Entry<PreByteTrieNode<List<byte[]>>> entry : parent.getNexts().cursor()) {
			byte c = entry.key;
			PreByteTrieNode<List<byte[]>> trie = entry.value;

			PreByteTrieNode<List<byte[]>> down = oracle.get(parent);
			while (down != null && down.nextNode(c) == null) {
				down.addNext(c, trie);
				down = oracle.get(down);
			}
			if (down != null) {
				PreByteTrieNode<List<byte[]>> next = down.nextNode(c);
				oracle.put(trie, next);
			} else {
				oracle.put(trie, init);
			}

			nexts.add(trie);
		}
		return nexts;
	}

	private static void computeTerminals(PreByteTrieNode<List<byte[]>> trie, List<byte[]> patterns, int minLength) {
		for (byte[] pattern : patterns) {
			byte[] prefix = Arrays.copyOfRange(pattern, 0, minLength);
			PreByteTrieNode<List<byte[]>> terminal = trie.nextNode(revert(prefix));
			List<byte[]> terminalPatterns = terminal.getAttached();
			if (terminalPatterns == null) {
				terminalPatterns = new ArrayList<>();
				terminal.setAttached(terminalPatterns);
				terminalPatterns.add(prefix);
			}
			byte[] tail = Arrays.copyOfRange(pattern, minLength, pattern.length);
			terminalPatterns.add(tail);
		}
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		return new Finder(bytes, options);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private class Finder extends AbstractStringFinder {

		private ByteProvider bytes;
		private Queue<StringMatch> buffer;

		public Finder(ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
			this.buffer = new LinkedList<>();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			buffer.clear();
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove();
			}
			final int lookahead = minLength - 1;
			next: while (!bytes.finished(lookahead)) {
				ByteTrieNode<List<byte[]>> current = trie;
				int j = lookahead;
				while (j >= 0 && current != null) {
					current = current.nextNode(bytes.lookahead(j));
					j--;
				}
				long currentWindowStart = bytes.current();
				long currentPos = currentWindowStart + j + 1;
				long currentWindowEnd = currentWindowStart + minLength;
				byte[] matchedPrefix = bytes.between(currentPos, currentWindowEnd);
				if (current != null && j < 0) {
					List<byte[]> patterns = current.getAttached();
					Iterator<byte[]> iPatterns = patterns.iterator();
					byte[] prefix = iPatterns.next();
					if (Arrays.equals(prefix, matchedPrefix)) {
						while (iPatterns.hasNext()) {
							byte[] suffix = iPatterns.next();
							long currentWordEnd = currentWindowEnd + suffix.length;
							if (!bytes.finished((int) (currentWordEnd - currentWindowStart - 1))) {
								byte[] matchedSuffix = bytes.between(currentWindowEnd, currentWordEnd);
								if (Arrays.equals(suffix, matchedSuffix)) {
									buffer.add(createMatch(currentWindowStart, currentWordEnd));
								}
							}
						}
						bytes.next();
						if (buffer.isEmpty()) {
							continue next;
						} else {
							return buffer.remove();
						}
					}

				}
				if (j <= 0) {
					bytes.next();
				} else {
					bytes.forward(j + 1);
				}
			}
			return null;
		}

		private StringMatch createMatch(long start, long end) {
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		private Charset charset;

		public Factory() {
			this(UTF_16LE);
		}

		public Factory(Charset charset) {
			this.charset = charset;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new SetBackwardOracleMatching(patterns, charset);
		}

	}

}
