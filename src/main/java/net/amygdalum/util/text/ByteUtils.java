package net.amygdalum.util.text;

import java.util.List;

public final class ByteUtils {

	private ByteUtils() {
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

}
