package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.AttachmentAdaptor.attach;
import static net.amygdalum.util.text.ByteFallbackAdaptor.getFallback;
import static net.amygdalum.util.text.ByteFallbackAdaptor.setFallback;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.StringUtils.toByteArray;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteAutomaton;
import net.amygdalum.util.text.ByteNode;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.ByteTask;
import net.amygdalum.util.text.ByteTrie;
import net.amygdalum.util.text.ByteWordSet;
import net.amygdalum.util.text.ByteWordSetBuilder;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayByteFallbackTrieCompiler;

/**
 * An implementation of the Aho-Corasick Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class AhoCorasick implements StringSearchAlgorithm {

	private ByteWordSet<ByteString> trie;
	private int minLength;

	public AhoCorasick(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = toByteArray(patterns, charset);
		this.trie = computeTrie(bytepatterns, charset);
		this.minLength = minLength(bytepatterns);
	}

	private static ByteWordSet<ByteString> computeTrie(List<byte[]> bytepatterns, Charset charset) {
		ByteWordSetBuilder<ByteString, ByteTrie<ByteString>> builder = new ByteWordSetBuilder<>(new DoubleArrayByteFallbackTrieCompiler<ByteString>());

		for (byte[] pattern : bytepatterns) {
			builder.extend(pattern, new ByteString(pattern, charset));
		}

		return builder
			.work(new FallbackLinks())
			.build();

	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(trie, bytes, options);
		} else {
			return new NextMatchFinder(trie, bytes, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static class FallbackLinks implements ByteTask<ByteString> {

		private ByteNode<ByteString> root;

		@Override
		public List<ByteNode<ByteString>> init(ByteNode<ByteString> root) {
			this.root = root;
			setFallback(root, null);
			return asList(root);
		}

		@Override
		public List<ByteNode<ByteString>> process(ByteNode<ByteString> node) {
			List<ByteNode<ByteString>> nexts = new ArrayList<>();
			for (byte b : node.getAlternatives()) {
				ByteNode<ByteString> next = node.nextNode(b);
				ByteNode<ByteString> down = getFallback(node);
				nextdown: while (down != null) {
					ByteNode<ByteString> nextNode = down.nextNode(b);
					if (nextNode != null) {
						setFallback(next, nextNode);
						if (next.getAttached() == null) {
							ByteString attachment = nextNode.getAttached();
							if (attachment != null) {
								attach(next, attachment);
							}
						}
						break nextdown;
					}
					down = getFallback(down);
				}
				if (down == null) {
					setFallback(next, root);
				}
				nexts.add(next);
			}
			return nexts;
		}

	}

	private static abstract class Finder extends BufferedStringFinder {

		protected ByteProvider bytes;
		protected ByteAutomaton<ByteString> cursor;

		public Finder(ByteWordSet<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.bytes = bytes;
			this.cursor = trie.cursor();
		}

		@Override
		public void skipTo(long pos) {
			if (pos > bytes.current()) {
				bytes.move(pos);
			}
			cursor.reset();
			clear();
		}

		protected List<StringMatch> createMatches(long end) {
			List<StringMatch> matches = new ArrayList<>();
			for (ByteString currentMatch : cursor) {
				long start = end - currentMatch.length();
				StringMatch nextMatch = createMatch(start, end);
				if (!matches.contains(nextMatch)) {
					matches.add(nextMatch);
				}
			}
			return matches;
		}

		private StringMatch createMatch(long start, long end) {
			ByteString s = bytes.slice(start, end);
			return new StringMatch(start, end, s.getString());
		}

	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(ByteWordSet<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(trie, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!bytes.finished()) {
				byte b = bytes.next();
				boolean success = cursor.accept(b);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(bytes.current()));
					return leftMost();
				}
			}
			return null;
		}
	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(ByteWordSet<ByteString> trie, ByteProvider bytes, StringFinderOption... options) {
			super(trie, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			while (!bytes.finished()) {
				byte b = bytes.next();
				boolean success = cursor.lookahead(b);
				if (!success && !isBufferEmpty()) {
					bytes.prev();
					break;
				}
				success = cursor.accept(b);
				if (!success) {
					cursor.reset();
				}
				if (cursor.hasAttachments()) {
					push(createMatches(bytes.current()));
				}
			}
			return longestLeftMost();
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
			return new AhoCorasick(patterns, charset);
		}

	}

}
