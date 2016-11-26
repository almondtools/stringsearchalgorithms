package net.amygdalum.util.text;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CharAlphabetTest {

	@Test
	public void testRanged() throws Exception {
		assertThat(CharAlphabet.ranged("abcba".toCharArray()).getRange(), equalTo(3));
		assertThat(CharAlphabet.ranged("abcba".toCharArray()).minChar(), equalTo('a'));
		assertThat(CharAlphabet.ranged("abcba".toCharArray()).maxChar(), equalTo('c'));
	}

	@Test
	public void testRangedMapped() throws Exception {
		CharMapping mapping = new CharMapping() {
			
			@Override
			public char[] normalized(char[] chars) {
				return chars;
			}
			
			@Override
			public char[] map(char c) {
				if (c == 'a') {
					return new char[]{'A'};
				} else if (c == 'C') {
					return new char[]{'c'};
				} else {
					return new char[]{c};
				}
			}
		};
		assertThat(CharAlphabet.ranged("abcba".toCharArray(), mapping).getRange(), equalTo(35));
		assertThat(CharAlphabet.ranged("abcba".toCharArray(), mapping).minChar(), equalTo('A'));
		assertThat(CharAlphabet.ranged("abcba".toCharArray(), mapping).maxChar(), equalTo('c'));
		assertThat(CharAlphabet.ranged("AbCba".toCharArray(), mapping).getRange(), equalTo(35));
		assertThat(CharAlphabet.ranged("AbCba".toCharArray(), mapping).minChar(), equalTo('A'));
		assertThat(CharAlphabet.ranged("AbCba".toCharArray(), mapping).maxChar(), equalTo('c'));
	}

}
