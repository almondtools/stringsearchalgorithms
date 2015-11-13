package com.almondtools.stringsandchars.io;

import java.io.IOException;

public class IORuntimeException extends RuntimeException {

	public IORuntimeException(IOException cause) {
		super(cause);
	}

}
