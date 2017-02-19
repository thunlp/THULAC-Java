package org.thunlp.manage;

import org.thunlp.base.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Postprocesser {
	private Dat p_dat;
	private String tag;

	public Postprocesser(String filename, String tag, boolean isTxt) throws IOException {
		this.tag = tag;
		if (isTxt) {
			BufferedReader buf = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename)));
			Vector<KeyValue> lexicon = new Vector<>();
			lexicon.add(new KeyValue());
			String str;
			int id = 0;
			while ((str = buf.readLine()) != null) {
				if (str.length() == 0) continue;
				if (str.endsWith("\r")) str = str.substring(0, str.length() - 1);
				lexicon.lastElement().key = str;
				lexicon.lastElement().value = id;

				// add new element
				lexicon.add(new KeyValue());
				id += 1;
			}
			DatMaker dm = new DatMaker();
			dm.makeDat(lexicon);
			dm.shrink();

			this.p_dat = new Dat(dm.datSize, dm.dat);
		} else this.p_dat = new Dat(filename);
	}

	public void adjust(TaggedSentence sentence) {
		if (this.p_dat == null) return;

		Vector<String> tmp = new Vector<>();
		for (int i = 0; i < sentence.size(); i++) {
			WordWithTag tagged = sentence.get(i);
			StringBuilder sb = new StringBuilder(tagged.word);
			if (this.p_dat.getInfo(sb.toString()) >= 0) continue;

			tmp.clear();
			for (int j = i + 1; j < sentence.size(); j++) {
				sb.append(sentence.get(j).word);
				if (this.p_dat.getInfo(sb.toString()) >= 0) break;
				tmp.add(sb.toString());
			}

			int k = tmp.size() - 1;
			for (; k >= 0 && this.p_dat.match(tmp.get(k)) == -1; k--) ;
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
