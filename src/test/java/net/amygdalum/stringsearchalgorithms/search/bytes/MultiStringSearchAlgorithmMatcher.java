package net.amygdalum.stringsearchalgorithms.search.bytes;

import static java.util.Arrays.asList;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class MultiStringSearchAlgorithmMatcher extends TypeSafeMatcher<Class<?>> {

	@Override
	protected boolean matchesSafely(Class<?> item) {
		if (!StringSearchAlgorithm.class.isAssignableFrom(item)) {
			return false;
		}
		try {
			MultiStringSearchAlgorithmFactory factory = null;
			for (Class<?> nestedClass : item.getClasses()) {
				if (MultiStringSearchAlgorithmFactory.class.isAssignableFrom(nestedClass)) {
					factory = (MultiStringSearchAlgorithmFactory) nestedClass.newInstance();
				}
			}
			StringSearchAlgorithm algorithm = factory.of(asList("x"));
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

	public static MultiStringSearchAlgorithmMatcher isMultiStringSearchAlgorithm() {
		return new MultiStringSearchAlgorithmMatcher();
	}

}
