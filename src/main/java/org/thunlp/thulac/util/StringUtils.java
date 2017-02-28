package org.thunlp.thulac.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class StringUtils {
	public static String toString(int... codePoints) {
		StringBuilder sb = new StringBuilder();
		for (int codePoint : codePoints) sb.appendCodePoint(codePoint);
		return sb.toString();
	}

	public static int[] toCodePoints(String str) {
		if (str == null) return null;
		int codePointCount = str.codePointCount(0, str.length());
		int[] codePoints = new int[codePointCount];
		for (int i = 0; i < codePointCount; ++i)
			codePoints[i] = str.codePointAt(str.offsetByCodePoints(0, i));
		return codePoints;
	}

	public static Reader toReader(String str, Charset charset) {
		if (charset == null) charset = StandardCharsets.UTF_8;
		return new InputStreamReader(
				new ByteArrayInputStream(str.getBytes(charset)), charset);
	}
}
