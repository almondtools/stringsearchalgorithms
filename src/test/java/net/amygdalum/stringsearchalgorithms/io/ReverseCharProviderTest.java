package net.amygdalum.stringsearchalgorithms.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.io.ReverseCharProvider;
import net.amygdalum.stringsearchalgorithms.io.StringCharProvider;


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
		assertThat(provider.current(), equalTo(2l));
	}
	
	@Test
	public void testMoveAndCurrent() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		provider.move(3);
		assertThat(provider.current(), equalTo(3l));
	}
	
	@Test
	public void testAtDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.at(2), equalTo('b'));
		assertThat(provider.current(), equalTo(4l));
	}
	
	@Test
	public void testSliceDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.slice(3, 1), equalTo("bc"));
		assertThat(provider.current(), equalTo(4l));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 4));
		assertThat(provider.between(3, 1), equalTo(new char[]{'b','c'}));
		assertThat(provider.current(), equalTo(4l));
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
	
	@Test
	public void testChangedWithoutChange() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedWithChange() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		provider.mark();
		
		provider.forward(1);
		
		assertThat(provider.changed(), is(true));
	}

	@Test
	public void testChangedWithTemporaryChange() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		provider.mark();
		
		provider.next();
		provider.prev();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedDoubleCall() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		provider.mark();
		
		provider.forward(1);
		provider.changed();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testFinish() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		provider.finish();
		
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinished() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		assertThat(provider.finished(1), is(false));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedNotChangesState() throws Exception {
		ReverseCharProvider provider = new ReverseCharProvider(new StringCharProvider("dcba", 2));
		
		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(1), is(false));
	}
	
}
