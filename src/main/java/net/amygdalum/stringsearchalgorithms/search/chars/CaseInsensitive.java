package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import net.amygdalum.stringsearchalgorithms.io.CaseInsensitiveCharProvider;
import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.util.text.CharMapping;

public class CaseInsensitive implements StringSearchAlgorithm {

	public static final CharMapping MAPPING = new CharMapping() {
		
		@Override
		public char[] map(char c) {
			return new char[]{toLowerCase(c), toUpperCase(c)};
		}
	};

	private StringSearchAlgorithm algorithm;

	private CaseInsensitive(StringSearchAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public static StringSearchAlgorithmFactory caseInsensitive(StringSearchAlgorithmFactory factory) {
		if (factory instanceof SupportsCharClasses<?>) {
			return ((SupportsCharClasses<?>) factory).withCharClasses(MAPPING);
		} else {
			return new Factory(factory);
		}
	}
	
	@Override
	public StringFinder createFinder(CharProvider chars, StringFinderOption... options) {
		return algorithm.createFinder(new CaseInsensitiveCharProvider(chars), options);
	}

	@Override
	public int getPatternLength() {
		return algorithm.getPatternLength();
	}

	public static class Factory implements StringSearchAlgorithmFactory {

		private StringSearchAlgorithmFactory factory;

		private Factory(StringSearchAlgorithmFactory factory) {
			this.factory = factory;
		}

		@Override
		public StringSearchAlgorithm of(String pattern) {
			String lc = pattern.toLowerCase();
			return new CaseInsensitive(factory.of(lc));
		}

	}

}
