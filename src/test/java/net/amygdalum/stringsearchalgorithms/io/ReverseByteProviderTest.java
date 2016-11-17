package net.amygdalum.stringsearchalgorithms.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.io.ReverseByteProvider;
import net.amygdalum.stringsearchalgorithms.io.StringByteProvider;

public class ReverseByteProviderTest {

	@Test
	public void testNextAtBeginning() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 8));
		assertThat(provider.next(), equalTo((byte) ('a' >> 8)));
	}

	@Test
	public void testNextInMiddle() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 6));
		assertThat(provider.next(), equalTo((byte) ('b' >> 8)));
	}

	@Test
	public void testNextAtOddIndex() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 7));
		assertThat(provider.next(), equalTo((byte) ('a')));
	}

	@Test
	public void testNextConsumes() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 8));
		assertThat(provider.next(), equalTo((byte) ('a' >> 8)));
		assertThat(provider.next(), equalTo((byte) 'a'));
		assertThat(provider.next(), equalTo((byte) ('b' >> 8)));
		assertThat(provider.next(), equalTo((byte) 'b'));
	}

	@Test
	public void testPrevInMiddle() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 6));
		assertThat(provider.prev(), equalTo((byte) 'a'));
	}

	@Test
	public void testPrevAtOddIndex() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 7));
		assertThat(provider.prev(), equalTo((byte) ('a'>>8)));
	}

	@Test
	public void testPrevAtEnd() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.prev(), equalTo((byte) 'd'));
	}

	@Test
	public void testPrevConsumes() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.prev(), equalTo((byte) 'd'));
		assertThat(provider.prev(), equalTo((byte) ('d' >> 8)));
		assertThat(provider.prev(), equalTo((byte) 'c'));
		assertThat(provider.prev(), equalTo((byte) ('c' >> 8)));
	}

	@Test
	public void testFinishedAtBeginning() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 4));
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedInMiddle() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 3));
		assertThat(provider.finished(), is(false));
	}

	@Test
	public void testFinishedAtEnd() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testLookahead() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 8));
		assertThat(provider.lookahead(), equalTo((byte) ('a'>>8)));
	}

	@Test
	public void testLookaheadWithN() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 8));
		assertThat(provider.lookahead(0), equalTo((byte) ('a' >> 8)));
		assertThat(provider.lookahead(1), equalTo((byte) 'a'));
		assertThat(provider.lookahead(2), equalTo((byte) ('b' >> 8)));
		assertThat(provider.lookahead(3), equalTo((byte) 'b'));
		assertThat(provider.lookahead(4), equalTo((byte) ('c' >> 8)));
		assertThat(provider.lookahead(5), equalTo((byte) 'c'));
	}

	@Test
	public void testLookbehind() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.lookbehind(), equalTo((byte) 'd'));
	}

	@Test
	public void testLookbehindWithN() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.lookbehind(0), equalTo((byte) 'd'));
		assertThat(provider.lookbehind(1), equalTo((byte) ('d' >> 8)));
		assertThat(provider.lookbehind(2), equalTo((byte) 'c'));
		assertThat(provider.lookbehind(3), equalTo((byte) ('c' >> 8)));
		assertThat(provider.lookbehind(4), equalTo((byte) 'b'));
		assertThat(provider.lookbehind(5), equalTo((byte) ('b' >> 8)));
	}

	@Test
	public void testCurrent() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));
		assertThat(provider.current(), equalTo(2l));
	}

	@Test
	public void testMoveAndCurrent() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));
		provider.move(3);
		assertThat(provider.current(), equalTo(3l));
	}

	@Test
	public void testAtDoesNotConsume() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 4));
		assertThat(provider.at(4), equalTo((byte) 'b'));
		assertThat(provider.current(), equalTo(4l));
	}

	@Test
	public void testSliceDoesNotConsume() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 4));
		assertThat(provider.slice(6, 2), equalTo("bc"));
		assertThat(provider.current(), equalTo(4l));
	}

	@Test
	public void testBetweenDoesNotConsume() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 4));
		assertThat(provider.between(6, 2), equalTo(new byte[] { 'b' >> 8, 'b', 'c' >> 8 , 'c', }));
		assertThat(provider.current(), equalTo(4l));
	}

	@Test
	public void testToStringAtBeginning() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 8));
		assertThat(provider.toString(), equalTo("|abcd"));
	}

	@Test
	public void testToStringInMiddle() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 6));
		assertThat(provider.toString(), equalTo("a|bcd"));
	}

	@Test
	public void testToStringAtOddIndex() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 5));
		assertThat(provider.toString(), equalTo("a~|~cd"));
	}

	@Test
	public void testToStringAtEnd() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 0));
		assertThat(provider.toString(), equalTo("abcd|"));
	}

	@Test
	public void testChangedWithoutChange() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedWithChange() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		provider.mark();

		provider.forward(1);

		assertThat(provider.changed(), is(true));
	}

	@Test
	public void testChangedWithTemporaryChange() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		provider.mark();

		provider.next();
		provider.prev();

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testChangedDoubleCall() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		provider.mark();

		provider.forward(1);
		provider.changed();

		assertThat(provider.changed(), is(false));
	}

	@Test
	public void testFinish() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		provider.finish();

		assertThat(provider.finished(), is(true));
	}

	@Test
	public void testFinished() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		assertThat(provider.finished(1), is(false));
		assertThat(provider.finished(2), is(true));
	}

	@Test
	public void testFinishedNotChangesState() throws Exception {
		ReverseByteProvider provider = new ReverseByteProvider(new StringByteProvider("dcba", 2));

		assertThat(provider.finished(2), is(true));
		assertThat(provider.finished(1), is(false));
	}

}
