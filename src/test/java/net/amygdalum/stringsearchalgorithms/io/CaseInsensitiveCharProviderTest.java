package net.amygdalum.stringsearchalgorithms.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class CaseInsensitiveCharProviderTest {

	@Test
	public void testNextAtBeginning() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 0));
		assertThat(provider.next(), equalTo('a'));
	}

	@Test
	public void testNextAtBeginningLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 0));
		assertThat(provider.next(), equalTo('a'));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 1));
		assertThat(provider.next(), equalTo('b'));
	}
	
	@Test
	public void testNextInMiddleLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("aBcd", 1));
		assertThat(provider.next(), equalTo('b'));
	}
	
	@Test
	public void testNextConsumes() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("aBcd", 0));
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 1));
		assertThat(provider.prev(), equalTo('a'));
	}
	
	@Test
	public void testPrevInMiddleLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 1));
		assertThat(provider.prev(), equalTo('a'));
	}
	
	@Test
	public void testPrevAtEnd() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 4));
		assertThat(provider.prev(), equalTo('d'));
	}
	
	@Test
	public void testPrevAtEndLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcD", 4));
		assertThat(provider.prev(), equalTo('d'));
	}
	
	@Test
	public void testPrevConsumes() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcD", 4));
		assertThat(provider.prev(), equalTo('d'));
		assertThat(provider.prev(), equalTo('c'));
	}
	
	@Test
	public void testFinishedAtBeginning() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 0));
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedInMiddle() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 1));
		assertThat(provider.finished(), is(false));
	}
	
	@Test
	public void testFinishedAtEnd() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 4));
		assertThat(provider.finished(), is(true));
	}
	
	@Test
	public void testLookahead() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 0));
		assertThat(provider.lookahead(), equalTo('a'));
	}
	
	@Test
	public void testLookaheadLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 0));
		assertThat(provider.lookahead(), equalTo('a'));
	}
	
	@Test
	public void testLookaheadWithN() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("aBcd", 0));
		assertThat(provider.lookahead(0), equalTo('a'));
		assertThat(provider.lookahead(1), equalTo('b'));
		assertThat(provider.lookahead(2), equalTo('c'));
	}
	
	@Test
	public void testLookbehind() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 4));
		assertThat(provider.lookbehind(), equalTo('d'));
	}
	
	@Test
	public void testLookbehindLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcD", 4));
		assertThat(provider.lookbehind(), equalTo('d'));
	}
	
	@Test
	public void testLookbehindWithN() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abCd", 4));
		assertThat(provider.lookbehind(0), equalTo('d'));
		assertThat(provider.lookbehind(1), equalTo('c'));
		assertThat(provider.lookbehind(2), equalTo('b'));
	}

	@Test
	public void testCurrent() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		assertThat(provider.current(), equalTo(2l));
	}
	
	@Test
	public void testMoveAndCurrent() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		provider.move(3);
		assertThat(provider.current(), equalTo(3l));
	}
	
	@Test
	public void testAtDoesNotConsume() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcD", 0));
		assertThat(provider.at(2), equalTo('c'));
		assertThat(provider.current(), equalTo(0l));
		assertThat(provider.at(3), equalTo('d'));
	}
	
	@Test
	public void testSliceIsNotCaseInsensitive() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("aBcd", 0));
		assertThat(provider.slice(1, 3), equalTo("Bc"));
	}

	@Test
	public void testSliceDoesNotConsume() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 0));
		assertThat(provider.slice(1, 3), equalTo("bc"));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testBetweenLC() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("aBcd", 0));
		assertThat(provider.between(1, 3), equalTo(new char[]{'b','c'}));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 0));
		assertThat(provider.between(1, 3), equalTo(new char[]{'b','c'}));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 0));
		assertThat(provider.toString(), equalTo("|Abcd"));
	}
	
	@Test
	public void testToStringInMiddle() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 1));
		assertThat(provider.toString(), equalTo("A|bcd"));
	}
	
	@Test
	public void testToStringAtEnd() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("Abcd", 4));
		assertThat(provider.toString(), equalTo("Abcd|"));
	}
	
	@Test
	public void testChangedWithoutChange() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedWithChange() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		provider.mark();
		
		provider.forward(1);
		
		assertThat(provider.changed(), is(true));
	}

	@Test
	public void testChangedWithTemporaryChange() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		provider.mark();
		
		provider.next();
		provider.prev();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedDoubleCall() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		provider.mark();
		
		provider.forward(1);
		provider.changed();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testFinish() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		provider.finish();
		
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinished() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		assertThat(provider.finished(1), is(false));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedNotChangesState() throws Exception {
		CaseInsensitiveCharProvider provider = new CaseInsensitiveCharProvider(new StringCharProvider("abcd", 2));
		
		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(1), is(false));
	}

}
