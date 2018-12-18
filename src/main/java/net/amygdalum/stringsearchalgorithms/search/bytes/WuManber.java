package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.ByteUtils.lastIndexOf;
import static net.amygdalum.util.text.ByteUtils.maxLength;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.ByteUtils.revert;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.text.ByteString;
import net.amygdalum.util.text.StringUtils;
import net.amygdalum.util.tries.ByteTrie;
import net.amygdalum.util.tries.ByteTrieCursor;
import net.amygdalum.util.tries.ByteTrieTreeCompiler;
import net.amygdalum.util.tries.PreByteTrieNode;

/**
 * An implementation of the Wu-Manber Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class WuManber implements StringSearchAlgorithm {

	private static final int SHIFT_SEED = 17;
	private static final int HASH_SEED = 23;
	private static final int SHIFT_SIZE = 255;
	private static final int HASH_SIZE = 127;

	private int minLength;
	private int maxLength;
	private int block;
	private int[] shift;
	private ByteTrie<ByteString>[] hash;

	public WuManber(Collection<String> patterns, Charset charset) {
		List<byte[]> bytepatterns = StringUtils.toByteArray(patterns, charset);
		this.minLength = minLength(bytepatterns);
		this.maxLength = maxLength(bytepatterns);
		this.block = blockSize(minLength, bytepatterns.size());
		this.shift = computeShift(bytepatterns, block, minLength);
		this.hash = computeHash(bytepatterns, block, charset);
	}

	private static int blockSize(int minLength, int patterns) {
		int optSize = (int) Math.ceil(Math.log(2 * minLength * patterns) / Math.log(256));
		if (optSize <= 0) {
			return 1;
		} else if (optSize > minLength) {
			return minLength;
		} else {
			return optSize;
		}
	}

	private static int[] computeShift(List<byte[]> patterns, int block, int minLength) {
		int[] shift = new int[SHIFT_SIZE];
		for (int i = 0; i < shift.length; i++) {
			shift[i] = minLength - block + 1;
		}
		List<byte[]> patternStrings = new ArrayList<>();
		Set<byte[]> blocks = new HashSet<>();
		for (byte[] pattern : patterns) {
			patternStrings.add(pattern);
			for (int i = 0; i < pattern.length + 1 - block; i++) {
				blocks.add(Arrays.copyOfRange(pattern, i, i + block));
			}
		}
		for (byte[] currentBlock : blocks) {
			int shiftKey = shiftHash(currentBlock);
			int shiftBy = shift[shiftKey];
			for (byte[] pattern : patternStrings) {
				int rightMost = pattern.length - lastIndexOf(pattern, currentBlock) - block;
				if (rightMost >= 0 && rightMost < shiftBy) {
					shiftBy = rightMost;
				}
			}
			shift[shiftKey] = shiftBy;
		}
		return shift;
	}

	public static int shiftHash(byte[] block) {
		int result = 1;
		for (byte b : block) {
			result = SHIFT_SEED * result + b;
		}
		int hash = result % SHIFT_SIZE;
		if (hash < 0) {
			hash += SHIFT_SIZE;
		}
		return hash;
	}

	@SuppressWarnings("unchecked")
	private static ByteTrie<ByteString>[] computeHash(List<byte[]> bytepatterns, int block, Charset charset) {
		PreByteTrieNode<ByteString>[] hash = new PreByteTrieNode[HASH_SIZE];
		for (byte[] pattern : bytepatterns) {
			byte[] lastBlock = Arrays.copyOfRange(pattern, pattern.length - block, pattern.length);
			int hashKey = hashHash(lastBlock);
			PreByteTrieNode<ByteString> trie = hash[hashKey];
			if (trie == null) {
				trie = new PreByteTrieNode<>();
				hash[hashKey] = trie;
			}
			PreByteTrieNode<ByteString> node = trie.extend(revert(pattern), 0);
			node.setAttached(new ByteString(pattern, charset));
		}
		return new ByteTrieTreeCompiler<ByteString>(false)
			.compileAndLink(hash);
	}

	public static int hashHash(byte[] block) {
		int result = 1;
		for (byte b : block) {
			result = HASH_SEED * result + b;
		}
		int hash = result % HASH_SIZE;
		if (hash < 0) {
			hash += HASH_SIZE;
		}
		return hash;
	}

	@Override
	public StringFinder createFinder(ByteProvider bytes, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(minLength, maxLength, block, shift, hash, bytes, options);
		} else {
			return new NextMatchFinder(minLength, maxLength, block, shift, hash, bytes, options);
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

	private static abstract class Finder extends BufferedStringFinder {

		protected final int minLength;
		protected final int lookahead;
		protected final int maxLength;
		protected final int block;
		protected final int[] shift;
		protected ByteProvider bytes;
		protected ByteTrieCursor<ByteString>[] hash;

		public Finder(int minLength, int maxLength, int block, int[] shift, ByteTrie<ByteString>[] hash, ByteProvider bytes, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.lookahead = minLength - 1;
			this.maxLength = maxLength;
			this.block = block;
			this.shift = shift;
			this.hash = cursor(hash);
			this.bytes = bytes;
		}

		@SuppressWarnings("unchecked")
		private static ByteTrieCursor<ByteString>[] cursor(ByteTrie<ByteString>[] hash) {
			ByteTrieCursor<ByteString>[] cursors = new ByteTrieCursor[hash.length];
			for (int i = 0; i < hash.length; i++) {
				ByteTrie<ByteString> node = hash[i];
				cursors[i] = node == null ? ByteTrieCursor.NULL : node.cursor();
			}
			return cursors;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			if (last > bytes.current()) {
				bytes.move(last);
			}
		}

		protected StringMatch createMatch(long start, long end) {
			ByteString slice = bytes.slice(start, end);
			return new StringMatch(start, end, slice.getString());
		}

	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(int minLength, int maxLength, int block, int[] shift, ByteTrie<ByteString>[] hash, ByteProvider bytes, StringFinderOption... options) {
			super(minLength, maxLength, block, shift, hash, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!bytes.finished(lookahead)) {
				long pos = bytes.current();
				byte[] lastBlock = bytes.between(pos + minLength - block, pos + minLength);
				int shiftKey = shiftHash(lastBlock);
				int shiftBy = shift[shiftKey];
				if (shiftBy == 0) {
					int hashkey = hashHash(lastBlock);
					ByteTrieCursor<ByteString> cursor = hash[hashkey];
					cursor.reset();
					int patternPointer = lookahead;
					boolean success = cursor.accept(bytes.lookahead(patternPointer));
					while (success) {
						if (cursor.hasAttachments()) {
							ByteString match = cursor.iterator().next();
							long start = bytes.current() + patternPointer;
							long end = bytes.current() + patternPointer + match.length();
							push(createMatch(start, end));
						}
						patternPointer--;
						if (pos + patternPointer < 0) {
							break;
						}
						success = cursor.accept(bytes.lookahead(patternPointer));
					}
					bytes.next();
					if (!isBufferEmpty()) {
						return leftMost();
					}
				} else {
					bytes.forward(shiftBy);
				}
			}
			return null;
		}

	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(int minLength, int maxLength, int block, int[] shift, ByteTrie<ByteString>[] hash, ByteProvider bytes, StringFinderOption... options) {
			super(minLength, maxLength, block, shift, hash, bytes, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			while (!bytes.finished(lookahead)) {
				long pos = bytes.current();
				byte[] lastBlock = bytes.between(pos + minLength - block, pos + minLength);
				int shiftKey = shiftHash(lastBlock);
				int shiftBy = shift[shiftKey];
				if (shiftBy == 0) {
					int hashkey = hashHash(lastBlock);
					ByteTrieCursor<ByteString> cursor = hash[hashkey];
					cursor.reset();
					int patternPointer = lookahead;
					boolean success = cursor.accept(bytes.lookahead(patternPointer));
					while (success) {
						if (cursor.hasAttachments()) {
							ByteString match = cursor.iterator().next();
							long start = bytes.current() + patternPointer;
							long end = bytes.current() + patternPointer + match.length();
							StringMatch stringMatch = createMatch(start, end);
							if (lastStart < 0) {
								lastStart = start;
							}
							push(stringMatch);
						}
						patternPointer--;
						if (pos + patternPointer < 0) {
							break;
						}
						success = cursor.accept(bytes.lookahead(patternPointer));
					}
					bytes.next();
					if (bufferContainsLongestMatch(lastStart)) {
						break;
					}
				} else {
					bytes.forward(shiftBy);
				}
			}
			return longestLeftMost();
		}

		public boolean bufferContainsLongestMatch(long lastStart) {
			return !isBufferEmpty()
				&& bytes.current() - lastStart - 1 > maxLength - minLength;
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
			return new WuManber(patterns, charset);
		}

	}

}
