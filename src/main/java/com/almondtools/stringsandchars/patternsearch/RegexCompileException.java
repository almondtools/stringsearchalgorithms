package com.almondtools.stringsandchars.patternsearch;

public class RegexCompileException extends RuntimeException {

	public RegexCompileException(String pattern, int pos, String missing) {
		super("Regular expression " + pattern + " fails to compile at position " + pos + ", missing: " + missing);
	}

}
