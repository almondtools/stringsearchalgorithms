package net.amygdalum.util.text;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.intArrayContaining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher;

public class QGramMappingTest {

	@Test
	public void testMap28() throws Exception {
		QGramMapping mapper = new QGramMapping(2, 8);

		assertThat(mapper.map(chars(1, 2), 0), equalTo((1 << 8) + 2));
		assertThat(mapper.map(chars(1,2, 4, 3), 2), equalTo((4 << 8) + 3));
		assertThat(mapper.map(chars(255, 254), 0), equalTo((255 << 8) + 254));
	}

	@Test
	public void testMap38() throws Exception {
		QGramMapping mapper = new QGramMapping(3, 8);

		assertThat(mapper.map(chars(1, 2, 3), 0), equalTo((1 << 16) + (2 << 8) + 3));
		assertThat(mapper.map(chars(1, 4, 3, 2), 1), equalTo((4 << 16) + (3 << 8) + 2));
		assertThat(mapper.map(chars(255, 254, 253), 0), equalTo((255 << 16) + (254 << 8) + 253));
	}

	@Test
	public void testMap24() throws Exception {
		QGramMapping mapper = new QGramMapping(2, 4);

		assertThat(mapper.map(chars(1, 2), 0), equalTo((1 << 4) + 2));
		assertThat(mapper.map(chars(1,2,4, 3), 2), equalTo((4 << 4) + 3));
		assertThat(mapper.map(chars(255, 254), 0), equalTo((15 << 4) + 14));
	}

	@Test
	public void testMap34() throws Exception {
		QGramMapping mapper = new QGramMapping(3, 4);

		assertThat(mapper.map(chars(1, 2, 3), 0), equalTo((1 << 8) + (2 << 4) + 3));
		assertThat(mapper.map(chars(1, 4, 3, 2), 1), equalTo((4 << 8) + (3 << 4) + 2));
		assertThat(mapper.map(chars(255, 254, 253), 0), equalTo((15 << 8) + (14 << 4) + 13));
	}

	@Test
	public void testMapCharMapping28() throws Exception {
		QGramMapping mapper = new QGramMapping(2, 8);

		assertThat(mapper.map(chars(1, 2), 0, doubleMapping()), intArrayContaining(
			(1 << 8) + 2,
			(1 << 8) + (2 << 1),
			(1 << 9) + 2,
			(1 << 9) + (2 << 1)));
		assertThat(mapper.map(chars(255, 254), 0, doubleMapping()), PrimitiveArrayMatcher.intArrayContaining(
			(255 << 8) + 254,
			(255 << 8) + 252,
			(254 << 8) + 254,
			(254 << 8) + 252
			));
	}

	@Test
	public void testMapCharMapping24() throws Exception {
		QGramMapping mapper = new QGramMapping(2, 4);

		assertThat(mapper.map(chars(1, 2), 0, doubleMapping()), intArrayContaining(
			(1 << 4) + 2,
			(1 << 4) + (2 << 1),
			(1 << 5) + 2,
			(1 << 5) + (2 << 1)));
		assertThat(mapper.map(chars(255, 254), 0, doubleMapping()), PrimitiveArrayMatcher.intArrayContaining(
			(15 << 4) + 14,
			(15 << 4) + 12,
			(14 << 4) + 14,
			(14 << 4) + 12
			));
	}

	private CharMapping doubleMapping() {
		return new CharMapping() {

			@Override
			public char[] normalized(char[] chars) {
				return chars;
			}

			@Override
			public char[] map(char c) {
				return new char[] { c, (char) (c << 1) };
			}
		};
	}

	private char[] chars(int... ints) {
		char[] chars = new char[ints.length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) ints[i];
		}
		return chars;
	}

}
