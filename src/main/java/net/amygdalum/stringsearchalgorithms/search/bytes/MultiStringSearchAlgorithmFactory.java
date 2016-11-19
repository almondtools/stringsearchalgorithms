package net.amygdalum.stringsearchalgorithms.search.bytes;

import java.util.Collection;

public interface MultiStringSearchAlgorithmFactory {

	StringSearchAlgorithm of(Collection<String> patterns);

}
