package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.amygdalum.util.text.StringUtils;

public class CharTrieSingleNodeTest {

	@Test
	public void testSubsumingConstructor() throws Exception {
		CharTrieSingleNode<String> subsumed = new CharTrieSingleNode<String>("hars".toCharArray(), "chars");
		CharTrieSingleNode<String> node = new CharTrieSingleNode<String>('c', subsumed, null);
		
		assertThat(node.length(), equalTo(5));
		assertThat(node.getAttached(), nullValue());
		assertThat(node.nextNode("chars".toCharArray()).getAttached(), equalTo("chars"));
	}

	@Test
	public void testLeafSubsumingConstructor() throws Exception {
		CharTrieTerminalNode<String> subsumed = new CharTrieTerminalNode<String>("c");
		CharTrieSingleNode<String> node = new CharTrieSingleNode<String>('c', subsumed, null);
		
		assertThat(node.length(), equalTo(1));
		assertThat(node.getAttached(), nullValue());
		assertThat(node.nextNode('c').getAttached(), equalTo("c"));
	}

	@Test
	public void testLength() throws Exception {
		assertThat(defaultNode("chars").length(), equalTo(5));
	}

	@Test
	public void testNextNodeChar() throws Exception {
		assertThat(defaultNode("chars").nextNode('x'), nullValue());
		assertThat(defaultNode("chars").nextNode('c').getAttached(), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode('h').getAttached(), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode('h').nextNode('a').nextNode('r').nextNode('s').getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode('c').nextNode("hars".toCharArray()).getAttached(), equalTo("chars"));
	}

	@Test
	public void testNextNodeCharArray() throws Exception {
		assertThat(defaultNode("chars").nextNode("xars".toCharArray()), nullValue());
		assertThat(defaultNode("chars").nextNode("chars".toCharArray()).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("charsx".toCharArray()), nullValue());
	}

	@Test
	public void testNextNodeCharArrayInt() throws Exception {
		assertThat(defaultNode("chars").nextNode("chars".toCharArray(), 0).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("xchars".toCharArray(), 1).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("xchars".toCharArray(), 0), nullValue());
		assertThat(defaultNode("chars").nextNode("charsx".toCharArray(), 0), nullValue());
	}

	@Test
	public void testGetAttached() throws Exception {
		assertThat(defaultNode("chars").getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode('c').getAttached(), equalTo("c"));
		assertThat(multiNode("c", "h", "ars").nextNode("ch".toCharArray()).getAttached(), equalTo("ch"));
		assertThat(multiNode("c", "h", "ars").nextNode("cha".toCharArray()).getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode("char".toCharArray()).getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode("chars".toCharArray()).getAttached(), equalTo("chars"));
	}

	@Test
	public void testGetAlternatives() throws Exception {
		assertThat(defaultNode("chars").getAlternatives(), charArrayContaining('c'));
	}
	
	@Test
	public void testProxyLength() throws Exception {
		assertThat(((CharTrieLeafNode<String>) defaultNode("chars").nextNode('c')).length(), equalTo(4));
	}

	@Test
	public void testProxyNextNodeChar() throws Exception {
		assertThat(((CharTrieLeafNode<String>) defaultNode("chars").nextNode('c').nextNode('h')).length(), equalTo(3));
		assertThat(defaultNode("chars").nextNode('c').nextNode('x'), nullValue());
		assertThat(defaultNode("chars").nextNode("chars".toCharArray()).nextNode('x'), nullValue());
	}

	@Test
	public void testProxyNextNodeCharArray() throws Exception {
		assertThat(defaultNode("chars").nextNode('c').nextNode("x".toCharArray()), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode("harsx".toCharArray()), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode("hars".toCharArray()).getAttached(), equalTo("chars"));
	}

	@Test
	public void testProxyNextNodeCharArrayInt() throws Exception {
		assertThat(defaultNode("chars").nextNode('c').nextNode("x".toCharArray(), 0), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode("harsx".toCharArray(), 0), nullValue());
		assertThat(defaultNode("chars").nextNode('c').nextNode("xhars".toCharArray(), 1).getAttached(), equalTo("chars"));
	}

	@Test
	public void testProxyGetAlternatives() throws Exception {
		assertThat(defaultNode("chars").nextNode('c').getAlternatives(), charArrayContaining('h'));
	}
	
	private CharTrieSingleNode<String> defaultNode(String string) {
		return new CharTrieSingleNode<>(string.toCharArray(), string);
	}

	private CharTrieSingleNode<String> multiNode(String... strings) {
		char[] chars = StringUtils.join(Arrays.asList(strings)).toCharArray();
		String[] attached = new String[chars.length + 1];
		int pos = 0;
		String acc = "";
		for (String string : strings) {
			pos += string.length();
			acc += string;
			attached[pos] = acc;
		}
		return new  CharTrieSingleNode<String>(chars, attached);
	}

}
