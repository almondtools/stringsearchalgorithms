package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.almondtools.stringsandchars.io.CharProvider;

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

	private char minChar;
	private char maxChar;
	private int minLength;
	private int block;
	private int[] shift;
	private TrieNode<Void>[] hash;

	public WuManber(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.maxChar = computeMaxChar(charpatterns);
		this.minChar = computeMinChar(charpatterns);
		this.minLength = minLength(charpatterns);
		this.block = blockSize(minLength, minChar, maxChar, charpatterns.size());
		this.shift = computeShift(charpatterns, block, minLength);
		this.hash = computeHash(charpatterns, block);
	}

	private int minLength(List<char[]> patterns) {
		int len = Integer.MAX_VALUE;
		for (char[] pattern : patterns) {
			if (pattern.length < len) {
				len = pattern.length;
			}
		}
		return len;
	}

	private static char computeMinChar(List<char[]> patterns) {
		char min = Character.MAX_VALUE;
		for (char[] pattern : patterns) {
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i] < min) {
					min = pattern[i];
				}
			}
		}
		return min;
	}

	private static char computeMaxChar(List<char[]> patterns) {
		char max = Character.MIN_VALUE;
		for (char[] pattern : patterns) {
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i] > max) {
					max = pattern[i];
				}
			}
		}
		return max;
	}

	private List<char[]> toCharArray(List<String> patterns) {
		List<char[]> charpatterns = new ArrayList<char[]>(patterns.size());
		for (String pattern : patterns) {
			charpatterns.add(pattern.toCharArray());
		}
		return charpatterns;
	}

	private static int blockSize(int minLength, char minChar, char maxChar, int patterns) {
		int optSize = (int) Math.ceil(Math.log(2 * minLength * patterns) / Math.log(maxChar - minChar));
		if (optSize > minLength) {
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
		List<String> patternStrings = new ArrayList<>();
		Set<String> blocks = new HashSet<>();
		for (char[] pattern : patterns) {
			patternStrings.add(new String(pattern));
			for (int i = 0; i < pattern.length + 1 - block; i++) {
				blocks.add(new String(Arrays.copyOfRange(pattern, i, i + block)));
			}
		}
		for (String currentBlock : blocks) {
			int shiftKey = shiftHash(currentBlock.toCharArray());
			int shiftBy = shift[shiftKey];
			for (String pattern : patternStrings) {
				int rightMost = pattern.length() - findRightMost(pattern, currentBlock) - block;
				if (rightMost >= 0 && rightMost < shiftBy) {
					shiftBy = rightMost;
				}
			}
			shift[shiftKey] = shiftBy;
		}
		return shift;
	}

	private static int findRightMost(String pattern, String block) {
		return pattern.lastIndexOf(block);
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

	@SuppressWarnings("unchecked")
	private static TrieNode<Void>[] computeHash(List<char[]> charpatterns, int block) {
		TrieNode<Void>[] hash = new TrieNode[HASH_SIZE];
		for (char[] pattern : charpatterns) {
			char[] lastBlock = Arrays.copyOfRange(pattern, pattern.length - block, pattern.length);
			int hashKey = hashHash(lastBlock);
			TrieNode<Void> trie = hash[hashKey];
			if (trie == null) {
				trie = new TrieNode<>();
				hash[hashKey] = trie;
			}
			trie.extendReverse(pattern);
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
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private class Finder extends AbstractStringFinder {

		private CharProvider chars;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.buffer = new LinkedList<>();
		}

		@Override
		public void skipTo(long pos) {
			chars.move(pos);
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove(0);
			}
			int lookahead = minLength - 1;
			while (!chars.finished(lookahead)) {
				long pos = chars.current();
				char[] lastBlock = chars.between(pos + minLength - block, pos + minLength);
				int shiftKey = shiftHash(lastBlock);
				int shiftBy = shift[shiftKey];
				if (shiftBy == 0) {
					int hashkey = hashHash(lastBlock);
					TrieNode<Void> node = hash[hashkey];
					if (node != null) {
						int patternPointer = lookahead;
						node = node.nextNode(chars.lookahead(patternPointer));
						while (node != null) {
							String match = node.getMatch();
							if (match != null) {
								buffer.add(createMatch(patternPointer, match));
							}
							patternPointer--;
							if (pos + patternPointer < 0) {
								break;
							}
							node = node.nextNode(chars.lookahead(patternPointer));
						}
					}
					chars.next();
					if (!buffer.isEmpty()) {
						return buffer.remove(0);
					}
				} else {
					chars.forward(shiftBy);
				}
			}
			return null;
		}

		private StringMatch createMatch(int patternPointer, String s) {
			long start = chars.current() + patternPointer;
			long end = chars.current() + minLength;
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new WuManber(patterns);
		}

	}

}
