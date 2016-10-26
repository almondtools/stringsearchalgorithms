package net.amygdalum.stringsearchalgorithms.search;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TrieNodeTest {

	@Test
	public void testAddNext() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		TrieNode<Object> nextNode = new TrieNode<Object>();
		
		trieNode.addNext('a', nextNode);
		
		assertThat(trieNode.getNexts().get('a'), sameInstance(nextNode));
	}

	@Test
	public void testAddNextAggregates() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		TrieNode<Object> nextNode = new TrieNode<Object>();
		TrieNode<Object> otherNode = new TrieNode<Object>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('b', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(nextNode));
		assertThat(trieNode.nextNode('b'), sameInstance(otherNode));
	}

	@Test
	public void testAddNextReplaces() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		TrieNode<Object> nextNode = new TrieNode<Object>();
		TrieNode<Object> otherNode = new TrieNode<Object>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('a', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(otherNode));
	}

	@Test
	public void testAddFallback() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		TrieNode<Object> fallbackNode = new TrieNode<Object>();
		
		trieNode.addFallback(fallbackNode);
		
		assertThat(trieNode.getFallback(), sameInstance(fallbackNode));
	}

	@Test
	public void testAddFallbackReplaces() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		TrieNode<Object> otherNode = new TrieNode<Object>();
		TrieNode<Object> fallbackNode = new TrieNode<Object>();
		
		trieNode.addFallback(otherNode);
		trieNode.addFallback(fallbackNode);
		
		assertThat(trieNode.getFallback(), sameInstance(fallbackNode));
	}

	@Test
	public void testSetMatch() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.setMatch("Match");
		
		assertThat(trieNode.getMatch(), equalTo("Match"));
	}

	@Test
	public void testSetAttached() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testSetAttachedReplaces() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.setAttached("OldAttached");
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testExtendCharArray() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.extend("AB".toCharArray());
		
		assertThat(trieNode.nextNode('A').nextNode('B').getMatch(), equalTo("AB"));
	}
	
	@Test
	public void testExtendCharArrayAggregates() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.extend("AB".toCharArray());
		trieNode.extend("AC".toCharArray());
		trieNode.extend("ABC".toCharArray());
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getMatch(), equalTo("AB"));
		assertThat(trieNode.nextNode("ABC".toCharArray()).getMatch(), equalTo("ABC"));
		assertThat(trieNode.nextNode("AC".toCharArray()).getMatch(), equalTo("AC"));
	}

	@Test
	public void testExtendWideRanges() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.extend("AB".toCharArray());
		trieNode.extend("\u4500A".toCharArray());
		trieNode.extend("\u4500\u4501\u4502".toCharArray());
		
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getMatch(), equalTo("AB"));
		assertThat(trieNode.nextNode("\u4500A".toCharArray()).getMatch(), equalTo("\u4500A"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getMatch(), equalTo("\u4500\u4501\u4502"));
	}

	@Test
	public void testExtendHighRanges() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.extend("\u4500\u4501".toCharArray());
		trieNode.extend("\u4500\u4502".toCharArray());
		trieNode.extend("\u4500\u4501\u4502".toCharArray());
		
		assertThat(trieNode.nextNode("\u4500\u4501".toCharArray()).getMatch(), equalTo("\u4500\u4501"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getMatch(), equalTo("\u4500\u4501\u4502"));
		assertThat(trieNode.nextNode("\u4500\u4502".toCharArray()).getMatch(), equalTo("\u4500\u4502"));
	}

	@Test
	public void testGetNexts() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		trieNode.extend("B".toCharArray());
		trieNode.extend("C".toCharArray());
		trieNode.extend("BC".toCharArray());

		assertThat(trieNode.getNexts().keySet(), containsInAnyOrder('B','C'));
		assertThat(trieNode.getNexts().get('B').getMatch(), equalTo("B"));
		assertThat(trieNode.getNexts().get('B').nextNode('C').getMatch(), equalTo("BC"));
		assertThat(trieNode.getNexts().get('C').getMatch(), equalTo("C"));
		assertThat(trieNode.getNexts().get('C').getNexts().keySet(), empty());
	}

	@Test
	public void testNextNode() throws Exception {
		TrieNode<Object> trieNode = new TrieNode<Object>();
		
		assertThat(trieNode.nextNode('a'), nullValue());
		assertThat(trieNode.nextNode("b".toCharArray()), nullValue());
		assertThat(trieNode.nextNode("ab".toCharArray()), nullValue());
	}

	@Test
	public void testRevert() throws Exception {
		assertThat(TrieNode.revert("AB".toCharArray()), equalTo("BA".toCharArray()));
	}

}
