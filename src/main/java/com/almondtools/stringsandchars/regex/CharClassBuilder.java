package com.almondtools.stringsandchars.regex;

import java.util.LinkedHashMap;
import java.util.Map;

public class CharClassBuilder {

	private char min;
	private char max;
	private Map<Character, CharNode> charClasses;
	
	public CharClassBuilder(char min, char max) {
		this.charClasses = new LinkedHashMap<>();
		this.min = min;
		this.max = max;
	}
	
	public CharClassBuilder add(char symbol, CharNode node) {
		charClasses.put(symbol, node);
		return this;
	}
	
	public CharClassBuilder add(SpecialCharClassNode specialCharClass) {
		charClasses.put(specialCharClass.getSymbol(), specialCharClass);
		SpecialCharClassNode invertedCharClass = specialCharClass.invert(min, max);
		charClasses.put(invertedCharClass.getSymbol(), invertedCharClass);
		return this;
	}

	public Map<Character, CharNode> build() {
		return charClasses;
	}

}
