package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;

public class SetHorspool implements StringSearchAlgorithm {

	private TrieRoot trie;
	private char minChar;
	private char maxChar;
	private int minLength;
	private int[] characterShift;

	public SetHorspool(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.maxChar = computeMaxChar(charpatterns);
		this.minChar = computeMinChar(charpatterns);
		this.minLength = minLength(charpatterns);
		this.characterShift = computeCharacterShift(charpatterns, minLength, minChar, maxChar);
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

	private static TrieRoot computeTrie(List<char[]> charpatterns) {
		TrieRoot trie = new TrieRoot();
		for (char[] pattern : charpatterns) {
			trie.extendReverse(pattern);
		}
		return trie;
	}

	private static int[] computeCharacterShift(List<char[]> patterns, int minLength, char min, char max) {
		int[] characters = new int[max - min + 1];
		for (int i = 0; i < characters.length; i++) {
			characters[i] = minLength;
		}
		for (char[] pattern : patterns) {
			for (int i = 0; i < pattern.length - 1; i++) {
				characters[pattern[i] - min] = min(characters[pattern[i] - min], pattern.length - i - 1);
			}
		}
		return characters;
	}

	private static int min(int i, int j) {
		return i < j ? i : j;
	}

	private int getShift(char c) {
		if (c < minChar || c > maxChar) {
			return minLength;
		}
		return characterShift[c - minChar];
	}

	@Override
	public StringFinder createFinder(CharProvider chars) {
		return new Finder(chars);
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private class Finder implements StringFinder {

		private CharProvider chars;
		private List<StringMatch> buffer;

		public Finder(CharProvider chars) {
			this.chars = chars;
			this.buffer = new LinkedList<StringMatch>();
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
				int patternPointer = lookahead;
				long pos = chars.current();
				char current = chars.lookahead(patternPointer);
				TrieNode node = trie.nextNode(current);
				while (node != null) {
					if (node.isTerminal()) {
						buffer.add(createMatch(patternPointer));
					}
					patternPointer--;
					if (pos + patternPointer < 0) {
						break;
					}
					node = node.nextNode(chars.lookahead(patternPointer));
				}
				chars.forward(getShift(current));
				if (!buffer.isEmpty()) {
					return buffer.remove(0);
				}
			}
			return null;
		}

		@Override
		public List<StringMatch> findAll() {
			List<StringMatch> matches = new ArrayList<StringMatch>();
			while (true) {
				StringMatch match = findNext();
				if (match == null) {
					return matches;
				} else {
					matches.add(match);
				}
			}
		}

		private StringMatch createMatch(int patternPointer) {
			long start = chars.current() + patternPointer;
			long end = chars.current() + minLength;
			String s = chars.slice(start, end);
			return new StringMatch(start, end, s);
		}
	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new SetHorspool(patterns);
		}

	}

}
