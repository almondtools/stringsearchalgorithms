package net.amygdalum.stringsearchalgorithms.search;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStringFinder implements StringFinder {

	private boolean nonOverlap;
	
	public AbstractStringFinder(StringFinderOption... options) {
		nonOverlap = NON_OVERLAP.in(options);
	}
	
	public abstract StringMatch findNext();

	public abstract void skipTo(long pos);

	@Override
	public List<StringMatch> findAll() {
		List<StringMatch> matches = new ArrayList<>();
		long last = -1;
		while (true) {
			StringMatch match = findNext();
			if (match == null) {
				return matches;
			} else {
				matches.add(match);
				if (nonOverlap && match.end() > last) {
					last = match.end(); 
					skipTo(last);
				}
			}
		}
	}
	
}
