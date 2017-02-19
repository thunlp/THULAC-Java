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
//		byte[] buff=new byte[]{};
//		buff=word.getBytes();
//		try {
//			out.write(buff,0,buff.length);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			out.write(separator);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		buff=tag.getBytes();
//		try {
//			out.write(buff,0,buff.length);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		out.write(' ');
	}

	public void print() throws IOException {
		System.out.print(this.word);
		System.out.print(this.separator);
		System.out.print(this.tag);
		System.out.print(' ');
	}
}
