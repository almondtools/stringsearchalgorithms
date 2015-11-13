package com.almondtools.stringsandchars.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.stringsandchars.io.ReverseCharProvider;


public class ReverseCharProviderTest {

	@Test
	public void testNextAtBeginning() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.next(), equalTo('a'));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 3));
		assertThat(provider.next(), equalTo('b'));
	}
	
	@Test
	public void testNextConsumes() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 3));
		assertThat(provider.prev(), equalTo('a'));
	}
	
	@Test
	public void testPrevAtEnd() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.prev(), equalTo('d'));
	}
	
	@Test
	public void testPrevConsumes() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.prev(), equalTo('d'));
		assertThat(provider.prev(), equalTo('c'));
	}
	
	@Test
	public void testFinishedAtBeginning() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedInMiddle() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 3));
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedAtEnd() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.finished(), is(true));
	}
	
	@Test
	public void testLookahead() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.lookahead(), equalTo('a'));
	}
	
	@Test
	public void testLookaheadWithN() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.lookahead(0), equalTo('a'));
		assertThat(provider.lookahead(1), equalTo('b'));
		assertThat(provider.lookahead(2), equalTo('c'));
	}
	
	@Test
	public void testLookbehind() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.lookbehind(), equalTo('d'));
	}
	
	@Test
	public void testLookbehindWithN() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.lookbehind(0), equalTo('d'));
		assertThat(provider.lookbehind(1), equalTo('c'));
		assertThat(provider.lookbehind(2), equalTo('b'));
	}

	@Test
	public void testCurrent() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		assertThat(provider.current(), equalTo(2));
	}
	
	@Test
	public void testMoveAndCurrent() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		provider.move(3);
		assertThat(provider.current(), equalTo(3));
	}
	
	@Test
	public void testAtDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.at(2), equalTo('b'));
		assertThat(provider.current(), equalTo(4));
	}
	
	@Test
	public void testSliceDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.slice(3, 1), equalTo("bc"));
		assertThat(provider.current(), equalTo(4));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.between(3, 1), equalTo(new char[]{'b','c'}));
		assertThat(provider.current(), equalTo(4));
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.toString(), equalTo("|abcd"));
	}
	
	@Test
	public void testToStringInMiddle() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 3));
		assertThat(provider.toString(), equalTo("a|bcd"));
	}
	
	@Test
	public void testToStringAtEnd() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 0));
		assertThat(provider.toString(), equalTo("abcd|"));
	}
	
}
