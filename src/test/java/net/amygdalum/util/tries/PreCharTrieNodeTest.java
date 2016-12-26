package net.amygdalum.util.tries;


import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

public class PreCharTrieNodeTest {

	@Test
	public void testAddNext() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> nextNode = new PreCharTrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		
		assertThat(trieNode.getNexts().get('a'), sameInstance(nextNode));
	}

	@Test
	public void testAddNextAggregates() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> nextNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> otherNode = new PreCharTrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('b', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(nextNode));
		assertThat(trieNode.nextNode('b'), sameInstance(otherNode));
	}

	@Test
	public void testAddNextReplaces() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> nextNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> otherNode = new PreCharTrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('a', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(otherNode));
	}

	@Test
	public void testLink() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> fallbackNode = new PreCharTrieNode<String>();
		
		trieNode.link(fallbackNode);
		
		assertThat(trieNode.getLink(), sameInstance(fallbackNode));
	}

	@Test
	public void testLinkReplaces() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> otherNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> fallbackNode = new PreCharTrieNode<String>();
		
		trieNode.link(otherNode);
		trieNode.link(fallbackNode);
		
		assertThat(trieNode.getLink(), sameInstance(fallbackNode));
	}

	@Test
	public void testSetAttached() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testSetAttachedNonString() throws Exception {
		PreCharTrieNode<Double> trieNode = new PreCharTrieNode<Double>();
		
		trieNode.setAttached(Double.valueOf(42));
		
		assertThat(trieNode.getAttached(), equalTo(Double.valueOf(42)));
	}

	@Test
	public void testSetAttachedReplaces() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.setAttached("OldAttached");
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testExtendCharArray() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.extend("AB".toCharArray(), "AB");
		
		assertThat(trieNode.nextNode('A').nextNode('B').getAttached(), equalTo("AB"));
	}
	
	@Test
	public void testExtendCharArrayAndNonString() throws Exception {
		PreCharTrieNode<Double> trieNode = new PreCharTrieNode<Double>();
		
		trieNode.extend("AB".toCharArray(), Double.valueOf(42));
		
		assertThat(trieNode.nextNode('A').nextNode('B').getAttached(), equalTo(Double.valueOf(42)));
	}
	
	@Test
	public void testExtendCharArrayAggregates() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.extend("AB".toCharArray(),"AB");
		trieNode.extend("AC".toCharArray(),"AC");
		trieNode.extend("ABC".toCharArray(),"ABC");
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode("ABC".toCharArray()).getAttached(), equalTo("ABC"));
		assertThat(trieNode.nextNode("AC".toCharArray()).getAttached(), equalTo("AC"));
	}

	@Test
	public void testExtendWideRanges() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.extend("AB".toCharArray(),"AB");
		trieNode.extend("\u4500A".toCharArray(),"\u4500A");
		trieNode.extend("\u4500\u4501\u4502".toCharArray(), "\u4500\u4501\u4502");
		
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode("\u4500A".toCharArray()).getAttached(), equalTo("\u4500A"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4501\u4502"));
	}

	@Test
	public void testExtendHighRanges() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.extend("\u4500\u4501".toCharArray(), "\u4500\u4501");
		trieNode.extend("\u4500\u4502".toCharArray(), "\u4500\u4502");
		trieNode.extend("\u4500\u4501\u4502".toCharArray(), "\u4500\u4501\u4502");
		
		assertThat(trieNode.nextNode("\u4500\u4501".toCharArray()).getAttached(), equalTo("\u4500\u4501"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4501\u4502"));
		assertThat(trieNode.nextNode("\u4500\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4502"));
	}

	@Test
	public void testGetNexts() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		trieNode.extend("B".toCharArray(),"B");
		trieNode.extend("C".toCharArray(),"C");
		trieNode.extend("BC".toCharArray(),"BC");

		assertThat(trieNode.getNexts().keys(), charArrayContaining('B','C'));
		assertThat(trieNode.getNexts().get('B').getAttached(), equalTo("B"));
		assertThat(trieNode.getNexts().get('B').nextNode('C').getAttached(), equalTo("BC"));
		assertThat(trieNode.getNexts().get('C').getAttached(), equalTo("C"));
		assertThat(trieNode.getNexts().get('C').getNexts().keys(), charArrayContaining());
	}

	@Test
	public void testNextNode() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		
		assertThat(trieNode.nextNode('a'), nullValue());
		assertThat(trieNode.nextNode("b".toCharArray()), nullValue());
		assertThat(trieNode.nextNode("ab".toCharArray()), nullValue());
	}

	@Test
	public void testReset() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		trieNode.addNext('a', new PreCharTrieNode<String>());
		trieNode.addNext('b', new PreCharTrieNode<String>());
		
		trieNode.reset();
		
		assertThat(trieNode.nextNode('a'), nullValue());
		assertThat(trieNode.nextNode('b'), nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNodes() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> aNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> bNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> cNode = new PreCharTrieNode<String>();
		trieNode.addNext('a', aNode);
		trieNode.addNext('b', bNode);
		aNode.addNext('c', cNode);
		
		Set<PreCharTrieNode<String>> nodes = trieNode.nodes();
		
		assertThat(nodes, containsInAnyOrder(trieNode, aNode, bNode, cNode));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNodesGraph() throws Exception {
		PreCharTrieNode<String> trieNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> aNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> bNode = new PreCharTrieNode<String>();
		PreCharTrieNode<String> cNode = new PreCharTrieNode<String>();
		trieNode.addNext('a', aNode);
		trieNode.addNext('b', bNode);
		aNode.addNext('c', cNode);
		aNode.addNext('b', bNode);
		
		Set<PreCharTrieNode<String>> nodes = trieNode.nodes();
		
		assertThat(nodes, containsInAnyOrder(trieNode, aNode, bNode, cNode));
	}

}
