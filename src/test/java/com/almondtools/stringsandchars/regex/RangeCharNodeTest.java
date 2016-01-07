package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
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
public class RangeCharNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetFrom() throws Exception {
		assertThat(new RangeCharNode('b','d').getFrom(), equalTo('b'));
	}

	@Test
	public void testGetTo() throws Exception {
		assertThat(new RangeCharNode('b','d').getTo(), equalTo('d'));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitRangeChar(any(RangeCharNode.class))).thenReturn("success");
		
		assertThat(new RangeCharNode('b','d').accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		RangeCharNode original = new RangeCharNode('b','d');
		RangeCharNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		RangeCharNode original = new RangeCharNode('b','d');
		RangeCharNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new RangeCharNode('b','d').toString(), equalTo("[b-d]"));
	}

	@Test
	public void testToInlinedString() throws Exception {
		assertThat(new RangeCharNode('b','d').toInlinedString(), equalTo("b-d"));
		assertThat(new RangeCharNode((char) 0,(char) 128).toInlinedString(), equalTo("\\u0000-\\u0080"));
	}

	@Test
	public void testSimplify() throws Exception {
		RangeCharNode notSimplifiable = new RangeCharNode('e', 'f');
		assertThat(notSimplifiable.simplify(), equalTo((RegexNode) notSimplifiable));
		assertThat(new RangeCharNode('e', 'e').simplify(), reflectiveEqualTo((DefinedCharNode) new SingleCharNode('e')));
	}

}
