package com.almondtools.stringsandchars.regex;

public enum RegexParserOption {

	DOT_ALL;

	public boolean in(RegexParserOption... options) {
		for (int i = 0; i < options.length; i++) {
			if (options[i] == this) {
				return true;
			}
		}
		return false;
	}

}
