package net.amygdalum.stringsearchalgorithms.io;

public class CaseInsensitiveCharProvider extends MappingCharProvider {

	public CaseInsensitiveCharProvider(CharProvider chars) {
		super(chars);
	}

	@Override
	protected char map(char base) {
		return Character.toLowerCase(base);
	}

}
