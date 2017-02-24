package org.thunlp.thulac;

import org.thunlp.thulac.data.POCGraph;
import org.thunlp.thulac.util.StringUtil;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class Preprocessor {
	private static final String OTHER_CODE_POINTS = StringUtil.toString(65292, 12290,
			65311, 65281, 65306, 65307, 8216, 8217, 8220, 8221, 12304, 12305,
			12289, 12298, 12299, 126, 183, 64, 124, 35, 65509, 37, 8230, 38, 42, 65288,
			65289, 8212, 45, 43, 61, 44, 46, 60, 62, 63, 47, 33, 59, 58, 39, 34, 123, 125,
			91, 93, 92, 124, 35, 36, 37, 94, 38, 42, 40, 41, 95, 45, 43, 61, 9700, 9734,
			9733, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82,
			83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106,
			107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121,
			122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57);
	private static final String SINGLE_PUNCTUATION_CODE_POINTS = StringUtil.toString(
			65292, 12290, 65311, 65281, 65306, 65307, 8216, 8217, 8220, 8221, 1230, 12304,
			12305, 12289, 12298, 12299, 64, 35, 65288, 65289, 34, 91, 93, 126, 47, 44, 58,
			63, 9700, 9734, 9733, 8230, 39, 33, 42, 43, 62, 40, 41, 59, 61);
	private static final String WHITESPACE_CODE_POINTS = StringUtil.toString(32, 12288);

	private HashMap<Integer, Integer> t2sMap;

	public Preprocessor() {
		this.t2sMap = new HashMap<>();
	}

	public boolean isSinglePunctuation(int c) {
		return SINGLE_PUNCTUATION_CODE_POINTS.indexOf(c) != -1;
	}

	public String cleanup(String sentence, POCGraph graph) {
		StringBuilder cleaned = new StringBuilder();
		graph.clear();
		boolean spaceFlag = false, otherFlag = false,
				singlePunctuationFlag = false, titleFlag = false;

		int titleStart = 0;
		int[] codePoints = StringUtil.toCodePoints(sentence);
		for (int c : codePoints) {
			if (WHITESPACE_CODE_POINTS.indexOf(c) != -1) {
				otherFlag = false;
				if (spaceFlag) continue;
				if (!graph.isEmpty())
					graph.setElementAt(graph.lastElement() & 12, graph.size() - 1);
				spaceFlag = true;
				continue;
			}

			cleaned.appendCodePoint(c);
			if (OTHER_CODE_POINTS.indexOf(c) != -1) {
				if (spaceFlag) {
					singlePunctuationFlag = this.isSinglePunctuation(c);
					graph.add(singlePunctuationFlag ? 8 : 9);
					spaceFlag = false;
				} else {
					if (otherFlag) {
						if (this.isSinglePunctuation(c)) {
							if (!graph.isEmpty()) graph.setElementAt(
									graph.lastElement() & 12, graph.size() - 1);
							graph.add(8);
						} else if (singlePunctuationFlag) graph.add(9);
						else {
							if (!graph.isEmpty() && graph.lastElement() == 0)
								graph.setElementAt(7, graph.size() - 1);
							graph.add(2);
						}
					} else graph.add(9);
					singlePunctuationFlag = this.isSinglePunctuation(c);
				}
				otherFlag = true;

				if (c == 12298) titleStart = graph.size();
				else if (c == 12299 && titleFlag) {
					int titleEnd = graph.size() - 2;
					if (titleEnd <= titleStart + 9)
						if (titleStart == titleEnd) graph.setElementAt(9, titleStart);
						else {
							graph.setElementAt(1, titleStart);
							for (int i = titleStart + 1; i < titleEnd; ++i)
								graph.setElementAt(2, i);
							graph.setElementAt(4, titleEnd);
						}
				}
				titleFlag = c == 12298;
			} else {
				if (spaceFlag) graph.add(9);
				else if (otherFlag) {
					graph.setElementAt(graph.lastElement() & 12, graph.size() - 1);
					graph.add(9);
					singlePunctuationFlag = false;
				} else graph.add(15);
				spaceFlag = false;
				otherFlag = false;
			}
		}

		// deal with first & last character
		if (!graph.isEmpty()) {
			int first = graph.firstElement() & 9, last = graph.lastElement() & 12;
			graph.setElementAt(first == 0 ? 9 : first, 0);
			graph.setElementAt(last == 0 ? 12 : last, graph.size() - 1);
		}

		return cleaned.toString();
	}

	public void loadT2SMap(String filename) throws IOException {
		File mapFile = new File(filename);
		int recordCount = (int) (mapFile.length() >> 3);
		DataInputStream input = new DataInputStream(new FileInputStream(mapFile));
		int[] traditional = new int[recordCount];
		for (int i = 0; i < recordCount; ++i) traditional[i] = input.readInt();
		for (int i = 0; i < recordCount; ++i) {
			int simplified = input.readInt();
			this.t2sMap.put(traditional[i], simplified);
		}
		input.close();
	}

	private int getSimplifiedCodePoint(int c) {
		if (this.t2sMap.containsKey(c)) return this.t2sMap.get(c);
		return c;
	}

	public String convertT2S(String sentence) {
		int[] codePoints = StringUtil.toCodePoints(sentence);
		StringBuilder sb = new StringBuilder();
		for (int codePoint : codePoints)
			sb.appendCodePoint(this.getSimplifiedCodePoint(codePoint));
		return sb.toString();
	}
}
