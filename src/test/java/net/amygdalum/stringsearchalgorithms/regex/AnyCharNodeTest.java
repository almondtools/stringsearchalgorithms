package net.amygdalum.stringsearchalgorithms.regex;

import static com.almondtools.conmatch.conventions.ReflectiveEqualsMatcher.reflectiveEqualTo;
import static net.amygdalum.stringsearchalgorithms.regex.AnyCharNode.dotAll;
import static net.amygdalum.stringsearchalgorithms.regex.AnyCharNode.dotDefault;
import static net.amygdalum.util.text.CharUtils.after;
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

@RunWith(MockitoJUnitRunner.class)
public class AnyCharNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@SuppressWarnings("unchecked")
	@Test
	public void testToCharNodes() throws Exception {
		assertThat(dotAll((char) 0, (char) 255).toCharNodes(), contains(reflectiveEqualTo((DefinedCharNode) new RangeCharNode((char) 0, (char) 255))));
		assertThat(dotDefault((char) 0, (char) 255).toCharNodes(), contains(
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode((char) 0, before('\n'))),
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode(after('\n'), before('\r'))),
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode(after('\r'), before('\u0085'))),
			reflectiveEqualTo((DefinedCharNode) new RangeCharNode(after('\u0085'), (char) 255))));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitAnyChar(any(AnyCharNode.class))).thenReturn("success");

		assertThat(dotDefault((char) 0, (char) 255).accept(visitor), equalTo("success"));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		AnyCharNode original = dotDefault((char) 0, (char) 255);
		AnyCharNode cloned = original.clone();

		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		AnyCharNode original = dotDefault((char) 0, (char) 255);
		AnyCharNode cloned = original.clone();

		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(dotDefault((char) 0, (char) 255).toString(), equalTo("."));
	}

}
