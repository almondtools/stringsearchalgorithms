package net.amygdalum.util.text;

import static com.almondtools.conmatch.conventions.UtilityClassMatcher.isUtilityClass;
import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.amygdalum.util.text.StringUtils.join;
import static net.amygdalum.util.text.StringUtils.reverse;
import static net.amygdalum.util.text.StringUtils.toCharArray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StringUtilsTest {
	
	@Test
	public void testStringUtils() throws Exception {
		assertThat(StringUtils.class, isUtilityClass());
	}

	@Test
	public void testReverse() throws Exception {
		assertThat(reverse("CBA"), equalTo("ABC"));
	}

	@Test
	public void testJoinOnEmptyList() throws Exception {
		assertThat(join(emptyList()), equalTo(""));
	}

	@Test
	public void testJoin() throws Exception {
		assertThat(join(asList("A", "B", "C")), equalTo("ABC"));
	}

	@Test
	public void testJoinWithSeparatorChar() throws Exception {
		assertThat(join(asList("A", "B", "C"), '-'), equalTo("A-B-C"));
	}

	@Test
	public void testJoinWithSeparatorCharOnSingleElementList() throws Exception {
		assertThat(join(asList("A"), '-'), equalTo("A"));
	}

	@Test
	public void testJoinWithSeparatorCharOnEmptyList() throws Exception {
		assertThat(join(emptyList(), '-'), equalTo(""));
	}

	@Test
	public void testJoinWithSeparatorString() throws Exception {
		assertThat(join(asList("A", "B", "C"), ", "), equalTo("A, B, C"));
	}

	@Test
	public void testJoinWithSeparatorStringOnSingleElementList() throws Exception {
		assertThat(join(asList("A"), ", "), equalTo("A"));
	}

	@Test
	public void testJoinWithSeparatorStringOnEmptyList() throws Exception {
		assertThat(join(emptyList(), ", "), equalTo(""));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testToCharArray() throws Exception {
		assertThat(toCharArray(asList("ab", "cd")), contains(charArrayContaining('a', 'b'), charArrayContaining('c','d')));
	}

}
