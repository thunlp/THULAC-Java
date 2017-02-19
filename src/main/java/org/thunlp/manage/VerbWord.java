package org.thunlp.manage;

import org.thunlp.base.Dat;
import org.thunlp.base.TaggedWord;

import java.io.IOException;
import java.util.List;

public class VerbWord implements IAdjustPass {
	Dat vM_dat;
	Dat vD_dat;
	String tag_v;

	public VerbWord(String filename, String filename2) throws IOException {
		this.vM_dat = new Dat(filename);
		this.vD_dat = new Dat(filename2);
		this.tag_v = "v";
	}

	@Override
	public void adjust(List<TaggedWord> sentence) {
		if ((this.vM_dat == null) || (this.vD_dat == null)) return;
		for (int i = 0; i < sentence.size() - 1; i++) {
			if ((sentence.get(i).tag.equals(this.tag_v)) && (sentence.get(
					i + 1).tag.equals(
					this.tag_v))) {
				if (this.vM_dat.match(sentence.get(i).word) != -1) {
					sentence.get(i).tag = "vm";
				} else if (this.vD_dat.match(sentence.get(i + 1).word) != -1) {
					sentence.get(i + 1).tag = "vd";
				}
			}
		}
	}
}
