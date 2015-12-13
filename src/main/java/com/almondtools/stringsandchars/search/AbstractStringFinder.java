package com.almondtools.stringsandchars.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractStringFinder implements StringFinder {

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
			}
		}
	}
	
	@Override
	public List<StringMatch> findAllNonOverlapping() {
		List<StringMatch> matches = new ArrayList<>();
		next: while (true) {
			StringMatch match = findNext();
			if (match == null) {
				return matches;
			} else {
				List<StringMatch> backup = new LinkedList<>();
				while (!matches.isEmpty()) {
					int lastIndex = matches.size() - 1;
					StringMatch last = matches.get(lastIndex);
					if (last.start() >= match.start() && last.end() <= match.end()) {
						backup.add(matches.remove(lastIndex));
					} else if (last.end() > match.start()){
						matches.addAll(backup);
						continue next;
					} else {
						break;
					}
				}
				matches.add(match);
			}
		}
	}

}
