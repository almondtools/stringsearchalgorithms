package com.almondtools.stringsandchars.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.StringCharProvider;


public class StringCharProviderTest {

	@Test
	public void testNextAtBeginning() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.next(), equalTo('a'));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 1);
		assertThat(provider.next(), equalTo('b'));
	}
	
	@Test
	public void testNextConsumes() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 1);
		assertThat(provider.prev(), equalTo('a'));
	}
	
	@Test
	public void testPrevAtEnd() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.prev(), equalTo('d'));
	}
	
	@Test
	public void testPrevConsumes() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.prev(), equalTo('d'));
		assertThat(provider.prev(), equalTo('c'));
	}
	
	@Test
	public void testFinishedAtBeginning() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedInMiddle() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 1);
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedAtEnd() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.finished(), is(true));
	}
	
	@Test
	public void testLookahead() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.lookahead(), equalTo('a'));
	}
	
	@Test
	public void testLookaheadWithN() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.lookahead(0), equalTo('a'));
		assertThat(provider.lookahead(1), equalTo('b'));
		assertThat(provider.lookahead(2), equalTo('c'));
	}
	
	@Test
	public void testLookbehind() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.lookbehind(), equalTo('d'));
	}
	
	@Test
	public void testLookbehindWithN() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.lookbehind(0), equalTo('d'));
		assertThat(provider.lookbehind(1), equalTo('c'));
		assertThat(provider.lookbehind(2), equalTo('b'));
	}

	@Test
	public void testCurrent() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 2);
		assertThat(provider.current(), equalTo(2));
	}
	
	@Test
	public void testMoveAndCurrent() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 2);
		provider.move(3);
		assertThat(provider.current(), equalTo(3));
	}
	
	@Test
	public void testAtDoesNotConsume() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.at(2), equalTo('c'));
		assertThat(provider.current(), equalTo(0));
	}
	
	@Test
	public void testSliceDoesNotConsume() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.slice(1, 3), equalTo("bc"));
		assertThat(provider.current(), equalTo(0));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.between(1, 3), equalTo(new char[]{'b','c'}));
		assertThat(provider.current(), equalTo(0));
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 0);
		assertThat(provider.toString(), equalTo("|abcd"));
	}
	
	@Test
	public void testToStringInMiddle() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 1);
		assertThat(provider.toString(), equalTo("a|bcd"));
	}
	
	@Test
	public void testToStringAtEnd() throws Exception {
		StringCharProvider provider = new StringCharProvider("abcd", 4);
		assertThat(provider.toString(), equalTo("abcd|"));
	}
	
}
