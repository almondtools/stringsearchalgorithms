package com.almondtools.stringsandchars.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static com.almondtools.util.text.CharUtils.after;
import static com.almondtools.util.text.CharUtils.before;
import static org.hamcrest.Matchers.contains;
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
public class CharClassNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testToCharNodes() throws Exception {
		assertThat(new CharClassNode(new SingleCharNode('d')).toCharNodes(), contains(reflectiveEqualTo((DefinedCharNode) new SingleCharNode('d'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInvert() throws Exception {
		assertThat(new CharClassNode(new SingleCharNode('e'), new SingleCharNode('f')).invert((char) 0, (char) 255).toCharNodes(), contains(
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode((char) 0, before('e'))), 
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode(after('f'), (char) 255))));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitCharClass(any(CharClassNode.class))).thenReturn("success");

		assertThat(new CharClassNode(new SingleCharNode('d')).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		CharClassNode original = new CharClassNode(new SingleCharNode('d'));
		CharClassNode cloned = original.clone();

		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		CharClassNode original = new CharClassNode(new SingleCharNode('d'));
		CharClassNode cloned = original.clone();

		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new CharClassNode(new SingleCharNode('d')).toString(), equalTo("[d]"));
	}

}
