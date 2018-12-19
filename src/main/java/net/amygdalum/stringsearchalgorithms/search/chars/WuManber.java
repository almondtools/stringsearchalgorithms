package net.amygdalum.stringsearchalgorithms.search.chars;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;
import static net.amygdalum.util.text.CharUtils.lastIndexOf;
import static net.amygdalum.util.text.CharUtils.maxLength;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.CharUtils.revert;
import static net.amygdalum.util.text.StringUtils.toCharArray;

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
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.text.CharAutomaton;
import net.amygdalum.util.text.CharTrieBuilder;
import net.amygdalum.util.text.CharWordSet;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayCharCompactTrie;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayCharTrieBuilder;

/**
 * An implementation of the Wu-Manber Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a
 * finder which can find any of these patterns in documents.
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
	private CharWordSet<String>[] hash;

	public WuManber(Collection<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.minLength = minLength(charpatterns);
		this.maxLength = maxLength(charpatterns);
		this.block = blockSize(minLength, charpatterns);
		this.shift = computeShift(charpatterns, block, minLength);
		this.hash = computeHash(charpatterns, block);
	}

	private static int blockSize(int minLength, List<char[]> charpatterns) {
		char maxChar = computeMaxChar(charpatterns);
		char minChar = computeMinChar(charpatterns);
		int optSize = (int) Math.ceil(Math.log(2 * minLength * charpatterns.size()) / Math.log(maxChar - minChar));
		if (optSize <= 0) {
			return 1;
		} else if (optSize > minLength) {
			return minLength;
		} else {
			return optSize;
		}
	}

	private static int[] computeShift(List<char[]> patterns, int block, int minLength) {
		int[] shift = new int[SHIFT_SIZE];
		for (int i = 0; i < shift.length; i++) {
			shift[i] = minLength - block + 1;
		}
		List<char[]> patternStrings = new ArrayList<>();
		Set<char[]> blocks = new HashSet<>();
		for (char[] pattern : patterns) {
			patternStrings.add(pattern);
			for (int i = 0; i < pattern.length + 1 - block; i++) {
				blocks.add(Arrays.copyOfRange(pattern, i, i + block));
			}
		}
		for (char[] currentBlock : blocks) {
			int shiftKey = shiftHash(currentBlock);
			int shiftBy = shift[shiftKey];
			for (char[] pattern : patternStrings) {
				int rightMost = pattern.length - lastIndexOf(pattern, currentBlock) - block;
				if (rightMost >= 0 && rightMost < shiftBy) {
					shiftBy = rightMost;
				}
			}
			shift[shiftKey] = shiftBy;
		}
		return shift;
	}

	public static int shiftHash(char[] block) {
		int result = 1;
		for (char c : block) {
			result = SHIFT_SEED * result + c;
		}
		int hash = result % SHIFT_SIZE;
		if (hash < 0) {
			hash += SHIFT_SIZE;
		}
		return hash;
	}

	private static CharWordSet<String>[] computeHash(List<char[]> charpatterns, int block) {
		@SuppressWarnings("unchecked")
		CharTrieBuilder<String>[] builders = new CharTrieBuilder[HASH_SIZE];
		for (char[] pattern : charpatterns) {
			char[] lastBlock = Arrays.copyOfRange(pattern, pattern.length - block, pattern.length);
			int hashKey = hashHash(lastBlock);
			CharTrieBuilder<String> builder = builders[hashKey];
			if (builder == null) {
				builder = new DoubleArrayCharTrieBuilder<>(new DoubleArrayCharCompactTrie<String>());

				builders[hashKey] = builder;
			}
			builder.extend(revert(pattern), new String(pattern));
		}

		@SuppressWarnings("unchecked")
		CharWordSet<String>[] hash = new CharWordSet[builders.length];
		for (int i = 0; i < hash.length; i++) {
			hash[i] = builders[i] == null ? null : builders[i].build();
		}
		return hash;
	}

	public static int hashHash(char[] block) {
		int result = 1;
		for (char c : block) {
			result = HASH_SEED * result + c;
		}
		int hash = result % HASH_SIZE;
		if (hash < 0) {
			hash += HASH_SIZE;
		}
		return hash;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(minLength, maxLength, block, shift, hash, chars, options);
		} else {
			return new NextMatchFinder(minLength, maxLength, block, shift, hash, chars, options);
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
		protected CharProvider chars;
		protected CharAutomaton<String>[] hash;

		public Finder(int minLength, int maxLength, int block, int[] shift, CharWordSet<String>[] hash, CharProvider chars, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.lookahead = minLength - 1;
			this.maxLength = maxLength;
			this.block = block;
			this.shift = shift;
			this.hash = cursor(hash);
			this.chars = chars;
		}

		@SuppressWarnings("unchecked")
		private static CharAutomaton<String>[] cursor(CharWordSet<String>[] hash) {
			CharAutomaton<String>[] cursors = new CharAutomaton[hash.length];
			for (int i = 0; i < hash.length; i++) {
				CharWordSet<String> node = hash[i];
				cursors[i] = node == null ? CharAutomaton.NULL : node.cursor();
			}
			return cursors;
		}

		@Override
		public void skipTo(long pos) {
			long last = removeMatchesBefore(pos);
			if (last > chars.current()) {
				chars.move(last);
			}
		}

		protected StringMatch createMatch(long start, long end) {
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}

	}

	private static class NextMatchFinder extends Finder {

		public NextMatchFinder(int minLength, int maxLength, int block, int[] shift, CharWordSet<String>[] hash, CharProvider chars, StringFinderOption... options) {
			super(minLength, maxLength, block, shift, hash, chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			while (!chars.finished(lookahead)) {
				long pos = chars.current();
				char[] lastBlock = chars.between(pos + minLength - block, pos + minLength);
				int shiftKey = shiftHash(lastBlock);
				int shiftBy = shift[shiftKey];
				if (shiftBy == 0) {
					int hashkey = hashHash(lastBlock);
					CharAutomaton<String> cursor = hash[hashkey];
					cursor.reset();
					int patternPointer = lookahead;
					boolean success = cursor.accept(chars.lookahead(patternPointer));
					while (success) {
						if (cursor.hasAttachments()) {
							String match = cursor.iterator().next();
							long start = chars.current() + patternPointer;
							long end = chars.current() + patternPointer + match.length();
							push(createMatch(start, end));
						}
						patternPointer--;
						if (pos + patternPointer < 0) {
							break;
						}
						success = cursor.accept(chars.lookahead(patternPointer));
					}
					chars.next();
					if (!isBufferEmpty()) {
						return leftMost();
					}
				} else {
					chars.forward(shiftBy);
				}
			}
			return null;
		}

	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(int minLength, int maxLength, int block, int[] shift, CharWordSet<String>[] hash, CharProvider chars, StringFinderOption... options) {
			super(minLength, maxLength, block, shift, hash, chars, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			while (!chars.finished(lookahead)) {
				long pos = chars.current();
				char[] lastBlock = chars.between(pos + minLength - block, pos + minLength);
				int shiftKey = shiftHash(lastBlock);
				int shiftBy = shift[shiftKey];
				if (shiftBy == 0) {
					int hashkey = hashHash(lastBlock);
					CharAutomaton<String> cursor = hash[hashkey];
					cursor.reset();
					int patternPointer = lookahead;
					boolean success = cursor.accept(chars.lookahead(patternPointer));
					while (success) {
						if (cursor.hasAttachments()) {
							String match = cursor.iterator().next();
							long start = chars.current() + patternPointer;
							long end = chars.current() + patternPointer + match.length();
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
						success = cursor.accept(chars.lookahead(patternPointer));
					}
					chars.next();
					if (bufferContainsLongestMatch(lastStart)) {
						break;
					}
				} else {
					chars.forward(shiftBy);
				}
			}
			return longestLeftMost();
		}

		public boolean bufferContainsLongestMatch(long lastStart) {
			return !isBufferEmpty()
				&& chars.current() - lastStart - 1 > maxLength - minLength;
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new WuManber(patterns);
		}

	}

}
