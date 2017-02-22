package org.thunlp.base;

import org.thunlp.util.StringUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Dat {
	private static class DataInputStream extends FilterInputStream {
		public DataInputStream(InputStream in) {
			super(in);
		}

		@Override
		public final int read(byte b[]) throws IOException {
			return this.in.read(b, 0, b.length);
		}

		@Override
		public final int read(byte b[], int off, int len) throws IOException {
			return this.in.read(b, off, len);
		}

		public final int readInt() throws IOException {
			int ch1 = this.in.read();
			int ch2 = this.in.read();
			int ch3 = this.in.read();
			int ch4 = this.in.read();
			return (ch4 << 24) | (ch3 << 16) | (ch2 << 8) | ch1;
		}
	}

	public List<Entry> dat;
	public int datSize;

	protected Dat(int size) {
		if (size == 0) this.dat = new ArrayList<>();
		else this.dat = new ArrayList<>(size);
		this.datSize = size;
	}

	public Dat(Dat old) {
		this(old.datSize);
		for (Entry entry : old.dat) this.dat.add(new Entry(entry.base, entry.check));
	}

	public Dat(String filename) throws IOException {
		this((int) (Files.size(Paths.get(filename)) >> 3));

		DataInputStream in = new DataInputStream(
				new BufferedInputStream(new FileInputStream(filename)));
		for (int i = 0; i < this.datSize; i++) {
			int base = in.readInt();
			int check = in.readInt();
			this.dat.add(new Entry(base, check));
		}
		in.close();
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
