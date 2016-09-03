package net.amygdalum.stringsearchalgorithms.search;

import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;

import net.amygdalum.stringsearchalgorithms.io.CharProvider;

public class EmptyMatchFinder extends BufferedStringFinder {

	private StringFinder finder;
	private CharProvider chars;
	private boolean longest;

	public EmptyMatchFinder(StringFinder finder, CharProvider chars, StringFinderOption... options) {
		super(options);
		this.finder = finder;
		this.chars = chars;
		this.longest = LONGEST_MATCH.in(options);
	}

	@Override
	public StringMatch findNext() {
		if (isBufferEmpty()) {
			long start = chars.current();
			StringMatch next = finder.findNext();
			if (next != null) {
				push(next);
				for (long pos = start; pos < next.start(); pos++) {
					push(new StringMatch(pos, pos, ""));
				}
			} else {
				for (long pos = start; pos < chars.current(); pos++) {
					push(new StringMatch(pos, pos, ""));
				}
			}
		}
		if (longest) {
			return longestLeftMost();
		} else {
			return leftMost();
		}
	}

	@Override
	public void skipTo(long pos) {
		finder.skipTo(pos);
		clear();
	}

}
