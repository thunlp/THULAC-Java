package org.thunlp.manage;

import org.thunlp.base.Dat;
import org.thunlp.base.TaggedWord;
import org.thunlp.util.StringHelper;

import java.io.IOException;
import java.util.List;

public class NegWord {
	private Dat neg_dat;

	public NegWord(String filename) throws IOException {
		this.neg_dat = new Dat(filename);
	}

	public void adjust(List<TaggedWord> sentence) {
		if (this.neg_dat == null) return;

		for (int i = sentence.size() - 1; i >= 0; --i) {
			TaggedWord tagged = sentence.get(i);
			if (this.neg_dat.match(tagged.word) != -1) {
				TaggedWord newWord = new TaggedWord(tagged.separator);
				newWord.word = StringHelper.toString(tagged.word.codePointAt(1));
				newWord.tag = "v";
				sentence.add(i + 1, newWord);
				tagged.word = StringHelper.toString(tagged.word.codePointAt(0));
				tagged.tag = "d";
			}
		}
	}
}
