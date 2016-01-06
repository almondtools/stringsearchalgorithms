package com.almondtools.stringsandchars.search;

import java.util.Collection;

public interface MultiWordSearchAlgorithmFactory {

	StringSearchAlgorithm of(Collection<String> patterns);

}
