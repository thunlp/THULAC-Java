package org.thunlp.base;

import java.io.FileOutputStream;
import java.io.IOException;

public class WordWithTag {
	public String word;
    public String tag;
	public char separator;
	public WordWithTag(char separator){
		this.separator = separator;
		word = new String("");
	}
	public void print(FileOutputStream out) throws IOException
	{
		byte[] buff=new byte[]{};
		buff=word.getBytes();
		try {
			out.write(buff,0,buff.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.write(separator);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buff=tag.getBytes();
		try {
			out.write(buff,0,buff.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.write(' ');
	}
	public void print() throws IOException
	{
		System.out.print(word);
		System.out.print(separator);
		System.out.print(tag);
		System.out.print(' ');
	}
}
