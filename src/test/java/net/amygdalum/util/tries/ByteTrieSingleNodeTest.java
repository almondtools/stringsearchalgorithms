package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.byteArrayContaining;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.amygdalum.util.text.StringUtils;

public class ByteTrieSingleNodeTest {

	@Test
	public void testSubsumingConstructor() throws Exception {
		ByteTrieSingleNode<String> subsumed = new ByteTrieSingleNode<String>("hars".getBytes(UTF_8), "chars");
		ByteTrieSingleNode<String> node = new ByteTrieSingleNode<String>((byte) 'c', subsumed, null);
		
		assertThat(node.length(), equalTo(5));
		assertThat(node.getAttached(), nullValue());
		assertThat(node.nextNode("chars".getBytes(UTF_8)).getAttached(), equalTo("chars"));
	}

	@Test
	public void testLeafSubsumingConstructor() throws Exception {
		ByteTrieTerminalNode<String> subsumed = new ByteTrieTerminalNode<String>("c");
		ByteTrieSingleNode<String> node = new ByteTrieSingleNode<String>((byte) 'c', subsumed, null);
		
		assertThat(node.length(), equalTo(1));
		assertThat(node.getAttached(), nullValue());
		assertThat(node.nextNode((byte) 'c').getAttached(), equalTo("c"));
	}

	@Test
	public void testLength() throws Exception {
		assertThat(defaultNode("chars").length(), equalTo(5));
	}

	@Test
	public void testNextNodeChar() throws Exception {
		assertThat(defaultNode("chars").nextNode((byte) 'x'), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').getAttached(), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode((byte) 'h').getAttached(), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode((byte) 'h').nextNode((byte) 'a').nextNode((byte) 'r').nextNode((byte) 's').getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("hars".getBytes(UTF_8)).getAttached(), equalTo("chars"));
	}

	@Test
	public void testNextNodeCharArray() throws Exception {
		assertThat(defaultNode("chars").nextNode("xars".getBytes(UTF_8)), nullValue());
		assertThat(defaultNode("chars").nextNode("chars".getBytes(UTF_8)).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("charsx".getBytes(UTF_8)), nullValue());
	}

	@Test
	public void testNextNodeCharArrayInt() throws Exception {
		assertThat(defaultNode("chars").nextNode("chars".getBytes(UTF_8), 0).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("xchars".getBytes(UTF_8), 1).getAttached(), equalTo("chars"));
		assertThat(defaultNode("chars").nextNode("xchars".getBytes(UTF_8), 0), nullValue());
		assertThat(defaultNode("chars").nextNode("charsx".getBytes(UTF_8), 0), nullValue());
	}

	@Test
	public void testGetAttached() throws Exception {
		assertThat(defaultNode("chars").getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode((byte) 'c').getAttached(), equalTo("c"));
		assertThat(multiNode("c", "h", "ars").nextNode("ch".getBytes(UTF_8)).getAttached(), equalTo("ch"));
		assertThat(multiNode("c", "h", "ars").nextNode("cha".getBytes(UTF_8)).getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode("char".getBytes(UTF_8)).getAttached(), nullValue());
		assertThat(multiNode("c", "h", "ars").nextNode("chars".getBytes(UTF_8)).getAttached(), equalTo("chars"));
	}

	@Test
	public void testGetAlternatives() throws Exception {
		assertThat(defaultNode("chars").getAlternatives(), byteArrayContaining((byte) 'c'));
	}
	
	@Test
	public void testProxyLength() throws Exception {
		assertThat(((ByteTrieLeafNode<String>) defaultNode("chars").nextNode((byte) 'c')).length(), equalTo(4));
	}

	@Test
	public void testProxyNextNodeChar() throws Exception {
		assertThat(((ByteTrieLeafNode<String>) defaultNode("chars").nextNode((byte) 'c').nextNode((byte) 'h')).length(), equalTo(3));
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode((byte) 'x'), nullValue());
		assertThat(defaultNode("chars").nextNode("chars".getBytes(UTF_8)).nextNode((byte) 'x'), nullValue());
	}

	@Test
	public void testProxyNextNodeCharArray() throws Exception {
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("x".getBytes(UTF_8)), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("harsx".getBytes(UTF_8)), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("hars".getBytes(UTF_8)).getAttached(), equalTo("chars"));
	}

	@Test
	public void testProxyNextNodeCharArrayInt() throws Exception {
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("x".getBytes(UTF_8), 0), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("harsx".getBytes(UTF_8), 0), nullValue());
		assertThat(defaultNode("chars").nextNode((byte) 'c').nextNode("xhars".getBytes(UTF_8), 1).getAttached(), equalTo("chars"));
	}

	@Test
	public void testProxyGetAlternatives() throws Exception {
		assertThat(defaultNode("chars").nextNode((byte) 'c').getAlternatives(), byteArrayContaining((byte) 'h'));
	}
	
	private ByteTrieSingleNode<String> defaultNode(String string) {
		return new ByteTrieSingleNode<>(string.getBytes(UTF_8), string);
	}

	private ByteTrieSingleNode<String> multiNode(String... strings) {
		byte[] bytes = StringUtils.join(Arrays.asList(strings)).getBytes(UTF_8);
		String[] attached = new String[bytes.length + 1];
		int pos = 0;
		String acc = "";
		for (String string : strings) {
			pos += string.length();
			acc += string;
			attached[pos] = acc;
		}
		return new  ByteTrieSingleNode<String>(bytes, attached);
	}

}
