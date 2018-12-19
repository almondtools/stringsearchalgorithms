package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.asList;
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

import net.amygdalum.stringsearchalgorithms.search.AbstractStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteAutomaton;
import net.amygdalum.util.text.ByteConnectionAdaptor;
import net.amygdalum.util.text.ByteDawgBuilder;
import net.amygdalum.util.text.ByteNode;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.ByteTask;
import net.amygdalum.util.text.ByteWordSet;
import net.amygdalum.util.text.JoinStrategy;
import net.amygdalum.util.text.linkeddawg.ByteClassicDawgFactory;
import net.amygdalum.util.text.linkeddawg.LinkedByteDawgBuilder;

/**
 * An implementation of the Set Backward Oracle Matching Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetBackwardOracleMatching implements StringSearchAlgorithm {

	private ByteWordSet<List<byte[]>> trie;
	private int minLength;

	public SetBackwardOracleMatching(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.minLength = minLength(bytepatterns);
		this.trie = computeTrie(bytepatterns, minLength);
	}

	private static ByteWordSet<List<byte[]>> computeTrie(List<byte[]> bytepatterns, int length) {
		ByteDawgBuilder<List<byte[]>> builder = new LinkedByteDawgBuilder<>(new ByteClassicDawgFactory<List<byte[]>>(), new MergePatterns());

		for (byte[] pattern : bytepatterns) {
			byte[] prefix = copyOfRange(pattern, 0, length);
			byte[] reversePrefix = revert(prefix);
			byte[] suffix = copyOfRange(pattern, length, pattern.length);
			builder.extend(reversePrefix, asList(prefix, suffix));
		}
		builder.work(new BuildOracle());

		return builder.build();
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		return new Finder(trie, minLength, bytes, options);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public static class MergePatterns implements JoinStrategy<List<byte[]>> {

		@Override
		public List<byte[]> join(List<byte[]> existing, List<byte[]> next) {
			if (existing == null) {
				return new ArrayList<>(next);
			} else {
				existing.add(next.get(1));
				return existing;
			}
		}

	}

	public static class BuildOracle implements ByteTask<List<byte[]>> {
		private Map<ByteNode<List<byte[]>>, ByteNode<List<byte[]>>> oracle;
		private ByteNode<List<byte[]>> init;

		public BuildOracle() {
			oracle = new IdentityHashMap<>();
		}

		@Override
		public List<ByteNode<List<byte[]>>> init(ByteNode<List<byte[]>> root) {
			this.init = root;
			return asList(root);
		}

		@Override
		public List<ByteNode<List<byte[]>>> process(ByteNode<List<byte[]>> node) {
			List<ByteNode<List<byte[]>>> nexts = new ArrayList<>();
			for (byte b : node.getAlternatives()) {
				ByteNode<List<byte[]>> current = node.nextNode(b);

				ByteNode<List<byte[]>> down = oracle.get(node);
				while (down != null) {
					ByteNode<List<byte[]>> next = down.nextNode(b);
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
		private void addNextNode(ByteNode<List<byte[]>> node, byte b, ByteNode<List<byte[]>> next) {
			((ByteConnectionAdaptor<List<byte[]>>) node).addNextNode(b, next);
		}
	}

	private static class Finder extends AbstractStringFinder {

		private final int minLength;
		private final int lookahead;
		private ByteProvider bytes;
		private ByteAutomaton<List<byte[]>> cursor;
		private Queue<StringMatch> buffer;

		public Finder(ByteWordSet<List<byte[]>> trie, int minLength, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.lookahead = minLength - 1;
			this.bytes = bytes;
			this.cursor = trie.cursor();
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
			next: while (!bytes.finished(lookahead)) {
				cursor.reset();
				int j = lookahead;
				boolean success = true;
				while (j >= 0 && success) {
					success = cursor.accept(bytes.lookahead(j));
					j--;
				}
				long currentWindowStart = bytes.current();
				long currentPos = currentWindowStart + j + 1;
				long currentWindowEnd = currentWindowStart + minLength;
				byte[] matchedPrefix = bytes.between(currentPos, currentWindowEnd);
				if (success && j < 0) {
					List<byte[]> patterns = cursor.iterator().next();
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
