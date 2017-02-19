package org.thunlp.manage;

import org.thunlp.base.Dat;
import org.thunlp.base.TaggedSentence;
import org.thunlp.util.StringHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Filter {
	private static final Set<String> ALLOWED_TAGS = new HashSet<>(Arrays.asList(
			"n", "np", "ns", "ni", "nz", "v", "a", "id", "t", "uw"));
	private static final String ARABIC_NUMBER_CODE_POINTS =
			StringHelper.toString(48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
					65296, 65297, 65298, 65299, 65300, 65301, 65302, 65303, 65304, 65305);
	private static final String CHINESE_NUMBER_CODE_POINTS =
			StringHelper.toString(12295, 19968, 20108, 19977, 22235,
					20116, 20845, 19971, 20843, 20061);

	private Dat xu_dat;
	private Dat time_dat;

	public Filter(String xuWordFile, String timeWordFile) throws IOException {
		this.xu_dat = new Dat(xuWordFile);
		this.time_dat = new Dat(timeWordFile);
	}

	public void adjust(TaggedSentence sentence) {
		if (this.xu_dat == null || this.time_dat == null) return;

		for (int i = sentence.size() - 1; i >= 0; --i) {
			String word = sentence.get(i).word;
			String tag = sentence.get(i).tag;

			if (ALLOWED_TAGS.contains(tag)) {
				if (this.xu_dat.match(word) != -1)
					sentence.remove(i);
				else if ("t".equals(tag)) {
					int count = 0;
					boolean hasArabicNum = false, hasChineseNum = false;
					int length = word.codePointCount(0, word.length());
					for (int j = 0; j < length; j++) {
						int cp = word.codePointAt(j);
						if (ARABIC_NUMBER_CODE_POINTS.indexOf(cp) != -1) {
							hasArabicNum = true;
							break;
						}
						if (CHINESE_NUMBER_CODE_POINTS.indexOf(cp) != -1) {
							++count;
							if (count == 2) {
								hasChineseNum = true;
								break;
							}
						}
					}
					if (hasArabicNum || hasChineseNum ||
							(this.time_dat.match(word) != -1)) sentence.remove(i);
				}
			} else sentence.remove(i);
		}
	}
}
