package net.amygdalum.stringsearchalgorithms.io;

import java.util.Objects;

public class ComparingCharProvider implements CharProvider {

	private CharProvider c1;
	private CharProvider c2;

	public ComparingCharProvider(CharProvider charprovider1, CharProvider charprovider2) {
		this.c1 = charprovider1;
		this.c2 = charprovider2;
	}

	@Override
	public char next() {
		char result1 = c1.next();
		char result2 = c2.next();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char lookahead() {
		char result1 = c1.lookahead();
		char result2 = c2.lookahead();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char lookahead(int i) {
		char result1 = c1.lookahead(i);
		char result2 = c2.lookahead(i);
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char prev() {
		char result1 = c1.prev();
		char result2 = c2.prev();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char lookbehind() {
		char result1 = c1.lookbehind();
		char result2 = c2.lookbehind();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char lookbehind(int i) {
		char result1 = c1.lookbehind(i);
		char result2 = c2.lookbehind(i);
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public long current() {
		long result1 = c1.current();
		long result2 = c2.current();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public void move(long i) {
		c1.move(i);
		c2.move(i);
	}

	@Override
	public char[] between(long start, long end) {
		char[] result1 = c1.between(start, end);
		char[] result2 = c2.between(start, end);
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public String slice(long start, long end) {
		String result1 = c1.slice(start, end);
		String result2 = c2.slice(start, end);
		if (Objects.equals(result1, result2)) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public void forward(int i) {
		c1.forward(i);
		c2.forward(i);
	}

	@Override
	public void finish() {
		c1.finish();
		c2.finish();
	}

	@Override
	public boolean finished() {
		boolean result1 = c1.finished();
		boolean result2 = c2.finished();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public boolean finished(int i) {
		boolean result1 = c1.finished(i);
		boolean result2 = c2.finished(i);
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public char at(long i) {
		char result1 = c1.at(i);
		char result2 = c2.at(i);
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public void mark() {
		c1.mark();
		c2.mark();
	}

	@Override
	public boolean changed() {
		boolean result1 = c1.changed();
		boolean result2 = c2.changed();
		if (result1 == result2) {
			return result1;
		} else {
			throw new RuntimeException();
		}
	}

}
