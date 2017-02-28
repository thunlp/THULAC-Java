package org.thunlp.thulac.util;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class InputProviderUtils {
	private static final int MAX_LENGTH = 20000;
	private static final Pattern SPLIT_PATTERN =
			Pattern.compile(".*([\u3002\uff1f\uff01\uff1b;!?]|$)");

	public static List<String> getLineSegments(String line) {
		List<String> lineSegments = new Vector<>();
		if (line.length() < MAX_LENGTH) lineSegments.add(line);
		else { // split the line into short line segments
			Matcher matcher = SPLIT_PATTERN.matcher(line);
			while (matcher.find()) lineSegments.add(matcher.group());
		}
		return lineSegments;
	}
}
