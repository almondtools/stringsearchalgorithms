package com.almondtools.stringsandchars.search;

import java.util.Collection;

public interface MultiStringSearchAlgorithmFactory {

	StringSearchAlgorithm of(Collection<String> patterns);

}
