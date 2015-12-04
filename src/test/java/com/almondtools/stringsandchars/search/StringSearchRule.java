package com.almondtools.stringsandchars.search;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.almondtools.stringsandchars.io.CharProvider;

public class StringSearchRule implements TestRule {

	private StringSearchAlgorithm algorithm;
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				SearchFor searchFor = description.getAnnotation(SearchFor.class);
				if (searchFor == null)  {
					throw new AssertionError();
				}
				String pattern = searchFor.value();
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

	private String computeMessage(Map<StringSearchAlgorithm, String> failures) {
		StringBuilder buffer = new StringBuilder();
		for (Map.Entry<StringSearchAlgorithm, String> entry : failures.entrySet()) {
			buffer.append("in algorithm <").append(entry.getKey().getClass().getSimpleName()).append(">: ").append(entry.getValue()).append("\n");
		}
		return buffer.toString();
	}

	private List<StringSearchAlgorithm> getAlgorithms(String pattern) {
		return Arrays.asList((StringSearchAlgorithm) 
			new ShiftAnd(pattern),
			new KnuthMorrisPratt(pattern),
			new Horspool(pattern),
			new Sunday(pattern)
		);
	}

	public StringFinder createSearcher(CharProvider chars) {
		return algorithm.createFinder(chars);
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface SearchFor {

		String value();
	}

}
