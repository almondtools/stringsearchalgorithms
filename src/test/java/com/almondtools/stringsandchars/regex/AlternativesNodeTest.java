package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static com.almondtools.stringsandchars.regex.AlternativesNode.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
public class AlternativesNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testAnyOf() throws Exception {
		assertThat(anyOf(new SingleCharNode('a'), new SingleCharNode('b')).toString(), equalTo("a|b"));
	}

	@Test
	public void testAnyOfEmpty() throws Exception {
		assertThat(anyOf().toString(), equalTo(""));
	}

	@Test
	public void testAnyOfAlternativesNodesAreInlined() throws Exception {
		assertThat(anyOf(new SingleCharNode('a'), anyOf(new SingleCharNode('b'), new SingleCharNode('c'))).toString(), equalTo("a|b|c"));
		assertThat(anyOf(anyOf(new SingleCharNode('a'), new SingleCharNode('b')), new SingleCharNode('c')).toString(), equalTo("a|b|c"));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitAlternatives(any(AlternativesNode.class))).thenReturn("success");
		
		assertThat(anyOf(new SingleCharNode('a'), new SingleCharNode('b')).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		AlternativesNode original = anyOf(new SingleCharNode('a'), new SingleCharNode('b'));
		AlternativesNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsDeep() throws Exception {
		AlternativesNode original = anyOf(new SingleCharNode('a'), new SingleCharNode('b'));
		AlternativesNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
		
		assertThat(cloned.getSubNodes(), not(equalTo(original.getSubNodes())));
		
		original.getSubNodes().clear();
		
		assertThat(cloned.getSubNodes(), hasSize(2));
	}

	@Test
	public void testSimplify() throws Exception {
		AlternativesNode notSimplifiable = anyOf(new SingleCharNode('a'), new SingleCharNode('b'));
		assertThat(notSimplifiable.simplify(), equalTo((RegexNode) notSimplifiable));
		assertThat(anyOf(new SingleCharNode('c')).simplify(), reflectiveEqualTo((RegexNode) new SingleCharNode('c')));
		assertThat(anyOf().simplify(), reflectiveEqualTo((RegexNode) new EmptyNode()));
	}

}
