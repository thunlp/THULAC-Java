package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.Dat;
import org.thunlp.thulac.data.TaggedWord;

import java.io.IOException;
import java.util.List;

public class VerbPass implements IPostprocessPass {
	// TODO: add more documentation

	private Dat vM_dat;
	private Dat vD_dat;
	private String tag;

	public VerbPass(String filename, String filename2) throws IOException {
		this.vM_dat = new Dat(filename);
		this.vD_dat = new Dat(filename2);
		this.tag = "v";
	}

	@Override
	public void process(List<TaggedWord> sentence) {
		if (this.vM_dat == null || this.vD_dat == null) return;
		if (sentence.isEmpty()) return;

		TaggedWord tagged = sentence.get(0), next;
		for (int i = 0, max = sentence.size() - 1; i < max; i++) {
			next = sentence.get(i + 1);
			if (this.tag.equals(tagged.tag) && this.tag.equals(next.tag))
				if (this.vM_dat.contains(tagged.word)) tagged.tag = "vm";
				else if (this.vD_dat.contains(next.word)) next.tag = "vd";
			tagged = next;
		}
	}
}
