package net.amygdalum.stringsearchalgorithms.regex;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SingleCharNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetValue() throws Exception {
		assertThat(new SingleCharNode('c').getValue(), equalTo('c'));
	}

	@Test
	public void testGetFrom() throws Exception {
		assertThat(new SingleCharNode('c').getFrom(), equalTo('c'));
	}

	@Test
	public void testGetTo() throws Exception {
		assertThat(new SingleCharNode('c').getTo(), equalTo('c'));
	}

	@Test
	public void testGetLiteralValue() throws Exception {
		assertThat(new SingleCharNode('c').getLiteralValue(), equalTo("c"));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitSingleChar(any(SingleCharNode.class))).thenReturn("success");
		
		assertThat(new SingleCharNode('c').accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		SingleCharNode original = new SingleCharNode('c');
		SingleCharNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		SingleCharNode original = new SingleCharNode('c');
		SingleCharNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new SingleCharNode('c').toString(), equalTo("c"));
	}

	@Test
	public void testToInlinedString() throws Exception {
		assertThat(new SingleCharNode('c').toInlinedString(), equalTo("c"));
		assertThat(new SingleCharNode((char) 0).toInlinedString(), equalTo("\\u0000"));
	}

}
