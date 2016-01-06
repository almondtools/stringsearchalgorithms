package com.almondtools.stringsandchars.regex;

import java.util.LinkedHashMap;
import java.util.Map;

public class CharClassBuilder {

	private Map<Character, CharNode> charClasses;
	
	public CharClassBuilder() {
		this.charClasses = new LinkedHashMap<>();
	}
	
	public CharClassBuilder add(char symbol, CharNode node) {
		charClasses.put(symbol, node);
		return this;
	}
	
	public Map<Character, CharNode> build() {
		return charClasses;
	}

}
