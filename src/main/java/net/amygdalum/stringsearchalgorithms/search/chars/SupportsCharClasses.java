package net.amygdalum.stringsearchalgorithms.search.chars;

import net.amygdalum.util.text.CharMapping;

public interface SupportsCharClasses<T extends StringSearchAlgorithmFactory> {

	T withCharClasses(CharMapping mapping);
}
