package com.almondtools.stringsandchars.search;

public interface BitMapStates {

	boolean supportsSingle();

	long single(char c);
	long[] all(char c);

}
