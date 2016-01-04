package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.List;

public enum MatchOption implements StringFinderOption {

	LONGEST_MATCH, NO_OVERLAP, NON_EMPTY;

	public boolean in(StringFinderOption[] options) {
		for (int i = 0; i < options.length; i++) {
			if (options[i] == this) {
				return true;
			}
		}
		return false;
	}

	public StringFinderOption[] removeFrom(StringFinderOption[] options) {
		List<StringFinderOption> newoptions = new ArrayList<>(options.length);
		for (int i = 0; i < options.length; i++) {
			if (options[i] != this) {
				newoptions.add(options[i]);
			}
		}
		return newoptions.toArray(new StringFinderOption[0]);
	}
	
}
