package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static com.almondtools.stringsandchars.regex.BoundedLoopNode.bounded;
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
public class BoundedLoopNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetSubNode() throws Exception {
		assertThat(bounded(new SingleCharNode('a'), 1, 2).getSubNode(), reflectiveEqualTo((RegexNode) new SingleCharNode('a')));
	}

	@Test
	public void testGetFrom() throws Exception {
		assertThat(bounded(new SingleCharNode('a'), 1, 2).getFrom(), equalTo(1));
	}

	@Test
	public void testGetTo() throws Exception {
		assertThat(bounded(new SingleCharNode('a'), 1, 2).getTo(), equalTo(2));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitBoundedLoop(any(BoundedLoopNode.class))).thenReturn("success");

		assertThat(bounded(new SingleCharNode('a'), 1, 2).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		BoundedLoopNode original = bounded(new SingleCharNode('a'), 1, 2);
		BoundedLoopNode cloned = original.clone();

		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		BoundedLoopNode original = bounded(new SingleCharNode('a'), 1, 2);
		BoundedLoopNode cloned = original.clone();

		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(bounded(new SingleCharNode('a'), 1, 2).toString(), equalTo("a{1,2}"));
		assertThat(bounded(new SingleCharNode('a'), 1, 1).toString(), equalTo("a{1}"));
	}

}
