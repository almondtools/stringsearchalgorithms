package com.almondtools.stringsandchars.search;

import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;
import static com.almondtools.util.text.CharUtils.maxLength;
import static com.almondtools.util.text.CharUtils.minLength;
import static com.almondtools.util.text.StringUtils.toCharArray;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.almondtools.stringsandchars.io.CharProvider;

/**
 * An implementation of the Set Horspool Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a finder which can find any of these patterns in documents. 
 */
public class SetHorspool implements StringSearchAlgorithm {

	private TrieNode<Void> trie;
	private int minLength;
	private int maxLength;
	private CharShift charShift;

	public SetHorspool(List<String> patterns) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
		this.maxLength = maxLength(charpatterns);
		this.charShift = computeCharacterShift(charpatterns, minLength);
	}

	private CharShift computeCharacterShift(List<char[]> charpatterns, int minLength) {
		if (isCompactRange(charpatterns, minLength)) {
			return new QuickShift(charpatterns, minLength);
		} else {
			return new SmartShift(charpatterns, minLength);
		}
	}

	public boolean isCompactRange(List<char[]> charpatterns, int minLength) {
		char minChar = computeMinChar(charpatterns);
		char maxChar = computeMaxChar(charpatterns);
		return maxChar - minChar < 256 || maxChar - minChar < minLength * 2;
	}

	private static TrieNode<Void> computeTrie(List<char[]> charpatterns) {
		TrieNode<Void> trie = new TrieNode<>();
		for (char[] pattern : charpatterns) {
			trie.extendReverse(pattern);
		}
		return trie;
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(chars, options);
		} else {
			return new NextMatchFinder(chars, options);
		}
	}

	@Override
	public int getPatternLength() {
		return minLength;
	}

	private class NextMatchFinder extends Finder {

		public NextMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!buffer.isEmpty()) {
				return buffer.remove();
			}
			int lookahead = minLength - 1;
			while (!chars.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = chars.current();
				char current = chars.lookahead(patternPointer);
				TrieNode<Void> node = trie.nextNode(current);
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
				chars.forward(charShift.getShift(current));
				if (!buffer.isEmpty()) {
					return buffer.remove();
				}
			}
			return null;
		}

	}

	private abstract class Finder extends AbstractStringFinder {

		protected CharProvider chars;
		protected Queue<StringMatch> buffer;

		public Finder(CharProvider chars, StringFinderOption... options) {
			super(options);
			this.chars = chars;
			this.buffer = new PriorityQueue<>();
		}

		@Override
		public void skipTo(long pos) {
			long last = pos;
			Iterator<StringMatch> bufferIterator = buffer.iterator();
			while (bufferIterator.hasNext()) {
				StringMatch next = bufferIterator.next();
				if (next.start() < pos) {
					bufferIterator.remove();
				} else if (next.end() > last) {
					last = next.end();
				}
			}
			chars.move(last);
		}

		protected StringMatch createMatch(int patternPointer, String s) {
			long start = chars.current() + patternPointer;
			long end = chars.current() + minLength;
			return new StringMatch(start, end, s);
		}
	}

	private class LongestMatchFinder extends Finder {

		public LongestMatchFinder(CharProvider chars, StringFinderOption... options) {
			super(chars, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			int lookahead = minLength - 1;
			while (!chars.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = chars.current();
				char current = chars.lookahead(patternPointer);
				TrieNode<Void> node = trie.nextNode(current);
				while (node != null) {
					String match = node.getMatch();
					if (match != null) {
						StringMatch stringMatch = createMatch(patternPointer, match);
						lastStart = stringMatch.start();
						buffer.add(stringMatch);
					}
					patternPointer--;
					if (pos + patternPointer < 0) {
						break;
					}
					node = node.nextNode(chars.lookahead(patternPointer));
				}
				chars.forward(charShift.getShift(current));
				if (bufferContainsLongestMatch(lastStart)) {
					break;
				}
			}
			if (buffer.isEmpty()) {
				return null;
			} else {
				return longestMatchFromBuffer();
			}
		}

		public boolean bufferContainsLongestMatch(long lastStart) {
			return !buffer.isEmpty()
				&& chars.current() - lastStart - 1 > maxLength - minLength;
		}

		private long lastStartFromBuffer() {
			long start = Long.MAX_VALUE;
			Iterator<StringMatch> bufferIterator = buffer.iterator();
			while (bufferIterator.hasNext()) {
				StringMatch next = bufferIterator.next();
				if (next.start() < start) {
					start = next.start();
				}
			}
			if (start == Long.MAX_VALUE) {
				return -1;
			} else {
				return start;
			}
		}

		private StringMatch longestMatchFromBuffer() {
			StringMatch match = buffer.remove();
			while (!buffer.isEmpty()) {
				StringMatch nextMatch = buffer.peek();
				if (nextMatch.start() == match.start()) {
					match = buffer.remove();
				} else {
					break;
				}
			}
			return match;
		}

	}

	public static class Factory implements MultiWordSearchAlgorithmFactory {

		@Override
		public StringSearchAlgorithm of(List<String> patterns) {
			return new SetHorspool(patterns);
		}

	}

	private static class QuickShift implements CharShift {

		private char minChar;
		private char maxChar;
		private int[] characterShift;
		private int defaultShift;

		public QuickShift(List<char[]> charpatterns, int minLength) {
			this.minChar = computeMinChar(charpatterns);
			this.maxChar = computeMaxChar(charpatterns);
			this.characterShift = computeCharacterShift(charpatterns, minLength, computeMinChar(charpatterns), computeMaxChar(charpatterns));
			this.defaultShift = minLength;
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

		@Override
		public int getShift(char c) {
			if (c < minChar || c > maxChar) {
				return defaultShift;
			}
			return characterShift[c - minChar];
		}

	}

	private static class SmartShift implements CharShift {

		private CharIntMap characterShift;

		public SmartShift(List<char[]> charpatterns, int minLength) {
			this.characterShift = computeCharacterShift(charpatterns, minLength);
		}

		private static CharIntMap computeCharacterShift(List<char[]> patterns, int minLength) {
			CharIntMap.Builder mapBuilder = new CharIntMap.Builder(minLength);
			for (char[] pattern : patterns) {
				for (int i = 0; i < pattern.length - 1; i++) {
					int value = mapBuilder.get(pattern[i]);
					mapBuilder.put(pattern[i], min(value, pattern.length - i - 1));
				}
			}
			return mapBuilder.perfectMinimal();
		}

		private static int min(int i, int j) {
			return i < j ? i : j;
		}

		@Override
		public int getShift(char c) {
			return characterShift.get(c);
		}

	}

}
