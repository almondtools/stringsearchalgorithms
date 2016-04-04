package com.almondtools.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CharObjectMapTest {

	private String DEFAULT_VALUE = "42";

	@Test
	public void testKeysEmpty() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.keys().length, equalTo(0));
	}

	@Test
	public void testKeys() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "55");
		assertThat(map.keys(), charArrayContaining('a'));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.getDefaultValue(), equalTo("42"));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.get((char) 0), equalTo("42"));
		assertThat(map.get('x'), equalTo("42"));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "43");
		assertThat(map.get('a'), equalTo("43"));
		assertThat(map.get('x'), equalTo("42"));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "43")
			.add('b', "44");
		assertThat(map.get('a'), equalTo("43"));
		assertThat(map.get('b'), equalTo("44"));
		assertThat(map.get('x'), equalTo("42"));
	}

}
