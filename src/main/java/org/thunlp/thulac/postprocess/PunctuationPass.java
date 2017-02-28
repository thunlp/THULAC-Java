package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.Dat;
import org.thunlp.thulac.data.TaggedWord;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class PunctuationPass implements IPostprocessPass {
	private Dat p_dat;

	public PunctuationPass(String filename) throws IOException {
		this.p_dat = new Dat(filename);
	}

	@Override
	public void process(List<TaggedWord> sentence) {
		if (this.p_dat == null) return;
		if (sentence.isEmpty()) return;

		Vector<String> tmp = new Vector<>();
		for (int i = 0; i < sentence.size(); i++) {
			TaggedWord tagged = sentence.get(i);
			StringBuilder sb = new StringBuilder(tagged.word);
			if (this.p_dat.getInfo(sb.toString()) >= 0) continue;

			tmp.clear();
			for (int j = i + 1; j < sentence.size(); j++) {
				sb.append(sentence.get(j).word);
				if (this.p_dat.getInfo(sb.toString()) >= 0) break;
				tmp.add(sb.toString());
			}

			int k = tmp.size() - 1;
			for (; k >= 0 && this.p_dat.match(tmp.get(k)) != -1; k--) ;
			if (k >= 0) {
				sb.setLength(0);
				for (int j = i; j < i + k + 2; j++) sb.append(sentence.get(j).word);
				tagged.word = sb.toString();
				tagged.tag = "w";

				for (int j = i + k + 1; j > i; j--) sentence.remove(j);
			} else if (this.p_dat.match(tagged.word) != -1) tagged.tag = "w";
		}
	}
}
