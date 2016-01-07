package com.almondtools.util.text;

import static com.almondtools.conmatch.conventions.UtilityClassMatcher.isUtilityClass;
import static com.almondtools.util.text.CharUtils.after;
import static com.almondtools.util.text.CharUtils.before;
import static com.almondtools.util.text.CharUtils.charToString;
import static com.almondtools.util.text.CharUtils.computeMaxChar;
import static com.almondtools.util.text.CharUtils.computeMinChar;
import static java.util.Arrays.asList;
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

}
