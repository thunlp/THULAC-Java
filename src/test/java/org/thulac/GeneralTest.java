package org.thulac;

import org.junit.Test;
import org.thunlp.thulac.Thulac;
import org.thunlp.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class GeneralTest {
	@Test
	public void test() throws IOException {
		String srcDir = "src/test/resources/";
		String tmpDir = "tmp/";

		String inputFile = srcDir + "data_input.txt";
		String compareFile = srcDir + "data_seg.txt";
		String outputFile = tmpDir + "output.txt";

		long startTime = System.currentTimeMillis();
		String[] args = {"-seg_only", "-input", inputFile, "-output", outputFile};
		Thulac.main(args);
		long time = System.currentTimeMillis() - startTime;

		List<String> input = getLines(inputFile);
		List<String> output = getLines(outputFile);
		List<String> compare = getLines(compareFile);

		List<Integer> outputSeg = extractSegments(input, output);
		List<Integer> compareSeg = extractSegments(input, compare);
		int matches = (int) outputSeg.stream().filter(compareSeg::contains).count();
		int segments = outputSeg.size(), total = compareSeg.size();

		System.out.printf("Result: %d total, %d segments, %d matches, %.2f%% accuracy\n" +
						"Time elapsed: %dms\n",
				total, segments, matches, 100f * matches / total, time);
	}

	private static List<String> getLines(String fileName) throws IOException {
		return FileHelper.getLines(fileName).stream()
				.filter(line -> !line.isEmpty())
				.collect(Collectors.toList());
	}

	private static List<Integer> extractSegments(
			List<String> input, List<String> result) {
		List<Integer> segments = new ArrayList<>();
		if (input.size() != result.size()) return segments;
		for (int i = 0, size = input.size(); i < size; ++i)
			extractSegments(input.get(i), result.get(i), segments);
		return segments;
	}

	private static void extractSegments(
			String input, String result, List<Integer> segments) {
		int[] cp1 = StringUtil.toCodePoints(input),
				cp2 = StringUtil.toCodePoints(result);
		int pointer = 0, len1 = cp1.length, len2 = cp2.length;
		assertTrue("Result shorter than input!", len1 <= len2);

		for (int i = 0; i < len1; ++i) {
			int c = cp1[i];
			if (cp2[pointer++] == c) continue;
			segments.add(i);
			for (; pointer < len2 && cp2[pointer] != c; ++pointer) ;
			if (pointer == len2)
				throw new AssertionError(
						new StringBuilder("Character '").appendCodePoint(c)
								.append("' not found in result string!\n")
								.append("Input: ").append(input)
								.append("Result:").append(result).toString());
			++pointer;
		}
	}
}