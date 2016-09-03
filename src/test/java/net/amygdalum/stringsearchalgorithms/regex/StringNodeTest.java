package net.amygdalum.stringsearchalgorithms.regex;

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

import net.amygdalum.stringsearchalgorithms.regex.RegexNodeVisitor;
import net.amygdalum.stringsearchalgorithms.regex.StringNode;


@RunWith(MockitoJUnitRunner.class)
public class StringNodeTest {

	@Mock
	private RegexNodeVisitor<String> visitor;

	@Test
	public void testGetValue() throws Exception {
		assertThat(new StringNode("abc").getValue(), equalTo("abc"));
	}

	@Test
	public void testGetLiteralValue() throws Exception {
		assertThat(new StringNode("abc").getLiteralValue(), equalTo("abc"));
	}

	@Test
	public void testAccept() throws Exception {
		when(visitor.visitString(any(StringNode.class))).thenReturn("success");
		
		assertThat(new StringNode("abc").accept(visitor), equalTo("success"));
	}

	@Test
	public void testToCharNodes() throws Exception {
		assertThat(new StringNode("abc").toCharNodes(), hasSize(3));
	}

	@Test
	public void testCloneIsNotOriginal() throws Exception {
		StringNode original = new StringNode("abc");
		StringNode cloned = original.clone();
		
		assertThat(cloned, not(sameInstance(original)));
	}

	@Test
	public void testCloneIsSimilar() throws Exception {
		StringNode original = new StringNode("abc");
		StringNode cloned = original.clone();
		
		assertThat(cloned.toString(), equalTo(original.toString()));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new StringNode("abc").toString(), equalTo("abc"));
	}

}
