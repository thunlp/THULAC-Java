package org.thunlp.base;

import java.io.IOException;
import java.io.PrintStream;

public class WordWithTag {
	public String word;
	public String tag;
	public char separator;

	public WordWithTag(char separator) {
		this.separator = separator;
		this.word = new String("");
	}

	public void print(PrintStream out) throws IOException {
		out.print(this.word);
		out.print(this.separator);
		out.print(this.tag);
		out.print(' ');
	}
}
