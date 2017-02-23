package org.thunlp.base;

import org.thunlp.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

public class Dat {
	public Vector<Entry> dat;
	public int datSize;

	protected Dat(int size) {
		if (size == 0) this.dat = new Vector<>();
		else this.dat = new Vector<>(size);
		this.datSize = size;
	}

	public Dat(String filename) throws IOException {
		this((int) (Files.size(Paths.get(filename)) >> 3));

		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		for (int i = 0; i < this.datSize; i++) {
			int base = this.readInt(in);
			int check = this.readInt(in);
			this.dat.add(new Entry(base, check));
		}
		in.close();
	}

	private int readInt(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		return (ch4 << 24) | (ch3 << 16) | (ch2 << 8) | ch1;
	}

	public int match(String word) {
		int ind = 0;
		int base = 0;
		int[] codePoints = StringUtil.toCodePoints(word);
		for (int c : codePoints) {
			ind = this.dat.get(ind).base + c;
			if (ind >= this.datSize || this.dat.get(ind).check != base) return -1;
			base = ind;
		}
		ind = this.dat.get(base).base;
		return ind < this.datSize && this.dat.get(ind).check == base ? ind : -1;
	}

	public int getInfo(String prefix) {
		int ind = 0;
		int base = 0;
		for (int i = 0; i < prefix.length(); i++) {
			ind = this.dat.get(ind).base + prefix.charAt(i);
			if (ind >= this.datSize || this.dat.get(ind).check != base) return i;
			base = ind;
		}
		return -base;
	}

	public int getDatSize() {
		return this.datSize;
	}

	public List<Entry> getDat() {
		return this.dat;
	}
}
