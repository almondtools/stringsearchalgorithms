package net.amygdalum.stringsearchalgorithms.search.chars;

import static com.almondtools.conmatch.datatypes.PrimitiveArrayMatcher.charArrayContaining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TrieNodeTest {

	@Test
	public void testAddNext() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		TrieNode<String> nextNode = new TrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		
		assertThat(trieNode.getNexts().get('a'), sameInstance(nextNode));
	}

	@Test
	public void testAddNextAggregates() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		TrieNode<String> nextNode = new TrieNode<String>();
		TrieNode<String> otherNode = new TrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('b', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(nextNode));
		assertThat(trieNode.nextNode('b'), sameInstance(otherNode));
	}

	@Test
	public void testAddNextReplaces() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		TrieNode<String> nextNode = new TrieNode<String>();
		TrieNode<String> otherNode = new TrieNode<String>();
		
		trieNode.addNext('a', nextNode);
		trieNode.addNext('a', otherNode);
		
		assertThat(trieNode.nextNode('a'), sameInstance(otherNode));
	}

	@Test
	public void testAddFallback() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		TrieNode<String> fallbackNode = new TrieNode<String>();
		
		trieNode.addFallback(fallbackNode);
		
		assertThat(trieNode.getFallback(), sameInstance(fallbackNode));
	}

	@Test
	public void testAddFallbackReplaces() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		TrieNode<String> otherNode = new TrieNode<String>();
		TrieNode<String> fallbackNode = new TrieNode<String>();
		
		trieNode.addFallback(otherNode);
		trieNode.addFallback(fallbackNode);
		
		assertThat(trieNode.getFallback(), sameInstance(fallbackNode));
	}

	@Test
	public void testSetAttached() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testSetAttachedNonString() throws Exception {
		TrieNode<Double> trieNode = new TrieNode<Double>();
		
		trieNode.setAttached(Double.valueOf(42));
		
		assertThat(trieNode.getAttached(), equalTo(Double.valueOf(42)));
	}

	@Test
	public void testSetAttachedReplaces() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.setAttached("OldAttached");
		trieNode.setAttached("Attached");
		
		assertThat(trieNode.getAttached(), equalTo((Object) "Attached"));
	}

	@Test
	public void testExtendCharArray() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.extend("AB".toCharArray(), "AB");
		
		assertThat(trieNode.nextNode('A').nextNode('B').getAttached(), equalTo("AB"));
	}
	
	@Test
	public void testExtendCharArrayAndNonString() throws Exception {
		TrieNode<Double> trieNode = new TrieNode<Double>();
		
		trieNode.extend("AB".toCharArray(), Double.valueOf(42));
		
		assertThat(trieNode.nextNode('A').nextNode('B').getAttached(), equalTo(Double.valueOf(42)));
	}
	
	@Test
	public void testExtendCharArrayAggregates() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.extend("AB".toCharArray(),"AB");
		trieNode.extend("AC".toCharArray(),"AC");
		trieNode.extend("ABC".toCharArray(),"ABC");
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode("ABC".toCharArray()).getAttached(), equalTo("ABC"));
		assertThat(trieNode.nextNode("AC".toCharArray()).getAttached(), equalTo("AC"));
	}

	@Test
	public void testExtendWideRanges() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.extend("AB".toCharArray(),"AB");
		trieNode.extend("\u4500A".toCharArray(),"\u4500A");
		trieNode.extend("\u4500\u4501\u4502".toCharArray(), "\u4500\u4501\u4502");
		
		
		assertThat(trieNode.nextNode("AB".toCharArray()).getAttached(), equalTo("AB"));
		assertThat(trieNode.nextNode("\u4500A".toCharArray()).getAttached(), equalTo("\u4500A"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4501\u4502"));
	}

	@Test
	public void testExtendHighRanges() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
		trieNode.extend("\u4500\u4501".toCharArray(), "\u4500\u4501");
		trieNode.extend("\u4500\u4502".toCharArray(), "\u4500\u4502");
		trieNode.extend("\u4500\u4501\u4502".toCharArray(), "\u4500\u4501\u4502");
		
		assertThat(trieNode.nextNode("\u4500\u4501".toCharArray()).getAttached(), equalTo("\u4500\u4501"));
		assertThat(trieNode.nextNode("\u4500\u4501\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4501\u4502"));
		assertThat(trieNode.nextNode("\u4500\u4502".toCharArray()).getAttached(), equalTo("\u4500\u4502"));
	}

	@Test
	public void testGetNexts() throws Exception {
		TrieNode<String> trieNode = new TrieNode<String>();
		
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
		TrieNode<String> trieNode = new TrieNode<String>();
		
		assertThat(trieNode.nextNode('a'), nullValue());
		assertThat(trieNode.nextNode("b".toCharArray()), nullValue());
		assertThat(trieNode.nextNode("ab".toCharArray()), nullValue());
	}

	@Test
	public void testRevert() throws Exception {
		assertThat(TrieNode.revert("AB".toCharArray()), equalTo("BA".toCharArray()));
	}

}
