package net.amygdalum.util.text;

import static com.almondtools.conmatch.conventions.UtilityClassMatcher.isUtilityClass;
import static java.util.Arrays.asList;
import static net.amygdalum.util.text.ByteUtils.lastIndexOf;
import static net.amygdalum.util.text.ByteUtils.maxLength;
import static net.amygdalum.util.text.ByteUtils.minLength;
import static net.amygdalum.util.text.ByteUtils.revert;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ByteUtilsTest {

	@Test
	public void testByteUtils() throws Exception {
		assertThat(ByteUtils.class, isUtilityClass());
	}

	@Test
	public void testMinMaxLength() throws Exception {
		assertThat(minLength(asList(new byte[] { 1, 2 }, new byte[] { 1, 2, 3 })), equalTo(2));
		assertThat(maxLength(asList(new byte[] { 1, 2 }, new byte[] { 1, 2, 3 })), equalTo(3));
	}

	@Test
	public void testLastIndexOf() throws Exception {
		assertThat(lastIndexOf(new byte[]{}, new byte[]{1}), equalTo(-1));
		assertThat(lastIndexOf(new byte[]{0}, new byte[]{1}), equalTo(-1));
		assertThat(lastIndexOf(new byte[]{0}, new byte[]{0}), equalTo(0));
		assertThat(lastIndexOf(new byte[]{0,0}, new byte[]{0}), equalTo(1));
		assertThat(lastIndexOf(new byte[]{0,1}, new byte[]{0}), equalTo(0));
		assertThat(lastIndexOf(new byte[]{1,1}, new byte[]{0}), equalTo(-1));
	}

	@Test
	public void testRevert() throws Exception {
		assertThat(revert(new byte[] { 0, 1 }), equalTo(new byte[] { 1, 0 }));
	}

}
