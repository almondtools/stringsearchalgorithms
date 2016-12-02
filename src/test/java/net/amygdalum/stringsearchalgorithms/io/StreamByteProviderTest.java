package net.amygdalum.stringsearchalgorithms.io;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StreamByteProviderTest {

	private static final byte a = (byte) 'a';
	private static final byte b = (byte) 'b';
	private static final byte c = (byte) 'c';
	private static final byte d = (byte) 'd';

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	InputStream I;

	@Test
	public void testNextAtBeginning() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.next(), equalTo(a));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 4, 1);
		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testNextConsumes() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.next(), equalTo(a));
		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testNextConsumesWithSmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 1);
		assertThat(provider.next(), equalTo(a));
		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testNextConsumesWithVerySmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 0);
		assertThat(provider.next(), equalTo(a));
		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 4, 1);
		assertThat(provider.prev(), equalTo(a));
	}

	@Test
	public void testPrevAtEnd() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 4, 1);
		assertThat(provider.prev(), equalTo(d));
	}

	@Test
	public void testPrevConsumes() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 4, 1);
		assertThat(provider.prev(), equalTo(d));
		assertThat(provider.prev(), equalTo(c));
	}

	@Test
	public void testPrevConsumesWithSmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 1, 2);
		provider.prev();
		provider.prev();
	}

	@Test
	public void testPrevConsumesWithTooSmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 1, 1);
		provider.prev();

		thrown.expect(OutOfBufferException.class);

		provider.prev();
	}

	@Test
	public void testForwardAtBeginning() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);

		provider.forward(1);

		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testForwardInMiddle() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 4, 1);

		provider.forward(1);

		assertThat(provider.next(), equalTo(c));
	}

	@Test
	public void testForwardConsumesWithSmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 1);

		provider.forward(1);

		assertThat(provider.next(), equalTo(b));
	}

	@Test
	public void testForwardConsumesWithVerySmallBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 0);

		provider.forward(2);

		assertThat(provider.next(), equalTo(c));
	}

	@Test
	public void testFinishedAtBeginning() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedInMiddle() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 4, 1);
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedAtEnd() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 4, 1);
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinishedAfterConsuming() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 2, 1);
		
		provider.next();
		provider.next();
		provider.next();
		
		assertThat(provider.finished(), is(true));
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinishedInMiddleOfBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abc".getBytes(UTF_8)), 2, 2, 1);
		provider.next();
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testLookahead() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.lookahead(), equalTo(a));
	}

	@Test
	public void testLookaheadWithN() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.lookahead(0), equalTo(a));
		assertThat(provider.lookahead(1), equalTo(b));
		assertThat(provider.lookahead(2), equalTo(c));
	}

	@Test
	public void testLookbehind() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 4, 1);
		assertThat(provider.lookbehind(), equalTo(d));
	}

	@Test
	public void testLookbehindWithN() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 4, 1);
		assertThat(provider.lookbehind(0), equalTo(d));
		assertThat(provider.lookbehind(1), equalTo(c));
		assertThat(provider.lookbehind(2), equalTo(b));
	}

	@Test
	public void testCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 4, 1);
		assertThat(provider.current(), equalTo(2l));
	}

	@Test
	public void testMoveAndCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 4, 1);
		provider.move(3);

		assertThat(provider.current(), equalTo(3l));
	}

	@Test
	public void testMoveReverseAndCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 4, 1);
		provider.move(1);

		assertThat(provider.current(), equalTo(1l));
	}

	@Test
	public void testMoveNotAndCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);
		provider.move(2);

		assertThat(provider.current(), equalTo(2l));
	}

	@Test
	public void testMoveBeyondReverseBufferAndCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		thrown.expect(OutOfBufferException.class);
		provider.move(1);
	}

	@Test
	public void testMoveBeyondBufferAndCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 0);
		provider.move(3);

		assertThat(provider.current(), equalTo(3l));
	}

	@Test
	public void testAtDoesNotConsume() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.at(2), equalTo(c));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testAtOnCurrent() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		assertThat(provider.at(2), equalTo(c));
	}

	@Test
	public void testAtInRange() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 1);

		assertThat(provider.at(3), equalTo(d));
	}

	@Test
	public void testAtFailsIfTooLongBackward() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.at(1);
	}

	@Test
	public void testAtFailsIfTooLongForward() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.at(1);
	}

	@Test
	public void testSliceDoesNotConsume() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), UTF_8, 0, 4, 1);
		assertThat(provider.slice(1, 3).getString(), equalTo("bc"));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 4, 1);
		assertThat(provider.between(1, 3), equalTo(new byte[] { b, c }));
		assertThat(provider.current(), equalTo(0l));
	}

	@Test
	public void testBetweenInBufferRange() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 1, 1, 2);
		assertThat(provider.between(2, 3), equalTo(new byte[] { c }));
		assertThat(provider.current(), equalTo(1l));
	}

	@Test
	public void testBetweenToLarge() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.between(1, 3);
	}

	@Test
	public void testBetweenToFar() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 0, 1, 1);

		thrown.expect(OutOfBufferException.class);

		provider.between(2, 3);
	}

	@Test
	public void testBetweenToFarBack() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.between(0, 1);
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), UTF_8, 0, 4, 1);
		assertThat(provider.toString(), equalTo("...|abcd"));
	}

	@Test
	public void testToStringInMiddle() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)),UTF_8, 1, 4, 1);
		assertThat(provider.toString(), equalTo("...a|bcd"));
	}

	@Test
	public void testToStringAtEnd() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), UTF_8, 4, 4, 1);
		assertThat(provider.toString(), equalTo("...abcd|"));
	}

	@Test
	public void testReverseBufferToSmall() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 3, 1, 1);
		provider.prev();

		thrown.expect(OutOfBufferException.class);

		provider.prev();
	}

	@Test
	public void testNoReverseBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 3, 1, 0);

		thrown.expect(OutOfBufferException.class);

		provider.prev();
	}

	@Test
	public void testChangedWithoutChange() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedWithChange() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		provider.mark();

		provider.forward(1);

		assertThat(provider.changed(), is(true));
	}

	@Test
	public void testChangedWithTemporaryChange() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		provider.mark();

		provider.next();
		provider.prev();

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedDoubleCall() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		provider.mark();

		provider.forward(1);
		provider.changed();

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testFinish() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 1, 0);

		provider.finish();

		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinishedLookahead() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 4, 1);

		assertThat(provider.finished(1), is(false));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedLookaheadRepeatedly() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 4, 1);

		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedLookaheadOutOfBuffer() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 3, 0);

		thrown.expect(OutOfBufferException.class);

		provider.finished(2);
	}

	@Test
	public void testFinishedLookaheadNotChangesState() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 2, 2, 1);

		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(1), is(false));
	}

	@Test
	public void testFinishedLookaheadBeforeEnd() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 1, 1);

		provider.prev();

		assertThat(provider.finished(1), is(true));
	}

	@Test
	public void testFinishedLookaheadAtEnd() throws Exception {
		StreamByteProvider provider = new StreamByteProvider(new ByteArrayInputStream("abcd".getBytes(UTF_8)), 4, 1, 1);

		assertThat(provider.finished(0), is(true));
		assertThat(provider.finished(1), is(true));
	}

}
