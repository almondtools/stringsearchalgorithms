package net.amygdalum.util.text;

public interface CharMapping {

	public static final CharMapping IDENTITY = new CharMapping() {
		
		@Override
		public char[] map(char c) {
			return new char[]{c};
		}

		@Override
		public char[] normalized(char[] chars) {
			return chars;
		}
	};

	char[] map(char c);

	char[] normalized(char[] chars);
}
