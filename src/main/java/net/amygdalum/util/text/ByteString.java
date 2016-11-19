package net.amygdalum.util.text;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteString {

	private byte[] bytes;
	private Charset charset;

	public ByteString(byte[] bytes, Charset charset) {
		this.bytes = bytes;
		this.charset = charset;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int length() {
		return bytes.length;
	}

	public Charset charset() {
		return charset;
	}

	public String getString() {
		return new String(bytes, charset);
	}

	public String getMappableSuffix() {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		while (buffer.hasRemaining()) {
			try {
				return charset.newDecoder().decode(buffer).toString();
			} catch (CharacterCodingException e) {
				buffer.get();
			}
		}
		return "";
	}

	public String getMappablePrefix() {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		while (buffer.hasRemaining()) {
			try {
				return charset.newDecoder().decode(buffer).toString();
			} catch (CharacterCodingException e) {
				buffer.rewind();
				buffer.limit(buffer.limit() - 1);
			}
		}
		return "";
	}

	public boolean isMappable() {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		if (buffer.hasRemaining()) {
			try {
				charset.newDecoder().decode(buffer);
				return true;
			} catch (CharacterCodingException e) {
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean equals(byte[] bytes) {
		return Arrays.equals(this.bytes, bytes);
	}

	public ByteString revert() {
		return new ByteString(ByteUtils.revert(bytes), charset);
	}

}
