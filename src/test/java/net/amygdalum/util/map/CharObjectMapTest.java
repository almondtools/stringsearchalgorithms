package net.amygdalum.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import net.amygdalum.util.map.CharObjectMap.Entry;

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

	@Test
	public void testCursor() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "43")
			.add('b', "44");
		Iterator<Entry<String>> iterator = map.cursor().iterator();

		List<Character> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}
		
		assertThat(keys, containsInAnyOrder('a', 'b'));
	}

	@Test
	public void testCursorWithNullValue() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "43")
			.add('b', "44")
			.add('c', null);
		Iterator<Entry<String>> iterator = map.cursor().iterator();

		List<Character> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}
		
		assertThat(keys, containsInAnyOrder('a', 'b','c'));
	}

	@Test
	public void testCursorWithNullKey() throws Exception {
		CharObjectMap<String> map = new CharObjectMap<String>(DEFAULT_VALUE)
			.add('a', "43")
			.add('b', "44")
			.add((char) 0, "45");
		Iterator<Entry<String>> iterator = map.cursor().iterator();

		List<Character> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}
		
		assertThat(keys, containsInAnyOrder('a', 'b', (char) 0));
	}

}
