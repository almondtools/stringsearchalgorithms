package com.almondtools.stringsandchars.search;

import static com.almondtools.stringsandchars.search.MatchOption.NO_OVERLAP;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStringFinder implements StringFinder {

	private boolean nonOverlap;
	
	public AbstractStringFinder(StringFinderOption... options) {
		nonOverlap = NO_OVERLAP.in(options);
	}
	
	public abstract StringMatch findNext();

	public abstract void skipTo(long pos);

	@Override
	public List<StringMatch> findAll() {
		List<StringMatch> matches = new ArrayList<>();
		while (true) {
			StringMatch match = findNext();
			if (match == null) {
				return matches;
			} else {
				matches.add(match);
				if (nonOverlap) {
					skipTo(match.end());
				}
			}
		}
	}
	
}
