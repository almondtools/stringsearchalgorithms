package com.almondtools.util.map;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.picklock.ObjectAccess;

public class CharLongMapTest {

	private long DEFAULT_VALUE = 42l;

	@Test
	public void testKeysEmpty() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.keys(), charArrayContaining((char) 0));
	}

	@Test
	public void testKeys() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 55l)
			.perfectMinimal();
		assertThat(map.keys(), charArrayContaining('a'));
	}

	@Test
	public void testGetDefaultValue() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.getDefaultValue(), equalTo(42l));
	}

	@Test
	public void testGetForEmptyMap() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		assertThat(map.get((char) 0), equalTo(42l));
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testGetForOneElement() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 43l)
			.perfectMinimal();
		assertThat(map.get('a'), equalTo(43l));
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testGetForMoreElements() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.addEntry('a', 43l)
			.addEntry('b', 44l)
			.perfectMinimal();
		assertThat(map.get('a'), equalTo(43l));
		assertThat(map.get('b'), equalTo(44l));
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testGetForUnexpectedChar() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE)
			.perfectMinimal();
		HashFunction h = ObjectAccess.unlock(map).features(OpenCharLongMap.class).getH();
		ObjectAccess.unlock(h).features(OpenHashFunction.class).setG(new int[] { 1 });
		assertThat(map.get('x'), equalTo(42l));
	}

	@Test
	public void testPerfectMinimalFailing() throws Exception {
		CharLongMap map = new CharLongMap.Builder(DEFAULT_VALUE) {
			protected void computeFunctions(int maxTries, double c) throws HashBuildException {
				throw new HashBuildException();
			}
		}
			.addEntry('a', 43l)
			.addEntry('b', 44l)
			.perfectMinimal();
		assertThat(map.getClass().getName(), containsString("Fallback"));
		assertThat(map.get('a'), equalTo(43l));
		assertThat(map.get('b'), equalTo(44l));
		assertThat(map.get('x'), equalTo(42l));
	}

	interface OpenCharLongMap {

		HashFunction getH();

	}

	interface OpenHashFunction {

		void setG(int[] g);

	}

}
