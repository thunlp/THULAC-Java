package org.thunlp.manage;

import org.thunlp.base.Dat;
import org.thunlp.base.TaggedSentence;
import org.thunlp.base.WordWithTag;
import org.thunlp.util.StringHelper;

import java.io.IOException;

public class NegWord {
	private Dat neg_dat;

	public NegWord(String filename) throws IOException {
		this.neg_dat = new Dat(filename);
	}

	public void adjust(TaggedSentence sentence) {
		if (this.neg_dat == null) return;

		for (int i = sentence.size() - 1; i >= 0; --i) {
			WordWithTag tagged = sentence.get(i);
			if (this.neg_dat.match(tagged.word) != -1) {
				WordWithTag newWord = new WordWithTag(tagged.separator);
				newWord.word = StringHelper.toString(tagged.word.codePointAt(1));
				newWord.tag = "v";
				sentence.add(i + 1, newWord);
				tagged.word = StringHelper.toString(tagged.word.codePointAt(0));
				tagged.tag = "d";
			}
		}
	}
}
