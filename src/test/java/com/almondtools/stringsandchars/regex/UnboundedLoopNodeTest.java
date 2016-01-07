package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static com.almondtools.stringsandchars.regex.UnboundedLoopNode.plus;
import static com.almondtools.stringsandchars.regex.UnboundedLoopNode.star;
import static com.almondtools.stringsandchars.regex.UnboundedLoopNode.unbounded;
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
public class UnboundedLoopNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetSubNode() throws Exception {
		assertThat(star(new SingleCharNode('a')).getSubNode(), reflectiveEqualTo((RegexNode) new SingleCharNode('a')));
	}

	@Test
	public void testGetFrom() throws Exception {
		assertThat(star(new SingleCharNode('a')).getFrom(), equalTo(0));
		assertThat(plus(new SingleCharNode('a')).getFrom(), equalTo(1));
		assertThat(unbounded(new SingleCharNode('a'), 4).getFrom(), equalTo(4));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitUnboundedLoop(any(UnboundedLoopNode.class))).thenReturn("success");

		assertThat(star(new SingleCharNode('a')).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		UnboundedLoopNode original = star(new SingleCharNode('a'));
		UnboundedLoopNode cloned = original.clone();

		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		UnboundedLoopNode original = star(new SingleCharNode('a'));
		UnboundedLoopNode cloned = original.clone();

		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(star(new SingleCharNode('a')).toString(), equalTo("a*"));
		assertThat(plus(new SingleCharNode('a')).toString(), equalTo("a+"));
		assertThat(unbounded(new SingleCharNode('a'), 4).toString(), equalTo("a{4,}"));
	}

}
