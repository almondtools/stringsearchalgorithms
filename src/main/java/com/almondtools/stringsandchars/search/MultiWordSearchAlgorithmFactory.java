package com.almondtools.stringsandchars.search;

import java.util.List;

public interface MultiWordSearchAlgorithmFactory {

	StringSearchAlgorithm of(List<String> patterns);

}
