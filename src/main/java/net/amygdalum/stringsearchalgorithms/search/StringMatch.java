package net.amygdalum.stringsearchalgorithms.search;


/**
 * a string match, that means a textual subsequence of a document matching a specific pattern
 */
public class StringMatch implements Comparable<StringMatch> {

	private long start;
	private long end;
	private String text;

	/**
	 * @param start the start of the match
	 * @param end the end of the match
	 * @param match the matched string
	 */
	public StringMatch(long start, long end, String match) {
		this.start = start;
		this.end = end;
		this.text = match;
	}

	public long start() {
		return start;
	}

	public long end() {
		return end;
	}

	public String text() {
		return text;
	}

	public boolean isEmpty() {
		return start == end;
	}

	public int length() {
		return text.length();
	}

	@Override
	public int compareTo(StringMatch o) {
		long start = this.start - o.start;
		if (start != 0) {
			return start < 0 ? -1 : 1;
		}
		long end = this.end - o.end;
		if (end != 0) {
			return end < 0 ? -1 : 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return start + ":" + end + "(" + text + ")";
	}

	@Override
	public int hashCode() {
		return 31 + (int) end * 13 + (int) start * 7 + text.hashCode() * 3;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StringMatch that = (StringMatch) obj;
		return this.start == that.start
			&& this.end == that.end
			&& this.text.equals(that.text);
	}

}
