package com.almondtools.util.text;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testReverse() throws Exception {
		assertThat(StringUtils.reverse("CBA"), equalTo("ABC"));
	}

	@Test
	public void testJoin() throws Exception {
		assertThat(StringUtils.join(Arrays.asList("A", "B", "C")), equalTo("ABC"));
	}

	@Test
	public void testJoinWithSeparatorChar() throws Exception {
		assertThat(StringUtils.join(Arrays.asList("A", "B", "C"), '-'), equalTo("A-B-C"));
	}

	@Test
	public void testJoinWithSeparatorString() throws Exception {
		assertThat(StringUtils.join(Arrays.asList("A", "B", "C"), ", "), equalTo("A, B, C"));
	}

}
