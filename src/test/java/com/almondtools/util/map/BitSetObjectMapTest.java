package com.almondtools.util.map;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.BitSet;

import org.junit.Test;

import com.almondtools.picklock.ObjectAccess;

public class BitSetObjectMapTest {

	private String DEFAULT_VALUE = "42";

	@Test
	public void testKeysEmpty() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.keys(), arrayContaining(bs()));
	}

	@Test
	public void testKeys() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.addEntry(bs(1), "55")
			.perfectMinimal();
		assertThat(map.keys(), arrayContaining(bs(1)));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.getDefaultValue(), equalTo("42"));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.get(bs()), equalTo("42"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.addEntry(bs(1), "43")
			.perfectMinimal();
		assertThat(map.get(bs(1)), equalTo("43"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.addEntry(bs(1), "43")
			.addEntry(bs(2,3,4), "44")
			.perfectMinimal();
		assertThat(map.get(bs(1)), equalTo("43"));
		assertThat(map.get(bs(2,3,4)), equalTo("44"));
		assertThat(map.get(bs(0,8,15)), equalTo("42"));
	}

	@Test
	public void testGetForUnexpectedChar() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE)
			.perfectMinimal();
		HashFunction h = ObjectAccess.unlock(map).features(OpenBitSetObjectMap.class).getH();
		ObjectAccess.unlock(h).features(OpenHashFunction.class).setG(new int[] { 1 });
		assertThat(map.get(bs()), equalTo("42"));
	}

	@Test
	public void testPerfectMinimalFailing() throws Exception {
		BitSetObjectMap<String> map = new BitSetObjectMap.Builder<String>(DEFAULT_VALUE) {
			protected void computeFunctions(int maxTries, double c) throws HashBuildException {
				throw new HashBuildException();
			}
		}
			.addEntry(bs(1), "43")
			.addEntry(bs(2,3,4), "44")
			.perfectMinimal();
		assertThat(map.getClass().getName(), containsString("Fallback"));
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

	interface OpenBitSetObjectMap {

		HashFunction getH();

	}

	interface OpenHashFunction {

		void setG(int[] g);

	}

}
