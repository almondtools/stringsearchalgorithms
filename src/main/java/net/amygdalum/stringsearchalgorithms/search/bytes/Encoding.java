package net.amygdalum.stringsearchalgorithms.search.bytes;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

public final class Encoding {

	public static byte[] encode(String pattern, Charset charset) {
		try {
			ByteBuffer buffer = charset.newEncoder().encode(CharBuffer.wrap(pattern));
			byte[] encoded = new byte[buffer.limit()];
			buffer.get(encoded);
			return encoded;
		} catch (CharacterCodingException e) {
			return new byte[0];
		}
	}

}
