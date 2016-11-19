package net.amygdalum.stringsearchalgorithms.search.bytes;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class StringSearchAlgorithmMatcher extends TypeSafeMatcher<Class<?>> {

	@Override
	protected boolean matchesSafely(Class<?> item) {
		if (!StringSearchAlgorithm.class.isAssignableFrom(item)) {
			return false;
		}
		try {
			StringSearchAlgorithmFactory factory = null;
			for (Class<?> nestedClass : item.getClasses()) {
				if (StringSearchAlgorithmFactory.class.isAssignableFrom(nestedClass)) {
					factory = (StringSearchAlgorithmFactory) nestedClass.newInstance();
				}
			}
			StringSearchAlgorithm algorithm = factory.of("x");
			if (!item.isInstance(algorithm)) {
				return false;
			}
		} catch (InstantiationException | IllegalAccessException e) {
			return false;
		}
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("a string search algorithm must contain a factory (default constructor) creating itself");
	}

	public static StringSearchAlgorithmMatcher isByteStringSearchAlgorithm() {
		return new StringSearchAlgorithmMatcher();
	}

}
