package net.amygdalum.util.text;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.List;

public final class ByteUtils {

	public static final int BYTE_RANGE = 1 << 8;

	private ByteUtils() {
	}

	public static byte after(byte b) {
		return (byte) (b + 1);
	}

	public static byte before(byte b) {
		return (byte) (b - 1);
	}

	public static byte[] revert(byte[] bytes) {
		final int ri = bytes.length - 1;
		byte[] reversebytes = new byte[bytes.length];
		for (int i = 0; i < reversebytes.length; i++) {
			reversebytes[i] = bytes[ri - i];
		}
		return reversebytes;
	}

	public static int lastIndexOf(byte[] pattern, byte[] block) {
		nextPos: for (int i = pattern.length - block.length; i >= 0; i--) {
			for (int j = block.length - 1; j >= 0; j--) {
				if (pattern[j + i] == block[j]) {
					continue;
				} else {
					continue nextPos;
				}
			}
			return i;
		}
		return -1;
	}

	public static int minLength(List<byte[]> patterns) {
		int len = Integer.MAX_VALUE;
		for (byte[] pattern : patterns) {
			if (pattern.length < len) {
				len = pattern.length;
			}
		}
		return len;
	}

	public static int maxLength(List<byte[]> patterns) {
		int len = Integer.MIN_VALUE;
		for (byte[] pattern : patterns) {
			if (pattern.length > len) {
				len = pattern.length;
			}
		}
		return len;
	}

	public static byte[] encode(String pattern) {
		return encode(pattern, UTF_8);
	}

	public static byte[] encode(String pattern, Charset charset) {
		try {
			CharsetEncoder encoder = charset.newEncoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
			ByteBuffer buffer = encoder.encode(CharBuffer.wrap(pattern));
			byte[] encoded = new byte[buffer.limit()];
			buffer.get(encoded);
			return encoded;
		} catch (CharacterCodingException e) {
			return new byte[0];
		}
	}

	public static String decode(byte[] pattern) {
		return decode(pattern, UTF_8);
	}

	public static String decode(byte[] pattern, Charset charset) {
		try {
			CharsetDecoder decoder = charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
			CharBuffer buffer = decoder.decode(ByteBuffer.wrap(pattern));
			return buffer.toString();
		} catch (CharacterCodingException e) {
			return "";
		}
	}
}
