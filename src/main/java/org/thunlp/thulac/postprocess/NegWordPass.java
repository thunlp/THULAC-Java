package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.Dat;
import org.thunlp.thulac.data.TaggedWord;
import org.thunlp.thulac.util.StringUtils;

import java.io.IOException;
import java.util.List;

public class NegWordPass implements IPostprocessPass {
	private Dat neg_dat;

	public NegWordPass(String filename) throws IOException {
		this.neg_dat = new Dat(filename);
	}

	@Override
	public void process(List<TaggedWord> sentence) {
		if (this.neg_dat == null) return;
		if (sentence.isEmpty()) return;

		for (int i = sentence.size() - 1; i >= 0; --i) {
			TaggedWord tagged = sentence.get(i);
			if (this.neg_dat.match(tagged.word) != -1) {
				TaggedWord newWord = new TaggedWord(tagged.separator);
				newWord.word = StringUtils.toString(
						tagged.word.codePointAt(tagged.word.offsetByCodePoints(0, 1)));
				newWord.tag = "v";
				sentence.add(i + 1, newWord);
				tagged.word = StringUtils.toString(tagged.word.codePointAt(0));
				tagged.tag = "d";
			}
		}
	}
}
