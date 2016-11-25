package net.amygdalum.stringsearchalgorithms.io;

public abstract class MappingCharProvider implements CharProvider {

	private CharProvider chars;

	public MappingCharProvider(CharProvider chars) {
		this.chars = chars;
	}

	protected abstract char map(char base);

	public char next() {
		return map(chars.next());
	}

	public char lookahead() {
		return map(chars.lookahead());
	}

	public char lookahead(int i) {
		return map(chars.lookahead(i));
	}

	public char prev() {
		return map(chars.prev());
	}

	public char lookbehind() {
		return map(chars.lookbehind());
	}

	public char lookbehind(int i) {
		return map(chars.lookbehind(i));
	}

	public long current() {
		return chars.current();
	}

	public void move(long i) {
		chars.move(i);
	}

	public char[] between(long start, long end) {
		char[] between = chars.between(start, end);
		char[] lc = new char[between.length];
		for (int i = 0; i < lc.length; i++) {
			lc[i] = map(between[i]);
		}
		return lc;
	}

	public String slice(long start, long end) {
		return chars.slice(start, end);
	}

	public void forward(int i) {
		chars.forward(i);
	}

	public void finish() {
		chars.finish();
	}

	public boolean finished() {
		return chars.finished();
	}

	public boolean finished(int i) {
		return chars.finished(i);
	}

	public char at(long i) {
		return map(chars.at(i));
	}

	public void mark() {
		chars.mark();
	}

	public boolean changed() {
		return chars.changed();
	}
	
	@Override
	public String toString() {
		return chars.toString();
	}
	
}
