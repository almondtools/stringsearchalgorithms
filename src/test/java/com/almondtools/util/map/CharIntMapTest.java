package com.almondtools.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.picklock.ObjectAccess;

public class CharIntMapTest {

	private int DEFAULT_VALUE = 42;

	@Test
	public void testKeysEmpty() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.keys(), charArrayContaining((char) 0));
	}

	@Test
	public void testKeys() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 55)
			.perfectMinimal();
		assertThat(map.keys(), charArrayContaining('a'));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.getDefaultValue(), equalTo(42));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.get((char) 0), equalTo(42));
		assertThat(map.get('x'), equalTo(42));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 43)
			.perfectMinimal();
		assertThat(map.get('a'), equalTo(43));
		assertThat(map.get('x'), equalTo(42));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 43)
			.addEntry('b', 44)
			.perfectMinimal();
		assertThat(map.get('a'), equalTo(43));
		assertThat(map.get('b'), equalTo(44));
		assertThat(map.get('x'), equalTo(42));
	}

	@Test
	public void testGetForUnexpectedChar() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		HashFunction h = ObjectAccess.unlock(map).features(OpenCharIntMap.class).getH();
		ObjectAccess.unlock(h).features(OpenHashFunction.class).setG(new int[] { 1 });
		assertThat(map.get('x'), equalTo(42));
	}

	@Test
	public void testPerfectMinimalFailing() throws Exception {
		CharIntMap map = new CharIntMap.Builder(DEFAULT_VALUE) {
			protected void computeFunctions(int maxTries, double c) throws HashBuildException {
				throw new HashBuildException();
			}
		}
			.addEntry('a', 43)
			.addEntry('b', 44)
			.perfectMinimal();
		assertThat(map.getClass().getName(), containsString("Fallback"));
		assertThat(map.get('a'), equalTo(43));
		assertThat(map.get('b'), equalTo(44));
		assertThat(map.get('x'), equalTo(42));
	}

	interface OpenCharIntMap {

		HashFunction getH();

	}

	interface OpenHashFunction {

		void setG(int[] g);

	}

}
