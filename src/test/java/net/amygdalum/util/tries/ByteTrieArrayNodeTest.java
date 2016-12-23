package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.byteArrayContaining;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.amygdalum.util.map.ByteObjectMap;

public class ByteTrieArrayNodeTest {

	@Test
	public void testGetAlternatives() throws Exception {
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.getAlternatives(), byteArrayContaining((byte) 'a', (byte) 'b').inAnyOrder());
	}

	@Test
	public void testGetAttached() throws Exception {
		assertThat(new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null).getAttached(), nullValue());
		assertThat(new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), "string").getAttached(), equalTo("string"));
	}

	@Test
	public void testLink() throws Exception {
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null);
		ByteTrieNode<String> link = new ByteTrieSingleNode<String>("link".getBytes(UTF_8), "link");

		node.link(link);

		assertThat(node.getLink(), equalTo(link));
	}

	@Test
	public void testNextNode() throws Exception {
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode((byte) 'a').nextNode("lpha".getBytes(UTF_8)).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode((byte) 'b').nextNode("eta".getBytes(UTF_8)).getAttached(), equalTo("beta"));
		assertThat(node.nextNode((byte) 'c'), nullValue());
	}

	@Test
	public void testNextNodeArray() throws Exception {
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode("alpha".getBytes(UTF_8)).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("alp".getBytes(UTF_8)).nextNode("ha".getBytes(UTF_8)).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("beta".getBytes(UTF_8)).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("be".getBytes(UTF_8)).nextNode("ta".getBytes(UTF_8)).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("gamma".getBytes(UTF_8)), nullValue());
	}

	@Test
	public void testNextNodeArrayInt() throws Exception {
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode("alpha".getBytes(UTF_8), 0).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("xyz,alpha".getBytes(UTF_8), 4).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("xyz,alpha".getBytes(UTF_8), 0), nullValue());
		assertThat(node.nextNode("beta".getBytes(UTF_8), 0).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("xyz,beta".getBytes(UTF_8), 4).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("xyz,beta".getBytes(UTF_8), 0), nullValue());
	}

	@Test
	public void testNextNodeArrayWithoutOrdinaryTermination() throws Exception {
		ByteObjectMap<ByteTrieNode<String>> empty = new ByteObjectMap<ByteTrieNode<String>>(new ByteTrieTerminalNode<String>(null));
		ByteTrieArrayNode<String> next = new ByteTrieArrayNode<String>(empty, "next");
		ByteObjectMap<ByteTrieNode<String>> nextMap = new ByteObjectMap<ByteTrieNode<String>>(new ByteTrieTerminalNode<String>(null));
		nextMap.put((byte) 'n', next);
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(nextMap, null);

		assertThat(node.nextNode((byte) 'n').getAttached(), equalTo("next"));
		assertThat(node.nextNode("n".getBytes(UTF_8)).getAttached(), equalTo("next"));
		assertThat(node.nextNode("no".getBytes(UTF_8)), nullValue());
	}

	@Test
	public void testNextNodeArrayIntWithoutOrdinaryTermination() throws Exception {
		ByteObjectMap<ByteTrieNode<String>> empty = new ByteObjectMap<ByteTrieNode<String>>(new ByteTrieTerminalNode<String>(null));
		ByteTrieArrayNode<String> next = new ByteTrieArrayNode<String>(empty, "next");
		ByteObjectMap<ByteTrieNode<String>> nextMap = new ByteObjectMap<ByteTrieNode<String>>(new ByteTrieTerminalNode<String>(null));
		nextMap.put((byte) 'n', next);
		ByteTrieArrayNode<String> node = new ByteTrieArrayNode<String>(nextMap, null);

		assertThat(node.nextNode((byte) 'n').getAttached(), equalTo("next"));
		assertThat(node.nextNode("n".getBytes(UTF_8), 0).getAttached(), equalTo("next"));
		assertThat(node.nextNode("no".getBytes(UTF_8), 0), nullValue());
	}

	private ByteObjectMap<ByteTrieNode<String>> mapOf(String... strings) {
		ByteObjectMap<ByteTrieNode<String>> byteObjectMap = new ByteObjectMap<ByteTrieNode<String>>(null);
		for (String string : strings) {
			byte[] bytes = string.getBytes(UTF_8);
			byte key = bytes[0];
			byte[] suffix = Arrays.copyOfRange(bytes, 1, bytes.length);
			byteObjectMap.add(key, new ByteTrieSingleNode<String>(suffix, string));
		}
		return byteObjectMap;
	}

}
