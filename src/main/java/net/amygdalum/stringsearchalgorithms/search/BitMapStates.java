package net.amygdalum.stringsearchalgorithms.search;

public interface BitMapStates {

	boolean supportsSingle();

	long single(char c);
	long[] all(char c);

}
