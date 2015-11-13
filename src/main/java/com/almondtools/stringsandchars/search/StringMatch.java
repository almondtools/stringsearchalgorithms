package com.almondtools.stringsandchars.search;

public class StringMatch {

	private int start;
	private int end;
	private String text;

	public StringMatch(int start, int end, String match) {
		this.start = start;
		this.end = end;
		this.text = match;
	}

	public int start() {
		return start;
	}
	
	public int end() {
		return end;
	}
	
	public String text() {
		return text;
	}
	
	@Override
	public String toString() {
		return start + ":" + end + "(" + text + ")";
	}

	@Override
	public int hashCode() {
		return 31 + end * 13 + start * 7 + text.hashCode() * 3;
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
