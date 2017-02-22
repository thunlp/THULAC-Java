package org.thulac;

import org.junit.Test;
import org.thunlp.thulac.Thulac;
import org.thunlp.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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

		int lines = input.size();
		List<List<Integer>> outputSeg = extractSegments(input, output);
		List<List<Integer>> compareSeg = extractSegments(input, compare);
		int matches = 0, segments = outputSeg.stream().mapToInt(List::size).sum(),
				total = compareSeg.stream().mapToInt(List::size).sum();
		for (int i = 0; i < lines; ++i) {
			List<Integer> outputLine = outputSeg.get(i);
			List<Integer> compareLine = compareSeg.get(i);
			matches += outputLine.stream().filter(compareLine::contains).count();
		}

		System.out.printf("Result: %d total, %d segments, %d matches, %.2f%% accuracy\n" +
						"Time elapsed: %dms\n",
				total, segments, matches, 100f * matches / total, time);
	}

	private static List<String> getLines(String fileName) throws IOException {
		return FileHelper.getLines(fileName).stream()
				.filter(line -> !line.isEmpty())
				.collect(Collectors.toList());
	}

	private static List<List<Integer>> extractSegments(
			List<String> input, List<String> result) {
		List<List<Integer>> segments = new ArrayList<>();
		assertEquals("Line count of input and result doesn't match",
				input.size(), result.size());
		for (int i = 0, size = input.size(); i < size; ++i)
			segments.add(extractSegments(input.get(i), result.get(i)));
		return segments;
	}

	private static List<Integer> extractSegments(
			String input, String result) {
		List<Integer> segments = new ArrayList<>();
		int[] cp1 = StringUtil.toCodePoints(input),
				cp2 = StringUtil.toCodePoints(result);
		int pointer = 0, len1 = cp1.length, len2 = cp2.length;
		assertTrue("Result shorter than input!", len1 <= len2);

		int i = 0;
		for (; i < len1 && pointer < len2; ++i, ++pointer) {
			int c = cp1[i];
			if (cp2[pointer] == c) continue;
			segments.add(i);
			for (; pointer < len2 && cp2[pointer] != c; ++pointer) ;
			if (pointer == len2)
				throw new AssertionError(
						new StringBuilder("Character '").appendCodePoint(c)
								.append("' not found in result string!\n")
								.append("Input: ").append(input)
								.append("Result:").append(result).toString());
		}
		if (i != len1) throw new AssertionError(
				new StringBuilder("Character '").appendCodePoint(cp1[i])
						.append("' not found in result string!\n")
						.append("Input: ").append(input)
						.append("Result:").append(result).toString());

		return segments;
	}
}