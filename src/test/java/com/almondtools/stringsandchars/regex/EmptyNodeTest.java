package com.almondtools.stringsandchars.regex;

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
public class EmptyNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetLiteralValue() throws Exception {
		assertThat(new EmptyNode().getLiteralValue(), equalTo(""));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitEmpty(any(EmptyNode.class))).thenReturn("success");
		
		assertThat(new EmptyNode().accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		EmptyNode original = new EmptyNode();
		EmptyNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		EmptyNode original = new EmptyNode();
		EmptyNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new EmptyNode().toString(), equalTo(""));
	}

}
