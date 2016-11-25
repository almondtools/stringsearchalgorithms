package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;

public class MatchBuilder implements MatchListener {
	
	private boolean longest;
	private SortedSet<StringMatch> matches;

	public MatchBuilder(boolean longest) {
		this.longest = longest;
		this.matches = new TreeSet<>();
	}

	@Override
	public void notify(long start, long end, CharProvider chars) {
		String s = chars.slice(start, end);
		matches.add(new StringMatch(start, end, s));
	}
	
	public SortedSet<StringMatch> getMatches() {
		if (longest && !matches.isEmpty()) {
			List<StringMatch> toRemove = new ArrayList<>();
			Iterator<StringMatch> matchIterator = matches.iterator();
			StringMatch longestMatch = matchIterator.next();
			while (matchIterator.hasNext()) {
				StringMatch currentMatch = matchIterator.next();
				if (currentMatch.start() > longestMatch.start()) {
					longestMatch = currentMatch;
				} else if (currentMatch.length() > longestMatch.length()) {
					toRemove.add(longestMatch);
				} else {
					toRemove.add(currentMatch);
				}
			}
			matches.removeAll(toRemove);
		}
		return matches;
	}

}