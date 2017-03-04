package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.Dat;
import org.thunlp.thulac.data.DatMaker;
import org.thunlp.thulac.data.TaggedWord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A postprocess pass which scans the word list, extract words that are found in the
 * dictionary and tag them.
 */
public class PostprocessPass implements IPostprocessPass {
	// TODO: add more documentation

	private Dat dictDat;
	private String tag;

	public PostprocessPass(String filename, String tag, boolean isTxt) throws
			IOException {
		this.tag = tag;
		if (isTxt) this.dictDat = DatMaker.readFromTxtFile(filename);
		else this.dictDat = new Dat(filename);
	}

	@Override
	public void process(List<TaggedWord> sentence) {
		if (this.dictDat == null) return;
		if (sentence.isEmpty()) return;

		List<String> tmp = new ArrayList<>();
		for (int i = 0; i < sentence.size(); i++) {
			TaggedWord tagged = sentence.get(i);
			StringBuilder sb = new StringBuilder(tagged.word);
			if (this.dictDat.getInfo(sb.toString()) >= 0) continue;

			tmp.clear();
			for (int j = i + 1; j < sentence.size(); j++) {
				sb.append(sentence.get(j).word);
				if (this.dictDat.getInfo(sb.toString()) >= 0) break;
				tmp.add(sb.toString());
			}

			int k = tmp.size() - 1;
			for (; k >= 0 && !this.dictDat.contains(tmp.get(k)); k--) ;
			if (k >= 0) {
				sb.setLength(0);
				for (int j = i; j < i + k + 2; j++) sb.append(sentence.get(j).word);
				tagged.word = sb.toString();
				tagged.tag = this.tag;

				for (int j = i + k + 1; j > i; j--) sentence.remove(j);
			}
		}
	}
}
