package org.thunlp.adjustment;

import org.thunlp.base.Dat2;
import org.thunlp.base.TaggedWord;

import java.io.IOException;
import java.util.List;

public class VerbPass implements IAdjustPass {
	private Dat2 vM_dat;
	private Dat2 vD_dat;
	private String tag;

	public VerbPass(String filename, String filename2) throws IOException {
		this.vM_dat = new Dat2(filename);
		this.vD_dat = new Dat2(filename2);
		this.tag = "v";
	}

	@Override
	public void adjust(List<TaggedWord> sentence) {
		if ((this.vM_dat == null) || (this.vD_dat == null)) return;

		TaggedWord tagged = sentence.get(0), next;
		for (int i = 0, max = sentence.size() - 1; i < max; i++) {
			next = sentence.get(i + 1);
			if (this.tag.equals(tagged.tag) && this.tag.equals(next.tag))
				if (this.vM_dat.match(tagged.word) != -1) tagged.tag = "vm";
				else if (this.vD_dat.match(next.word) != -1) next.tag = "vd";
			tagged = next;
		}
	}
}
