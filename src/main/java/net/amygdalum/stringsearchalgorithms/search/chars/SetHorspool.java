package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.lang.Math.min;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;
import static net.amygdalum.util.text.CharUtils.maxLength;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.CharUtils.revert;
import static net.amygdalum.util.text.StringUtils.toCharArray;

import java.util.Collection;
import java.util.List;

import net.amygdalum.stringsearchalgorithms.search.BufferedStringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.map.CharIntMap;
import net.amygdalum.util.text.CharAutomaton;
import net.amygdalum.util.text.CharTrieBuilder;
import net.amygdalum.util.text.CharWordSet;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayCharCompactTrie;
import net.amygdalum.util.text.doublearraytrie.DoubleArrayCharTrieBuilder;

/**
 * An implementation of the Set Horspool Algorithm.
 * 
 * This algorithm takes a multiple string patterns as input and generates a
 * finder which can find any of these patterns in documents.
 */
public class SetHorspool implements StringSearchAlgorithm {

	private CharWordSet<String> trie;
	private int minLength;
	private int maxLength;
	private CharShift charShift;

	public SetHorspool(Collection<String> patterns) {
		this(patterns, false);
	}

	public SetHorspool(Collection<String> patterns, boolean relaxed) {
		List<char[]> charpatterns = toCharArray(patterns);
		this.trie = computeTrie(charpatterns);
		this.minLength = minLength(charpatterns);
		this.maxLength = maxLength(charpatterns);
		this.charShift = computeCharacterShift(charpatterns, minLength, relaxed);
	}

	private CharShift computeCharacterShift(List<char[]> charpatterns, int minLength, boolean relaxed) {
		if (isCompactRange(charpatterns, minLength)) {
			return new QuickShift(charpatterns, minLength);
		} else if (relaxed) {
			return new RelaxedShift(charpatterns, minLength);
		} else {
			return new SmartShift(charpatterns, minLength);
		}
	}

	public boolean isCompactRange(List<char[]> charpatterns, int minLength) {
		char minChar = computeMinChar(charpatterns);
		char maxChar = computeMaxChar(charpatterns);
		return maxChar - minChar < 256 || maxChar - minChar < minLength * 2;
	}

	private static CharWordSet<String> computeTrie(List<char[]> charpatterns) {
		CharTrieBuilder<String> builder = new DoubleArrayCharTrieBuilder<>(new DoubleArrayCharCompactTrie<String>());

		for (char[] pattern : charpatterns) {
			builder.extend(revert(pattern), new String(pattern));
		}

		return builder.build();
	}

	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		if (LONGEST_MATCH.in(options)) {
			return new LongestMatchFinder(trie, minLength, maxLength, charShift, chars, options);
		} else {
			return new NextMatchFinder(trie, minLength, maxLength, charShift, chars, options);
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
		protected final int maxLength;
		protected final CharShift charShift;
		protected CharProvider chars;
		protected CharAutomaton<String> cursor;

		public Finder(CharWordSet<String> trie, int minLength, int maxLength, CharShift charShift, CharProvider chars, StringFinderOption... options) {
			super(options);
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.charShift = charShift;
			this.chars = chars;
			this.cursor = trie.cursor();
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

		public NextMatchFinder(CharWordSet<String> trie, int minLength, int maxLength, CharShift charShift, CharProvider chars, StringFinderOption... options) {
			super(trie, minLength, maxLength, charShift, chars, options);
		}

		@Override
		public StringMatch findNext() {
			if (!isBufferEmpty()) {
				return leftMost();
			}
			int lookahead = minLength - 1;
			while (!chars.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = chars.current();
				char current = chars.lookahead(patternPointer);

				cursor.reset();
				boolean success = cursor.accept(current);
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
				chars.forward(charShift.getShift(current));
				if (!isBufferEmpty()) {
					return leftMost();
				}
			}
			return null;
		}

	}

	private static class LongestMatchFinder extends Finder {

		public LongestMatchFinder(CharWordSet<String> trie, int minLength, int maxLength, CharShift charShift, CharProvider chars, StringFinderOption... options) {
			super(trie, minLength, maxLength, charShift, chars, options);
		}

		@Override
		public StringMatch findNext() {
			long lastStart = lastStartFromBuffer();
			int lookahead = minLength - 1;
			while (!chars.finished(lookahead)) {
				int patternPointer = lookahead;
				long pos = chars.current();
				char current = chars.lookahead(patternPointer);

				cursor.reset();
				boolean success = cursor.accept(current);
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
				chars.forward(charShift.getShift(current));
				if (bufferContainsLongestMatch(lastStart)) {
					break;
				}
			}
			return longestLeftMost();
		}

		public boolean bufferContainsLongestMatch(long lastStart) {
			return !isBufferEmpty()
				&& chars.current() - lastStart > maxLength;
		}

	}

	public static class Factory implements MultiStringSearchAlgorithmFactory {

		private boolean relaxed;

		public Factory() {
			this(false);
		}

		public Factory(boolean relaxed) {
			this.relaxed = relaxed;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			return new SetHorspool(patterns, relaxed);
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

		@Override
		public int getShift(char c) {
			if (c < minChar || c > maxChar) {
				return defaultShift;
			}
			return characterShift[c - minChar];
		}

	}

	private static class RelaxedShift implements CharShift {

		private int[] characterShift;

		public RelaxedShift(List<char[]> charpatterns, int minLength) {
			this.characterShift = computeCharacterShift(charpatterns, minLength);
		}

		private static int[] computeCharacterShift(List<char[]> patterns, int minLength) {
			int[] characters = new int[256];
			for (int i = 0; i < characters.length; i++) {
				characters[i] = minLength;
			}
			for (char[] pattern : patterns) {
				for (int i = 0; i < pattern.length - 1; i++) {
					int index = pattern[i] % 256;
					int newShift = pattern.length - i - 1;
					if (newShift < characters[index]) {
						characters[index] = newShift;
					}
				}
			}
			return characters;
		}

		@Override
		public int getShift(char c) {
			return characterShift[c % 256];
		}

	}

	private static class SmartShift implements CharShift {

		private CharIntMap characterShift;

		public SmartShift(List<char[]> charpatterns, int minLength) {
			this.characterShift = computeCharacterShift(charpatterns, minLength);
		}

		private static CharIntMap computeCharacterShift(List<char[]> patterns, int minLength) {
			CharIntMap map = new CharIntMap(minLength);
			for (char[] pattern : patterns) {
				for (int i = 0; i < pattern.length - 1; i++) {
					int value = map.get(pattern[i]);
					map.put(pattern[i], min(value, pattern.length - i - 1));
				}
			}
			return map;
		}

		@Override
		public int getShift(char c) {
			return characterShift.get(c);
		}

	}

}
