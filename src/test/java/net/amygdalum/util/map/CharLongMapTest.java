package net.amygdalum.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CharLongMapTest {

	private long DEFAULT_VALUE = 42l;

	@Test
	public void testKeysEmpty() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE);
		assertThat(map.keys().length, equalTo(0));
	}

	@Test
	public void testKeys() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE)
			.add('a', 55l);
		assertThat(map.keys(), charArrayContaining('a'));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE);
		assertThat(map.getDefaultValue(), equalTo(42l));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE);
		assertThat(map.get((char) 0), equalTo(42l));
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE)
			.add('a', 43l);
		assertThat(map.get('a'), equalTo(43l));
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		CharLongMap map = new CharLongMap(DEFAULT_VALUE)
			.add('a', 43l)
			.add('b', 44l);
		assertThat(map.get('a'), equalTo(43l));
		assertThat(map.get('b'), equalTo(44l));
		assertThat(map.get('x'), equalTo(42l));
	}

}
