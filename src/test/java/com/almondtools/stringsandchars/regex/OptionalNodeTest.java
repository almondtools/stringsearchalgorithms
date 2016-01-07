package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static com.almondtools.stringsandchars.regex.OptionalNode.optional;
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
public class OptionalNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetSubNode() throws Exception {
		assertThat(optional(new SingleCharNode('a')).getSubNode(), reflectiveEqualTo((RegexNode) new SingleCharNode('a')));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitOptional(any(OptionalNode.class))).thenReturn("success");
		
		assertThat(optional(new SingleCharNode('a')).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		OptionalNode original = optional(new SingleCharNode('a'));
		OptionalNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		OptionalNode original = optional(new SingleCharNode('a'));
		OptionalNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(optional(new SingleCharNode('a')).toString(), equalTo("a?"));
	}

}
