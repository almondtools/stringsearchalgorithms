package net.amygdalum.stringsearchalgorithms.search.chars;

import java.util.Collection;

public interface MultiStringSearchAlgorithmFactory {

	StringSearchAlgorithm of(Collection<String> patterns);

}
