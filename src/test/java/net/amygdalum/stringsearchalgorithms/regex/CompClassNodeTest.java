package net.amygdalum.stringsearchalgorithms.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static java.util.Arrays.asList;
import static net.amygdalum.util.text.CharUtils.before;
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

import net.amygdalum.stringsearchalgorithms.regex.CompClassNode;
import net.amygdalum.stringsearchalgorithms.regex.DefinedCharNode;
import net.amygdalum.stringsearchalgorithms.regex.RangeCharNode;
import net.amygdalum.stringsearchalgorithms.regex.RegexNodeVisitor;

@RunWith(MockitoJUnitRunner.class)
public class CompClassNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testToCharNodes() throws Exception {
		assertThat(new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255)))
			.toCharNodes(), contains(reflectiveEqualTo((DefinedCharNode) new RangeCharNode('e', (char) 255))));
	}

	@Test
	public void testInvert() throws Exception {
		assertThat(new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255)))
			.invert((char) 0, (char) 255).toCharNodes(), contains(
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode((char) 0, before('d')))));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitCompClass(any(CompClassNode.class))).thenReturn("success");

		assertThat(new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255)))
			.accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		CompClassNode original = new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255)));
		CompClassNode cloned = original.clone();

		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		CompClassNode original = new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255)));
		CompClassNode cloned = original.clone();

		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new CompClassNode(
			asList((DefinedCharNode) new RangeCharNode((char) 0, 'd')),
			asList((DefinedCharNode) new RangeCharNode('e', (char) 255))).toString(), equalTo("[^\\u0000-d]"));
	}

}
