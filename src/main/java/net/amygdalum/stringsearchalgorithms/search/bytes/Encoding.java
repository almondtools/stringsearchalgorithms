package net.amygdalum.stringsearchalgorithms.search.bytes;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

public final class Encoding {

	public static byte[] encode(String pattern, Charset charset) {
		try {
			return charset.newEncoder().encode(CharBuffer.wrap(pattern)).array();
		} catch (CharacterCodingException e) {
			return new byte[0];
		}
	}

}
