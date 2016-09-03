package net.amygdalum.stringsearchalgorithms.search;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.io.StringCharProvider;
import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringFinderOption;
import net.amygdalum.stringsearchalgorithms.search.StringSearchAlgorithm;
import net.amygdalum.stringsearchalgorithms.search.StringSearchAlgorithmFactory;

public class StringSearchRule implements TestRule {

	private StringSearchAlgorithm algorithm;
	private List<StringSearchAlgorithmFactory> algorithmFactories;

	public StringSearchRule(StringSearchAlgorithmFactory... algorithmFactories) {
		this.algorithmFactories = asList(algorithmFactories);
	}

	private List<StringSearchAlgorithm> getAlgorithms(String pattern) {
		List<StringSearchAlgorithm> algorithms = new ArrayList<>();
		for (StringSearchAlgorithmFactory algorithmFactory : algorithmFactories) {
			algorithms.add(algorithmFactory.of(pattern));
		}
		return algorithms;
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				String pattern = extractPattern(description);
				List<StringSearchAlgorithm> algorithms = getAlgorithms(pattern);
				Map<StringSearchAlgorithm, String> failures = new IdentityHashMap<StringSearchAlgorithm, String>();
				StackTraceElement[] stackTrace = null;
				for (StringSearchAlgorithm algorithm : algorithms) {
					StringSearchRule.this.algorithm = algorithm;
					try {
						base.evaluate();
					} catch (AssertionError e) {
						String message = e.getMessage() == null ? "" : e.getMessage();
						failures.put(algorithm, message);
						if (stackTrace == null) {
							stackTrace = e.getStackTrace();
						}
					} catch (Throwable e) {
						String message = e.getMessage() == null ? "" : e.getMessage();
						throw new RuntimeException("In mode " + algorithm.toString() + ": " + message, e);
					}
				}
				if (!failures.isEmpty()) {
					AssertionError ne = new AssertionError(computeMessage(failures));
					ne.setStackTrace(stackTrace);
					throw ne;
				}
			}

		};
	}

	private String extractPattern(final Description description) throws AssertionError {
		SearchFor searchFor = description.getAnnotation(SearchFor.class);
		if (searchFor == null) {
			throw new AssertionError("expected @SearchFor annotation");
		}
		String[] pattern = searchFor.value();
		if (pattern.length != 1) {
			throw new AssertionError("expected exactly one pattern");
		}
		return pattern[0];
	}

	private String computeMessage(Map<StringSearchAlgorithm, String> failures) {
		StringBuilder buffer = new StringBuilder();
		for (Map.Entry<StringSearchAlgorithm, String> entry : failures.entrySet()) {
			buffer.append("in algorithm <").append(entry.getKey().getClass().getSimpleName()).append(">: ").append(entry.getValue()).append("\n");
		}
		return buffer.toString();
	}

	public StringFinder createSearcher(String chars, StringFinderOption... options) {
		return createSearcher(new StringCharProvider(chars, 0), options);
	}
	
	public StringFinder createSearcher(CharProvider chars, StringFinderOption... options) {
		return algorithm.createFinder(chars, options);
	}

	public StringSearchAlgorithm getAlgorithm() {
		return algorithm;
	}

}
