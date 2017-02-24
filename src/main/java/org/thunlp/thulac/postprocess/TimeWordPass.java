package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.TaggedWord;
import org.thunlp.thulac.util.StringUtil;

import java.util.List;

public class TimeWordPass implements IPostprocessPass {
	private static final String ARABIC_NUMBER_CODE_POINTS =
			StringUtil.toString(48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
					65296, 65297, 65298, 65299, 65300, 65301, 65302, 65303, 65304, 65305);
	private static final String TIME_WORD_CODE_POINTS =
			StringUtil.toString(24180, 26376, 26085, 21495, 26102, 28857, 20998, 31186);
	private static final String OTHER_CODE_POINTS =
			StringUtil.toString(65292, 12290, 65311, 65281, 65306, 65307, 8216, 8217,
					8220, 8221, 12304, 12305, 12289, 12298, 12299, 126, 183, 64, 124, 35,
					65509, 37, 8230, 38, 42, 65288, 65289, 8212, 45, 43, 61, 44, 46, 60,
					62, 63, 47, 33, 59, 58, 39, 34, 123, 125, 91, 93, 92, 124, 35, 36, 37,
					94, 38, 42, 40, 41, 95, 45, 43, 61, 9700, 9734, 9733, 65, 66, 67,
					68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
					85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105,
					106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
					120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57);

	private boolean isArabicNum(String word) {
		int len = word.codePointCount(0, word.length());
		for (int i = 0; i < len; i++)
			if (ARABIC_NUMBER_CODE_POINTS.indexOf(word.codePointAt(i)) == -1)
				return false;
		return true;
	}

	private boolean isTimeWord(String word) {
		return word.length() == 1 && TIME_WORD_CODE_POINTS.indexOf(word.charAt(0)) != -1;
	}

	private boolean isDoubleWord(String word, String postWord) {
		if (word.length() != 1 || postWord.length() != 1) return false;
		else {
			int wordInt = word.codePointAt(0);
			int postWordInt = postWord.codePointAt(0);
			return wordInt == postWordInt && OTHER_CODE_POINTS.indexOf(wordInt) != -1;
		}
	}

	private boolean isHttpWord(String word) {
		return word.length() >= 5 && word.startsWith("http");
	}

	@Override
	public void process(List<TaggedWord> sentence) {
		this.processTimeWords(sentence);
		this.processDoubleWords(sentence);
		this.processHttpWords(sentence);
		this.processMailAddress(sentence);
	}

	private void processDoubleWords(List<TaggedWord> sentence) {
		TaggedWord tagged, last = sentence.get(sentence.size() - 1);
		for (int i = sentence.size() - 2; i >= 0; i--) {
			tagged = sentence.get(i);
			if (this.isDoubleWord(tagged.word, last.word)) {
				tagged.word += last.word;
				sentence.remove(i + 1);
			}
			last = tagged;
		}
	}

	private void processTimeWords(List<TaggedWord> sentence) {
		boolean hasTimeWord = false;
		for (int i = sentence.size() - 1; i >= 0; i--) {
			TaggedWord tagged = sentence.get(i);
			if (this.isTimeWord(tagged.word)) hasTimeWord = true;
			else if (hasTimeWord) {
				if (this.isArabicNum(tagged.word)) {
					tagged.word += sentence.remove(i + 1).word;
					tagged.tag = "t";
				} else hasTimeWord = false;
			}
		}
	}

	private void processHttpWords(List<TaggedWord> sentence) {
		for (TaggedWord tagged : sentence)
			if (this.isHttpWord(tagged.word)) tagged.tag = "x";
	}

	private void processMailAddress(List<TaggedWord> sentence) {
		TaggedWord last = sentence.get(0), tagged;
		for (int i = 1, size = sentence.size(); i < size; i++) {
			tagged = sentence.get(i);
			if ("@".equals(last.word) && !"@".equals(tagged.word)) tagged.tag = "np";
			last = tagged;
		}
	}
}
