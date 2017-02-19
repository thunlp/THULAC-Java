package org.thunlp.base;

public class WordWithTag {
	public String word;
	public String tag;
	public char separator;

	public WordWithTag(char separator) {
		this.separator = separator;
		this.word = "";
	}

	@Override
	public String toString() {
		return this.word + this.separator + this.tag;
	}
}
