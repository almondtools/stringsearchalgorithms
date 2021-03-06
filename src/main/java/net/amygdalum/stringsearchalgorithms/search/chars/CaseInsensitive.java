package net.amygdalum.stringsearchalgorithms.search.chars;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.util.io.CaseInsensitiveCharProvider;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.text.CharMapping;

public class CaseInsensitive implements StringSearchAlgorithm, StringSearchAlgorithmWrapper {

	public static final CharMapping MAPPING = new CharMapping() {

		@Override
		public char[] map(char c) {
			char lc = toLowerCase(c);
			char uc = toUpperCase(c);
			if (lc != uc) {
				return new char[] { lc, uc };
			} else {
				return new char[] { lc };
			}
		}
		
		public char[] normalized(char[] chars) {
			char[] normalized = new char[chars.length];
			for (int i = 0; i < normalized.length; i++) {
				normalized[i] = toLowerCase(chars[i]);
			}
			return normalized;
		};
	};

	private StringSearchAlgorithm algorithm;

	private CaseInsensitive(StringSearchAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	@Override
	public StringSearchAlgorithm getAlgorithm() {
		return algorithm;
	}

	public static StringSearchAlgorithmFactory caseInsensitive(StringSearchAlgorithmFactory factory) {
		if (factory instanceof SupportsCharClasses) {
			((SupportsCharClasses) factory).enableCharClasses(MAPPING);
			return factory;
		} else {
			return new Factory(factory);
		}
	}

	public static MultiStringSearchAlgorithmFactory caseInsensitive(MultiStringSearchAlgorithmFactory factory) {
		if (factory instanceof SupportsCharClasses) {
			((SupportsCharClasses) factory).enableCharClasses(MAPPING);
			return factory;
		} else {
			return new MultiFactory(factory);
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

	public static class MultiFactory implements MultiStringSearchAlgorithmFactory {

		private MultiStringSearchAlgorithmFactory factory;

		private MultiFactory(MultiStringSearchAlgorithmFactory factory) {
			this.factory = factory;
		}

		@Override
		public StringSearchAlgorithm of(Collection<String> patterns) {
			Set<String> lc = new LinkedHashSet<>();
			for (String pattern : patterns) {
				lc.add(pattern.toLowerCase());
			}
			return new CaseInsensitive(factory.of(lc));
		}

	}

}
