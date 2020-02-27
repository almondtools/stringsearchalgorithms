package net.amygdalum.stringsearchalgorithms.search;

import static net.amygdalum.extensions.hamcrest.conventions.EnumMatcher.isEnum;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class MatchOptionTest {

	@Test
	public void testMatchOption() throws Exception {
		assertThat(MatchOption.class, isEnum().withElements(3));
	}
	
	@Test
	public void testIn() throws Exception {
		assertThat(LONGEST_MATCH.in(new StringFinderOption[]{LONGEST_MATCH}), is(true));
		assertThat(LONGEST_MATCH.in(new StringFinderOption[]{}), is(false));
		assertThat(LONGEST_MATCH.in(new StringFinderOption[]{new StringFinderOption() {}}), is(false));
	}

}
