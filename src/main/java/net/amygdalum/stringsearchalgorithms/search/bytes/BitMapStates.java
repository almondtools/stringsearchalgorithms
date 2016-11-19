package net.amygdalum.stringsearchalgorithms.search.bytes;

public interface BitMapStates {

	boolean supportsSingle();

	long single(byte b);
	long[] all(byte b);

}
