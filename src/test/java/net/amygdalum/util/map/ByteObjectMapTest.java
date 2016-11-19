package net.amygdalum.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.byteArrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ByteObjectMapTest {

	private static final byte a = (byte) 0x61;
	private static final byte b = (byte) 0x62;
	private static final byte c = (byte) 0x63;
	private static final byte x = (byte) 0x78;

	private String DEFAULT_VALUE = "42";
	
	@Test
	public void testKeysEmpty() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.keys().length, equalTo(0));
	}

	@Test
	public void testKeys() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "55");
		assertThat(map.keys(), byteArrayContaining(a));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.getDefaultValue(), equalTo("42"));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.get((byte) 0), equalTo("42"));
		assertThat(map.get(x), equalTo("42"));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "43");
		assertThat(map.get(a), equalTo("43"));
		assertThat(map.get((byte) 0x78), equalTo("42"));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "43")
			.add(b, "44");
		assertThat(map.get(a), equalTo("43"));
		assertThat(map.get(b), equalTo("44"));
		assertThat(map.get((byte) 0x78), equalTo("42"));
	}

	@Test
	public void testCursor() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "43")
			.add(b, "44");
		Iterator<ByteObjectMap<String>.Entry> iterator = map.cursor().iterator();

		List<Byte> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}

		assertThat(keys, containsInAnyOrder(a, b));
	}

	@Test
	public void testCursorWithNullValue() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "43")
			.add(b, "44")
			.add(c, null);
		Iterator<ByteObjectMap<String>.Entry> iterator = map.cursor().iterator();

		List<Byte> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}

		assertThat(keys, containsInAnyOrder(a, b, c));
	}

	@Test
	public void testCursorWithNullKey() throws Exception {
		ByteObjectMap<String> map = new ByteObjectMap<String>(DEFAULT_VALUE)
			.add(a, "43")
			.add(b, "44")
			.add((byte) 0, "45");
		Iterator<ByteObjectMap<String>.Entry> iterator = map.cursor().iterator();

		List<Byte> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			keys.add(iterator.next().key);
		}

		assertThat(keys, containsInAnyOrder(a, b, (byte) 0));
	}

}
