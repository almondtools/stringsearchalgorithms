package net.amygdalum.stringsearchalgorithms.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.amygdalum.stringsearchalgorithms.io.OutOfBufferException;
import net.amygdalum.stringsearchalgorithms.io.ReaderBufferCharProvider;

public class ReaderBufferCharProviderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testNextAtBeginning() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.next(), equalTo('a'));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 4, 1);
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testNextConsumes() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testNextConsumesWithSmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 1);
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testNextConsumesWithVerySmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 0);
		assertThat(provider.next(), equalTo('a'));
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 4, 1);
		assertThat(provider.prev(), equalTo('a'));
	}

	@Test
	public void testPrevAtEnd() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.prev(), equalTo('d'));
	}

	@Test
	public void testPrevConsumes() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.prev(), equalTo('d'));
		assertThat(provider.prev(), equalTo('c'));
	}

	@Test
	public void testPrevConsumesWithSmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 1, 2);
		provider.prev();
		provider.prev();
	}

	@Test
	public void testPrevConsumesWithTooSmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 1, 1);
		provider.prev();
		
		thrown.expect(OutOfBufferException.class);
		
		provider.prev();
	}

	@Test
	public void testForwardAtBeginning() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		
		provider.forward(1);
		
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testForwardInMiddle() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 4, 1);
		
		provider.forward(1);
		
		assertThat(provider.next(), equalTo('c'));
	}

	@Test
	public void testForwardConsumesWithSmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 1);
		
		provider.forward(1);
		
		assertThat(provider.next(), equalTo('b'));
	}

	@Test
	public void testForwardConsumesWithVerySmallBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 0);

		provider.forward(2);
		
		assertThat(provider.next(), equalTo('c'));
	}

	@Test
	public void testFinishedAtBeginning() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedInMiddle() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 4, 1);
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedAtEnd() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testLookahead() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.lookahead(), equalTo('a'));
	}

	@Test
	public void testLookaheadWithN() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.lookahead(0), equalTo('a'));
		assertThat(provider.lookahead(1), equalTo('b'));
		assertThat(provider.lookahead(2), equalTo('c'));
	}

	@Test
	public void testLookbehind() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.lookbehind(), equalTo('d'));
	}

	@Test
	public void testLookbehindWithN() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.lookbehind(0), equalTo('d'));
		assertThat(provider.lookbehind(1), equalTo('c'));
		assertThat(provider.lookbehind(2), equalTo('b'));
	}

	@Test
	public void testCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 4, 1);
		assertThat(provider.current(), equalTo(2l));
	}

	@Test
	public void testMoveAndCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 4, 1);
		provider.move(3);
		
		assertThat(provider.current(), equalTo(3l));
	}

	@Test
	public void testMoveReverseAndCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 4, 1);
		provider.move(1);
		
		assertThat(provider.current(), equalTo(1l));
	}

	@Test
	public void testMoveNotAndCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		provider.move(2);
		
		assertThat(provider.current(), equalTo(2l));
	}

	@Test
	public void testMoveBeyondReverseBufferAndCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		thrown.expect(OutOfBufferException.class);
		provider.move(1);
	}

	@Test
	public void testMoveBeyondBufferAndCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 0);
		provider.move(3);
		
		assertThat(provider.current(), equalTo(3l));
	}

	@Test
	public void testAtDoesNotConsume() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.at(2), equalTo('c'));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testAtOnCurrent() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		assertThat(provider.at(2), equalTo('c'));
	}

	@Test
	public void testAtInRange() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 1);
		
		assertThat(provider.at(3), equalTo('d'));
	}

	@Test
	public void testAtFailsIfTooLongBackward() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.at(1);
	}

	@Test
	public void testAtFailsIfTooLongForward() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 0);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.at(1);
	}

	@Test
	public void testSliceDoesNotConsume() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.slice(1, 3), equalTo("bc"));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.between(1, 3), equalTo(new char[] { 'b', 'c' }));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testBetweenInBufferRange() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 1, 2);
		assertThat(provider.between(2, 3), equalTo(new char[] { 'c' }));
		assertThat(provider.current(), equalTo(1l));
	}

	@Test
	public void testBetweenToLarge() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.between(1, 3);
	}

	@Test
	public void testBetweenToFar() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 1, 1);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.between(2,3);
	}

	@Test
	public void testBetweenToFarBack() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.between(0,1);
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 0, 4, 1);
		assertThat(provider.toString(), equalTo("...|abcd"));
	}

	@Test
	public void testToStringInMiddle() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 1, 4, 1);
		assertThat(provider.toString(), equalTo("...a|bcd"));
	}

	@Test
	public void testToStringAtEnd() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 4, 1);
		assertThat(provider.toString(), equalTo("...abcd|"));
	}

	@Test
	public void testReverseBufferToSmall() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 3, 1, 1);
		provider.prev();

		thrown.expect(OutOfBufferException.class);

		provider.prev();
	}

	@Test
	public void testNoReverseBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 3, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.prev();
	}

	@Test
	public void testChangedWithoutChange() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedWithChange() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		provider.mark();
		
		provider.forward(1);
		
		assertThat(provider.changed(), is(true));
	}

	@Test
	public void testChangedWithTemporaryChange() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		provider.mark();
		
		provider.next();
		provider.prev();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedDoubleCall() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		provider.mark();
		
		provider.forward(1);
		provider.changed();
		
		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testFinish() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 1, 0);
		
		provider.finish();
		
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinishedLookahead() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 4, 1);
		
		assertThat(provider.finished(1), is(false));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedLookaheadRepeatedly() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 4, 1);
		
		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedLookaheadOutOfBuffer() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 3, 0);
		
		thrown.expect(OutOfBufferException.class);
		
		provider.finished(2);
	}

	@Test
	public void testFinishedLookaheadNotChangesState() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 2, 2, 1);
		
		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(1), is(false));
	}
	
	@Test
	public void testFinishedLookaheadBeforeEnd() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 1, 1);
		
		provider.prev();
		
		assertThat(provider.finished(1), is(true));
	}
	
	@Test
	public void testFinishedLookaheadAtEnd() throws Exception {
		ReaderBufferCharProvider provider = new ReaderBufferCharProvider(new StringReader("abcd"), 4, 1, 1);
		
		assertThat(provider.finished(0), is(true));
		assertThat(provider.finished(1), is(true));
	}

}
