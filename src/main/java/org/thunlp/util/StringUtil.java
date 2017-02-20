package org.thunlp.util;

/**
 *
 */
public class StringUtil {
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
}
