package org.thunlp.base;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class DatMaker extends Dat {

	public static Comparator<KeyValue> compareWords = new Comparator<KeyValue>() {
		@Override
		public int compare(KeyValue first, KeyValue second) {
			// TODO Auto-generated method stub
			String firstKey = first.key;
			String secondKey = second.key;

			int minSize = (firstKey.length() < secondKey.length()) ? firstKey.length() : secondKey
					.length();
			for (int i = 0; i < minSize; i++) {
				if (firstKey.charAt(i) > secondKey.charAt(i)) return 1;
				if (firstKey.charAt(i) < secondKey.charAt(i)) return -1;
			}
			if (firstKey.length() < secondKey.length()) {
				return -1;
			} else if (firstKey.length() > secondKey.length()) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private int head;
	private int tail;

	public DatMaker() {
		this.datSize = 1;
		this.dat = new Vector<>();
		Entry entry = new Entry();
		entry.base = 1;
		entry.check = -1;
		this.dat.add(entry);
		this.head = 0;
		this.tail = 0;
	}


	/**
	 * <p>Title:use</p>
	 * <p>Description: use [ind] as an entry<p>
	 *
	 * @param ind
	 */
	public void use(int ind) {
		if (this.dat.get(ind).check >= 0) System.out.println("cell reused!!");
		if (this.dat.get(ind).base == 1) {
			this.head = this.dat.get(ind).check;
		} else {
			this.dat.get(-this.dat.get(ind).base).check = this.dat.get(ind).check;
		}
		if (this.dat.get(ind).check == -this.datSize) {
			this.tail = this.dat.get(ind).base;
		} else {
			this.dat.get(-this.dat.get(ind).check).base = this.dat.get(ind).base;
		}
		this.dat.get(ind).check = ind;
	}

	public void extend() {
		int oldSize = this.datSize;
		this.datSize *= 2;
		for (int i = 0; i < oldSize; i++) {
			Entry entry = new Entry();
			entry.base = -(oldSize + i - 1);
			entry.check = -(oldSize + i + 1);
			this.dat.add(entry);
		}
		this.dat.get(oldSize).base = this.tail;
		if (-this.tail > 0) this.dat.get(-this.tail).check = -oldSize;
		this.tail = -(oldSize * 2 - 1);
	}

	public void shrink() {
		int last = this.datSize - 1;
		while (this.dat.get(last).check < 0) {
			this.dat.remove(last);
			last--;
		}
		this.datSize = last + 1;
	}

	public int alloc(Vector<Integer> offsets) {
		int size = offsets.size();
		int base = -this.head;
		while (true) {
			if (base == this.datSize) extend();
			if (size != 0) {
				while ((base + offsets.get(size - 1)) >= this.datSize) {
					extend();
				}
			}
			boolean flag = true;
			if (this.dat.get(base).check >= 0) {
				flag = false;
			} else {
				for (int i = 0; i < size; i++) {
					if (this.dat.get(base + offsets.get(i)).check >= 0) {// used
						flag = false;
						break;
					}
				}
			}
			if (flag) {
				use(base);
				for (int i = 0; i < size; i++) {
					use(base + offsets.get(i));
				}
				return base;//got it and return it
			}
			if (this.dat.get(base).check == -this.datSize) {
				extend();
			}
			base = -this.dat.get(base).check;
		}
	}

	public void genChildren(
			Vector<KeyValue> lexicon, int start, String prefix,
			Vector<Integer> children) {
		children.clear();
		int l = prefix.length();
		for (int ind = start; ind < lexicon.size(); ind++) {
			String word = lexicon.get(ind).key;
			if (word.length() < l) {
				return;
			}
			for (int i = 0; i < l; i++) {
				if (word.charAt(i) != prefix.charAt(i)) {
					return;
				}
			}
			if (word.length() > l) {
				if (children.isEmpty() || (((int) word.charAt(
						l)) != children.lastElement())) {
					children.add((int) word.charAt(l));
				}
			}
		}
	}

	public int assign(int check, Vector<Integer> offsets, boolean isWord) {
		int base = alloc(offsets);
		this.dat.get(base).base = 0;
		if (isWord) {
			this.dat.get(base).check = check;
		} else {
			this.dat.get(base).check = base;
		}

		for (int i = 0; i < offsets.size(); i++) {
			this.dat.get(base + offsets.get(i)).base = 0;
			this.dat.get(base + offsets.get(i)).check = check;
		}
		this.dat.get(check).base = base;

		return base;
	}

	public void makeDat(Vector<KeyValue> lexicon) {
		Collections.sort(lexicon, compareWords);
		int size = lexicon.size();
		String prefix = "";
		Vector<Integer> children = new Vector<>();
		genChildren(lexicon, 0, prefix, children);
		int base = assign(0, children, true);
		this.dat.get(0).base = base;
		for (int i = 0; i < size; i++) {
			String word = lexicon.get(i).key;
			int off = getInfo(word);
			if (off <= 0) {
				off = word.length();
			}
			for (int offset = off; offset <= word.length(); offset++) {
				prefix = word.substring(0, offset);
				int pBase = -getInfo(prefix);
				genChildren(lexicon, i, prefix, children);
				base = assign(pBase, children, (offset == word.length()));
			}
			off = -getInfo(word);
			this.dat.get(this.dat.get(off).base).base = lexicon.get(i).value;
			if ((i != 0) && (i % 100000 == 0)) {
				System.out.println(((double) i / (double) size));
			}
		}
	}

	public static void main(String[] args) throws IOException {
		DatMaker dm = new DatMaker();
		Vector<KeyValue> lexicon = new Vector<>();

		String filename = "res/pun.txt";
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), "UTF8"));
		String line = "";
		int id = 0;
		while ((line = in.readLine()) != null) {
			if (line.equals("")) {
				continue;
			}

			lexicon.add(new KeyValue(line.trim(), id));
			id++;
		}
		in.close();
		System.out.println(lexicon.size() + " words are loaded.");
		dm.makeDat(lexicon);
		dm.shrink();
		System.out.println("size of DAT " + dm.getDatSize());
		dm.save("res/javaPun.dat");
	}
}
