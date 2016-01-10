package com.almondtools.stringsandchars.patternsearch;

import java.util.ArrayList;
import java.util.List;

import com.almondtools.stringsandchars.io.CharProvider;
import com.almondtools.stringsandchars.search.StringMatch;

public class MatchBuilder implements MatchListener {
	
	private boolean longest;
	private List<StringMatch> matches;

	public MatchBuilder(boolean longest) {
		this.longest = longest;
		this.matches = new ArrayList<>();
	}

	@Override
	public void notify(long start, long end, CharProvider chars) {
		matches.add(new StringMatch(start, end, chars.slice(start, end)));
	}
	
	public List<StringMatch> getMatches() {
		if (longest && !matches.isEmpty()) {
			return matches.subList(matches.size() -1, matches.size());
		} else {
			return matches;
		}
	}

}