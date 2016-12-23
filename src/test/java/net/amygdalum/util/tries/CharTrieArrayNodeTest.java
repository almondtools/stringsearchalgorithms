package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.amygdalum.util.map.CharObjectMap;

public class CharTrieArrayNodeTest {

	@Test
	public void testGetAlternatives() throws Exception {
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.getAlternatives(), charArrayContaining('a', 'b').inAnyOrder());
	}

	@Test
	public void testGetAttached() throws Exception {
		assertThat(new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null).getAttached(), nullValue());
		assertThat(new CharTrieArrayNode<String>(mapOf("alpha", "beta"), "string").getAttached(), equalTo("string"));
	}

	@Test
	public void testLink() throws Exception {
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null);
		CharTrieNode<String> link = new CharTrieSingleNode<String>("link".toCharArray(), "link");

		node.link(link);

		assertThat(node.getLink(), equalTo(link));
	}

	@Test
	public void testNextNode() throws Exception {
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode('a').nextNode("lpha".toCharArray()).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode('b').nextNode("eta".toCharArray()).getAttached(), equalTo("beta"));
		assertThat(node.nextNode('c'), nullValue());
	}

	@Test
	public void testNextNodeArray() throws Exception {
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode("alpha".toCharArray()).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("alp".toCharArray()).nextNode("ha".toCharArray()).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("beta".toCharArray()).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("be".toCharArray()).nextNode("ta".toCharArray()).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("gamma".toCharArray()), nullValue());
	}

	@Test
	public void testNextNodeArrayInt() throws Exception {
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode("alpha".toCharArray(), 0).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("xyz,alpha".toCharArray(), 4).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode("xyz,alpha".toCharArray(), 0), nullValue());
		assertThat(node.nextNode("beta".toCharArray(), 0).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("xyz,beta".toCharArray(), 4).getAttached(), equalTo("beta"));
		assertThat(node.nextNode("xyz,beta".toCharArray(), 0), nullValue());
	}

	@Test
	public void testNextNodeArrayWithoutOrdinaryTermination() throws Exception {
		CharObjectMap<CharTrieNode<String>> empty = new CharObjectMap<CharTrieNode<String>>(new CharTrieTerminalNode<String>(null));
		CharTrieArrayNode<String> next = new CharTrieArrayNode<String>(empty, "next");
		CharObjectMap<CharTrieNode<String>> nextMap = new CharObjectMap<CharTrieNode<String>>(new CharTrieTerminalNode<String>(null));
		nextMap.put('n', next);
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(nextMap, null);

		assertThat(node.nextNode('n').getAttached(), equalTo("next"));
		assertThat(node.nextNode("n".toCharArray()).getAttached(), equalTo("next"));
		assertThat(node.nextNode("no".toCharArray()), nullValue());
	}

	@Test
	public void testNextNodeArrayIntWithoutOrdinaryTermination() throws Exception {
		CharObjectMap<CharTrieNode<String>> empty = new CharObjectMap<CharTrieNode<String>>(new CharTrieTerminalNode<String>(null));
		CharTrieArrayNode<String> next = new CharTrieArrayNode<String>(empty, "next");
		CharObjectMap<CharTrieNode<String>> nextMap = new CharObjectMap<CharTrieNode<String>>(new CharTrieTerminalNode<String>(null));
		nextMap.put('n', next);
		CharTrieArrayNode<String> node = new CharTrieArrayNode<String>(nextMap, null);

		assertThat(node.nextNode('n').getAttached(), equalTo("next"));
		assertThat(node.nextNode("n".toCharArray(), 0).getAttached(), equalTo("next"));
		assertThat(node.nextNode("no".toCharArray(), 0), nullValue());
	}

	private CharObjectMap<CharTrieNode<String>> mapOf(String... strings) {
		CharObjectMap<CharTrieNode<String>> byteObjectMap = new CharObjectMap<CharTrieNode<String>>(null);
		for (String string : strings) {
			char[] chars = string.toCharArray();
			char key = chars[0];
			char[] suffix = Arrays.copyOfRange(chars, 1, chars.length);
			byteObjectMap.add(key, new CharTrieSingleNode<String>(suffix, string));
		}
		return byteObjectMap;
	}

}
