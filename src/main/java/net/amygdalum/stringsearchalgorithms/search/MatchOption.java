package net.amygdalum.stringsearchalgorithms.search;


public enum MatchOption implements StringFinderOption {

	LONGEST_MATCH, NON_OVERLAP, NON_EMPTY;

	public boolean in(StringFinderOption... options) {
		for (int i = 0; i < options.length; i++) {
			if (options[i] == this) {
				return true;
			}
		}
		return false;
	}
	
}
