package base;

import java.io.FileOutputStream;
import java.io.IOException;

public class WordWithTag {
	public String word = "";
	public String tag;
	public char separator = '_';

	public WordWithTag(){
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void print(FileOutputStream out) throws IOException
	{
		byte[] buff=new byte[]{};
		buff=word.getBytes("utf-8");
		try {
			out.write(buff,0,buff.length);
			out.write(separator);
			buff=tag.getBytes("utf-8");
			out.write(buff,0,buff.length);
			out.write(' ');
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public String toString() {
		return "WordWithTag{" + "word=" + word + ", tag=" + tag + '}';
	}
}
