package net.amygdalum.util.text;

import static com.almondtools.conmatch.conventions.UtilityClassMatcher.isUtilityClass;
import static java.util.Arrays.asList;
import static net.amygdalum.util.text.CharUtils.after;
import static net.amygdalum.util.text.CharUtils.before;
import static net.amygdalum.util.text.CharUtils.charToString;
import static net.amygdalum.util.text.CharUtils.computeMaxChar;
import static net.amygdalum.util.text.CharUtils.computeMinChar;
import static net.amygdalum.util.text.CharUtils.lastIndexOf;
import static net.amygdalum.util.text.CharUtils.maxLength;
import static net.amygdalum.util.text.CharUtils.minLength;
import static net.amygdalum.util.text.CharUtils.revert;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class CharUtilsTest {
	
	@Test
	public void testCharUtils() throws Exception {
		assertThat(CharUtils.class, isUtilityClass());
	}

	@Test
	public void testCharToStringForPrintableCharacters() throws Exception {
		assertThat(charToString(' '), equalTo(" "));
		assertThat(charToString('~'), equalTo("~"));
	}

	@Test
	public void testCharToStringForNonPrintableCharacters() throws Exception {
		assertThat(charToString(before(' ')), equalTo("\\u001f"));
		assertThat(charToString(after('~')), equalTo("\\u007f"));
	}

	@Test
	public void testAfter() throws Exception {
		assertThat(after('a'), equalTo('b'));
	}

	@Test
	public void testBefore() throws Exception {
		assertThat(before('b'), equalTo('a'));
	}

	@Test
	public void testComputeMinChar() throws Exception {
		assertThat(computeMinChar(new char[]{'a','b'}), equalTo('a'));
		assertThat(computeMinChar(new char[]{'b','a'}), equalTo('a'));
		assertThat(computeMinChar(asList(new char[]{'b','c'}, new char[]{'a','d'})), equalTo('a'));
	}

	@Test
	public void testComputeMaxChar() throws Exception {
		assertThat(computeMaxChar(new char[]{'a','b'}), equalTo('b'));
		assertThat(computeMaxChar(new char[]{'b','a'}), equalTo('b'));
		assertThat(computeMaxChar(asList(new char[]{'a','d'}, new char[]{'b','c'})), equalTo('d'));
		assertThat(computeMaxChar(asList(new char[]{'b','c'}, new char[]{'a','d'})), equalTo('d'));
	}

	@Test
	public void testMinMaxLength() throws Exception {
		assertThat(minLength(asList(new char[]{'a','b'}, new char[]{'a','b','c'})), equalTo(2));
		assertThat(maxLength(asList(new char[]{'a','b'}, new char[]{'a','b','c'})), equalTo(3));
	}

	@Test
	public void testLastIndexOf() throws Exception {
		assertThat(lastIndexOf(new char[]{}, new char[]{1}), equalTo(-1));
		assertThat(lastIndexOf(new char[]{0}, new char[]{1}), equalTo(-1));
		assertThat(lastIndexOf(new char[]{0}, new char[]{0}), equalTo(0));
		assertThat(lastIndexOf(new char[]{0,0}, new char[]{0}), equalTo(1));
		assertThat(lastIndexOf(new char[]{0,1}, new char[]{0}), equalTo(0));
		assertThat(lastIndexOf(new char[]{1,1}, new char[]{0}), equalTo(-1));
	}

	@Test
	public void testRevert() throws Exception {
		assertThat(revert("AB".toCharArray()), equalTo("BA".toCharArray()));
	}

}
