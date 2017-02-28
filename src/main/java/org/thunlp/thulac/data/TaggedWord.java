package org.thunlp.thulac.data;

public class TaggedWord {
	public String word;
	public String tag;
	public char separator;

	public TaggedWord(char separator) {
		this.separator = separator;
		this.word = "";
	}

	@Override
	public String toString() {
		return this.word + this.separator + this.tag;
	}
}
