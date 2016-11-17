package net.amygdalum.stringsearchalgorithms.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class StringByteProvider implements ByteProvider {

	private static final int NO_MARK = -1;

	private CharsetDecoder decoder;
	private ByteBuffer encoded;
	private int mark;

	public StringByteProvider(String input, int start) {
		this(input, start, StandardCharsets.UTF_16LE);
	}

	public StringByteProvider(String input, int start, Charset charset) {
		this.encoded = encode(charset, input);
		this.decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE);
		this.encoded.position((int) start);
		this.mark = NO_MARK;
	}

	private static ByteBuffer encode(Charset charset, String str) {
		CharBuffer chars = CharBuffer.wrap(str);
		CharsetEncoder encoder = charset.newEncoder();
		try {
			return encoder.encode(chars);
		} catch (CharacterCodingException e) {
			return ByteBuffer.allocate(0);
		}
	}

	public void restart() {
		encoded.position(0);
	}

	@Override
	public void finish() {
		encoded.position(encoded.limit());
	}

	@Override
	public byte next() {
		return encoded.get();
	}

	@Override
	public byte lookahead() {
		return encoded.get(encoded.position());
	}

	@Override
	public byte lookahead(int i) {
		return encoded.get(encoded.position() + i);
	}

	@Override
	public byte prev() {
		int pos = encoded.position() - 1;
		encoded.position(pos);
		return encoded.get(pos);
	}

	@Override
	public byte lookbehind() {
		return encoded.get(encoded.position() - 1);
	}

	@Override
	public byte lookbehind(int i) {
		return encoded.get(encoded.position() - i - 1);
	}

	@Override
	public long current() {
		return encoded.position();
	}

	@Override
	public void move(long i) {
		encoded.position((int) i);
	}

	@Override
	public void forward(int i) {
		encoded.position(encoded.position() + i);
	}

	@Override
	public boolean finished() {
		return !encoded.hasRemaining();
	}

	@Override
	public boolean finished(int i) {
		return encoded.remaining() <= i;
	}

	@Override
	public byte at(long i) {
		return encoded.get((int) i);
	}

	@Override
	public byte[] between(long start, long end) {
		int len = (int) (end - start);
		byte[] between = new byte[len];
		int limit = encoded.limit();
		int pos = encoded.position();
		encoded.position((int) start);
		encoded.limit((int) end);
		encoded.get(between);
		encoded.position(pos);
		encoded.limit(limit);
		return between;
	}

	@Override
	public String slice(long start, long end) {
		int limit = encoded.limit();
		int pos = encoded.position();
		encoded.position((int) start);
		encoded.limit((int) end);
		ByteBuffer slice = encoded.slice();
		encoded.position(pos);
		encoded.limit(limit);
		try {
			return decoder.decode(slice).toString();
		} catch (CharacterCodingException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		long splitPoint = current();
		StringBuilder buffer = new StringBuilder(slice(0, splitPoint));
		
		if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) == decoder.replacement().charAt(0)) {
			buffer.deleteCharAt(buffer.length() - 1);
			buffer.append("~|~");
			splitPoint++;
		} else {
			buffer.append('|');
		}
		if (splitPoint < encoded.limit()) {
			buffer.append(slice(splitPoint, encoded.limit()));
		}
		return buffer.toString();
	}

	@Override
	public void mark() {
		mark = encoded.position();
	}

	@Override
	public boolean changed() {
		boolean changed = mark != NO_MARK && mark != encoded.position();
		mark = NO_MARK;
		return changed;
	}

}
