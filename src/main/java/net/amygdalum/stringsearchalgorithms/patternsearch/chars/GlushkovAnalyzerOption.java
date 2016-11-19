package net.amygdalum.stringsearchalgorithms.patternsearch.chars;


public enum GlushkovAnalyzerOption {

	SELF_LOOP, FACTORS;

	public boolean in(GlushkovAnalyzerOption[] options) {
		for (int i = 0; i < options.length; i++) {
			if (options[i] == this) {
				return true;
			}
		}
		return false;
	}

}
