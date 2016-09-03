package net.amygdalum.util.map;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.BitSet;

import org.junit.Test;

import net.amygdalum.util.map.BitSetObjectMap;

public class BitSetObjectMapTest {

	private String DEFAULT_VALUE = "42";

	@Test
	public void testKeysEmpty() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.keys().length, equalTo(0));
	}

	@Test
	public void testKeys() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE)
			.add(bs(1), "55");
		assertThat(map.keys(), arrayContaining(bs(1)));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.getDefaultValue(), equalTo("42"));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE);
		assertThat(map.get(bs()), equalTo("42"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE)
			.add(bs(1), "43");
		assertThat(map.get(bs(1)), equalTo("43"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap<String>(DEFAULT_VALUE)
			.add(bs(1), "43")
			.add(bs(2,3,4), "44");
		assertThat(map.get(bs(1)), equalTo("43"));
		assertThat(map.get(bs(2,3,4)), equalTo("44"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	private static BitSet bs(int... activeBits) {
		BitSet bitSet = new BitSet();
		for (int bit : activeBits) {
			bitSet.set(bit);
		}
		return bitSet;
	}

}
