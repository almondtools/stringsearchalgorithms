package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.amygdalum.util.map.CharObjectMap;

public class CharTrieMapNodeTest {

	@Test
	public void testGetAlternatives() throws Exception {
		CharTrieMapNode<String> node = new CharTrieMapNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.getAlternatives(), charArrayContaining('a', 'b').inAnyOrder());
	}

	@Test
	public void testNextNode() throws Exception {
		CharTrieMapNode<String> node = new CharTrieMapNode<String>(mapOf("alpha", "beta"), null);

		assertThat(node.nextNode('a').nextNode("lpha".toCharArray()).getAttached(), equalTo("alpha"));
		assertThat(node.nextNode('b').nextNode("eta".toCharArray()).getAttached(), equalTo("beta"));
		assertThat(node.nextNode('c'), nullValue());
	}

	@Test
	public void testGetAttached() throws Exception {
		assertThat(new CharTrieMapNode<String>(mapOf("alpha", "beta"), null).getAttached(), nullValue());
		assertThat(new CharTrieMapNode<String>(mapOf("alpha", "beta"), "string").getAttached(), equalTo("string"));
	}

	@Test
	public void testLink() throws Exception {
		CharTrieMapNode<String> node = new CharTrieMapNode<String>(mapOf("alpha", "beta"), null);
		CharTrieNode<String> link = new CharTrieSingleNode<String>("link".toCharArray(), "link");

		node.link(link);

		assertThat(node.getLink(), equalTo(link));
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
