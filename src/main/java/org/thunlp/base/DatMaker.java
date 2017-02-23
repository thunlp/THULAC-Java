package org.thunlp.base;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Vector;

public class DatMaker extends Dat {
	public static Comparator<KeyValue> KEY_VALUE_COMPARATOR = new Comparator<KeyValue>() {
		@Override
		public int compare(KeyValue first, KeyValue second) {
			return first.key.compareTo(second.key);
		}
	};

	public static DatMaker readFromTxtFile(String filename) throws IOException {
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename)));
		Vector<KeyValue> lexicon = new Vector<>();
		String str;
		int id = 0;
		while ((str = buf.readLine()) != null) {
			if (str.length() == 0) continue;
			if (str.endsWith("\r")) str = str.substring(0, str.length() - 1);
			lexicon.add(new KeyValue(str, id++));
		}
		lexicon.add(new KeyValue());

		DatMaker dm = new DatMaker();
		dm.makeDat(lexicon);
		dm.shrink();
		return dm;
	}

	private int head;
	private int tail;

	public DatMaker() {
		super(1);
		this.dat[0] = 1;
		this.dat[1] = -1;
		this.head = this.tail = 0;
	}

	/**
	 * Use {@code dat[ind]} as an entry.
	 */
	public void use(int ind) {
		int base = this.dat[ind << 1], check = this.dat[(ind << 1) + 1];
		if (check >= 0) System.out.println("cell reused!!");
		if (base == 1) this.head = check;
		else this.dat[((-base) << 1) + 1] = check;
		if (check == -this.datSize) this.tail = base;
		else this.dat[(-check) << 1] = base;
		this.dat[(ind << 1) + 1] = ind;
	}

	public void extend() {
		int oldSize = this.datSize;
		this.datSize *= 2;
		int[] newDat = new int[this.dat.length << 1];
		System.arraycopy(this.dat, 0, newDat, 0, this.dat.length);
		this.dat = newDat;
		for (int i = 0; i < oldSize; i++) {
			int pos = (oldSize + i) << 2;
			newDat[pos] = (-oldSize + i - 1);
			newDat[pos + 1] = (-oldSize + i + 1);
		}
		this.dat[oldSize << 1] = this.tail;
		if (this.tail < 0) this.dat[((-this.tail) << 1) + 1] = -oldSize;
		this.tail = -(oldSize * 2 - 1);
	}

	/**
	 * Remove trailing entries in {@code dat} which satisfies {@code entry.check < 0}.
	 */
	public void shrink() {
		int last = this.datSize - 1;
		for (; this.dat[(last << 1) + 1] < 0; --last) ;
		this.datSize = last + 1;
	}

	public int alloc(Vector<Integer> offsets) {
		int size = offsets.size();
		int base = -this.head;

		while (true) {
			if (base == this.datSize) this.extend();

			if (size != 0) {
				while ((base + offsets.get(size - 1)) >= this.datSize) {
					this.extend();
				}
			}

			boolean flag = true;
			if (this.dat[(base << 1) + 1] >= 0) flag = false;
			else {
				int i = 0;
				for (; i < size && this.dat[(base + offsets.get(i) << 1) + 1] < 0; i++) ;
				if (i < size) flag = false;
			}

			if (flag) {
				this.use(base);
				for (int offset : offsets) this.use(base + offset);
				return base; //got it and return it
			}

			if (this.dat[(base << 1) + 1] == -this.datSize) this.extend();

			base = -this.dat[(base << 1) + 1];
		}
	}

	/**
	 * Finds all the characters that each one, say, {@code c}, satisfies:<br>
	 * <ul>
	 * <li>Let {@code sub} be the sublist of {@code lexicon} with start index {@code
	 * start}.</li> <li>Let {@code pre} be {@code prefix + c}. (here + represents the
	 * string append).</li> <li>Assert that at least one string in {@code sub} starts
	 * with {@code pre}.</li>
	 * </ul>
	 * It is assumed that {@code lexicon} is already sorted using {@link
	 * #KEY_VALUE_COMPARATOR}.<br> The result is stored in {@code children}, as <i>Unicode
	 * Code Points</i>, and the previous content of {@code children} would be erased.
	 */
	public void genChildren(
			Vector<KeyValue> lexicon, int start, String prefix,
			Vector<Integer> children) {
		children.clear();
		int l = prefix.length();
		for (int ind = start; ind < lexicon.size(); ind++) {
			String word = lexicon.get(ind).key;
			if (word.length() < l) return;
			for (int i = 0; i < l; i++) if (word.charAt(i) != prefix.charAt(i)) return;
			if (word.length() > l)
				if (children.isEmpty() || word.charAt(l) != children.lastElement())
					children.add((int) word.charAt(l));
		}
	}

	public int assign(int check, Vector<Integer> offsets, boolean isWord) {
		int base = this.alloc(offsets);
		this.dat[base << 1] = 0;
		this.dat[(base << 1) + 1] = isWord ? check : base;
		for (int offset : offsets) {
			int pos = (base + offset) << 1;
			this.dat[pos] = 0;
			this.dat[pos + 1] = check;
		}
		this.dat[check << 1] = base;
		return base;
	}

	public void makeDat(Vector<KeyValue> lexicon) {
		lexicon.sort(KEY_VALUE_COMPARATOR);
		int size = lexicon.size();
		String prefix = "";
		Vector<Integer> children = new Vector<>();
		this.genChildren(lexicon, 0, prefix, children);
		this.dat[0] = this.assign(0, children, true);
		for (int i = 0; i < size; i++) {
			String word = lexicon.get(i).key;
			int off = this.getInfo(word);
			if (off <= 0) off = word.length();
			for (int offset = off; offset <= word.length(); offset++) {
				prefix = word.substring(0, offset);
				int pBase = -this.getInfo(prefix);
				this.genChildren(lexicon, i, prefix, children);
				this.assign(pBase, children, (offset == word.length()));
			}
			off = -this.getInfo(word);
			this.dat[this.dat[off << 1] << 1] = lexicon.get(i).value;
			if (i != 0 && i % 100000 == 0) System.out.println((float) i / size);
		}
	}
}
