package net.amygdalum.util.tries;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.byteArrayContaining;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;


public class PreByteTrieNodeTest {

	private static final byte A = (byte) 0x41;
	private static final byte B = (byte) 0x42;
	private static final byte C = (byte) 0x43;
	private static final byte a = (byte) 0x61;
	private static final byte b = (byte) 0x62;
	private static final byte c = (byte) 0x63;
	private static final byte x = (byte) 0x78;

	@Test
	public void testAddNext() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> nextNode = new PreByteTrieNode<String>();

		trieNode.addNext(a, nextNode);

		assertThat(trieNode.getNexts().get(a), sameInstance(nextNode));
	}

	@Test
	public void testAddNextAggregates() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> nextNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> otherNode = new PreByteTrieNode<String>();

		trieNode.addNext(a, nextNode);
		trieNode.addNext(b, otherNode);

		assertThat(trieNode.nextNode(a), sameInstance(nextNode));
		assertThat(trieNode.nextNode(b), sameInstance(otherNode));
	}

	@Test
	public void testAddNextReplaces() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> nextNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> otherNode = new PreByteTrieNode<String>();

		trieNode.addNext(a, nextNode);
		trieNode.addNext(a, otherNode);

		assertThat(trieNode.nextNode(a), sameInstance(otherNode));
	}

	@Test
	public void testLink() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> fallbackNode = new PreByteTrieNode<String>();

		trieNode.link(fallbackNode);

		assertThat(trieNode.getLink(), sameInstance(fallbackNode));
	}

	@Test
	public void testAddFallbackReplaces() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> otherNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> fallbackNode = new PreByteTrieNode<String>();

		trieNode.link(otherNode);
		trieNode.link(fallbackNode);

		assertThat(trieNode.getLink(), sameInstance(fallbackNode));
	}

	@Test
	public void testSetAttached() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.setAttached("Attached");

		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testSetAttachedNonString() throws Exception {
		PreByteTrieNode<Double> trieNode = new PreByteTrieNode<Double>();
		
		trieNode.setAttached(Double.valueOf(42));
		
		assertThat(trieNode.getAttached(), equalTo(Double.valueOf(42)));
	}

	@Test
	public void testSetAttachedReplaces() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.setAttached("OldAttached");
		trieNode.setAttached("Attached");

		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testExtendCharArray() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.extend(new byte[] { A, B }, "AB");

		assertThat(trieNode.nextNode(A).nextNode(B).getAttached(), equalTo("AB"));
	}

	@Test
	public void testExtendCharArrayAndNonString() throws Exception {
		PreByteTrieNode<Double> trieNode = new PreByteTrieNode<Double>();
		
		trieNode.extend(new byte[]{A,B}, Double.valueOf(42));
		
		assertThat(trieNode.nextNode(A).nextNode(B).getAttached(), equalTo(Double.valueOf(42)));
	}

	@Test
	public void testExtendCharArrayAggregates() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.extend(new byte[] { A, B }, "AB");
		trieNode.extend(new byte[] { A, C }, "AC");
		trieNode.extend(new byte[] { A, B, C }, "ABC");

		assertThat(trieNode.nextNode(new byte[] { A, B }).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode(new byte[] { A, B, C }).getAttached(), equalTo("ABC"));
		assertThat(trieNode.nextNode(new byte[] { A, C }).getAttached(), equalTo("AC"));
	}

	@Test
	public void testExtendWideRanges() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.extend(new byte[] { A, B }, "AB");
		trieNode.extend("\u4500A".getBytes(UTF_8), "\u4500A");
		trieNode.extend("\u4500\u4501\u4502".getBytes(UTF_8), "\u4500\u4501\u4502");

		assertThat(trieNode.nextNode(new byte[] { A, B }).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode("\u4500A".getBytes(UTF_8)).getAttached(), equalTo("\u4500A"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".getBytes(UTF_8)).getAttached(), equalTo("\u4500\u4501\u4502"));
	}

	@Test
	public void testExtendHighRanges() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.extend("\u4500\u4501".getBytes(UTF_8), "\u4500\u4501");
		trieNode.extend("\u4500\u4502".getBytes(UTF_8), "\u4500\u4502");
		trieNode.extend("\u4500\u4501\u4502".getBytes(UTF_8), "\u4500\u4501\u4502");

		assertThat(trieNode.nextNode("\u4500\u4501".getBytes(UTF_8)).getAttached(), equalTo("\u4500\u4501"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".getBytes(UTF_8)).getAttached(), equalTo("\u4500\u4501\u4502"));
		assertThat(trieNode.nextNode("\u4500\u4502".getBytes(UTF_8)).getAttached(), equalTo("\u4500\u4502"));
	}

	@Test
	public void testGetNexts() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		trieNode.extend(new byte[] { B }, "B");
		trieNode.extend(new byte[] { C }, "C");
		trieNode.extend(new byte[] { B, C }, "BC");

		assertThat(trieNode.getNexts().keys(), byteArrayContaining(B, C));
		assertThat(trieNode.getNexts().get(B).getAttached(), equalTo("B"));
		assertThat(trieNode.getNexts().get(B).nextNode(C).getAttached(), equalTo("BC"));
		assertThat(trieNode.getNexts().get(C).getAttached(), equalTo("C"));
		assertThat(trieNode.getNexts().get(C).getNexts().keys(), byteArrayContaining());
	}

	@Test
	public void testNextNode() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();

		assertThat(trieNode.nextNode(a), nullValue());
		assertThat(trieNode.nextNode(new byte[] { b }), nullValue());
		assertThat(trieNode.nextNode(new byte[] { a, b }), nullValue());
	}

	@Test
	public void testReset() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		trieNode.addNext(a, new PreByteTrieNode<String>());
		trieNode.addNext(b, new PreByteTrieNode<String>());
		
		trieNode.reset();
		
		assertThat(trieNode.nextNode(a), nullValue());
		assertThat(trieNode.nextNode(b), nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNodes() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> aNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> bNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> cNode = new PreByteTrieNode<String>();
		trieNode.addNext(a, aNode);
		trieNode.addNext(b, bNode);
		aNode.addNext(c, cNode);
		
		Set<PreByteTrieNode<String>> nodes = trieNode.nodes();
		
		assertThat(nodes, containsInAnyOrder(trieNode, aNode, bNode, cNode));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNodesGraph() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> aNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> bNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> cNode = new PreByteTrieNode<String>();
		trieNode.addNext(a, aNode);
		trieNode.addNext(b, bNode);
		aNode.addNext(c, cNode);
		aNode.addNext(b, bNode);
		
		Set<PreByteTrieNode<String>> nodes = trieNode.nodes();
		
		assertThat(nodes, containsInAnyOrder(trieNode, aNode, bNode, cNode));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNodesCyclicGraph() throws Exception {
		PreByteTrieNode<String> trieNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> aNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> bNode = new PreByteTrieNode<String>();
		PreByteTrieNode<String> cNode = new PreByteTrieNode<String>();
		trieNode.addNext(a, aNode);
		trieNode.addNext(b, bNode);
		aNode.addNext(c, cNode);
		aNode.addNext(x, trieNode);
		
		Set<PreByteTrieNode<String>> nodes = trieNode.nodes();
		
		assertThat(nodes, containsInAnyOrder(trieNode, aNode, bNode, cNode));
	}

}
