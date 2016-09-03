package net.amygdalum.stringsearchalgorithms.io;

import java.io.IOException;

public class IORuntimeException extends RuntimeException {

	public IORuntimeException(IOException cause) {
		super(cause);
	}

}
