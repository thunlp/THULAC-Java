package org.thunlp.util;

/**
 *
 */
public class StringUtil {
	public static String toString(int... codePoints) {
		StringBuilder sb = new StringBuilder();
		for (int codePoint : codePoints) sb.append(codePoint);
		return sb.toString();
	}
}
