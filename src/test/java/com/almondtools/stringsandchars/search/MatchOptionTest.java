package com.almondtools.stringsandchars.search;

import static com.almondtools.conmatch.conventions.EnumMatcher.isEnum;
import static com.almondtools.stringsandchars.search.MatchOption.LONGEST_MATCH;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
