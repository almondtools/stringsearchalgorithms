package com.almondtools.rexlex.stringsearch;

import java.util.List;

public interface MultiWordSearchAlgorithmFactory {

	StringSearchAlgorithm of(List<String> patterns);

}
