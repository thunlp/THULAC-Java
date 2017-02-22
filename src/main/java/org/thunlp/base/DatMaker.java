package org.thunlp.base;

import java.util.Comparator;
import java.util.Vector;

public class DatMaker extends Dat {
	public static Comparator<KeyValue> compareWords = new Comparator<KeyValue>() {
		@Override
		public int compare(KeyValue first, KeyValue second) {
			return first.key.compareTo(second.key);
		}
	};

	private int head;
	private int tail;

	public DatMaker() {
		super(1);
		this.dat.add(new Entry(1, -1));
		this.head = this.tail = 0;
	}

	/**
	 * Description: use [ind] as an entry
	 */
	public void use(int ind) {
		Entry entry = this.dat.get(ind);
		if (entry.check >= 0) System.out.println("cell reused!!");
		if (entry.base == 1) this.head = entry.check;
		else this.dat.get(-entry.base).check = entry.check;
		if (entry.check == -this.datSize) this.tail = entry.base;
		else this.dat.get(-entry.check).base = entry.base;
		entry.check = ind;
	}

	public void extend() {
		int oldSize = this.datSize;
		this.datSize *= 2;
		for (int i = 0; i < oldSize; i++)
			this.dat.add(new Entry(-(oldSize + i - 1), -(oldSize + i + 1)));
		this.dat.get(oldSize).base = this.tail;
		if (this.tail < 0) this.dat.get(-this.tail).check = -oldSize;
		this.tail = -(oldSize * 2 - 1);
	}

	public void shrink() {
		int last = this.datSize - 1;
		for (; this.dat.get(last).check < 0; --last) this.dat.remove(last);
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
			if (this.dat.get(base).check >= 0) flag = false;
			else {
				int i = 0;
				for (; i < size && this.dat.get(base + offsets.get(i)).check < 0; i++) ;
				if (i < size) flag = false;
			}

			if (flag) {
				this.use(base);
				for (int offset : offsets) this.use(base + offset);
				return base; //got it and return it
			}

			if (this.dat.get(base).check == -this.datSize) this.extend();

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
			if (word.length() < l) return;
			for (int i = 0; i < l; i++) if (word.charAt(i) != prefix.charAt(i)) return;
			if (word.length() > l)
				if (children.isEmpty() || word.charAt(l) != children.lastElement())
					children.add((int) word.charAt(l));
		}
	}

	public int assign(int check, Vector<Integer> offsets, boolean isWord) {
		int base = this.alloc(offsets);
		this.dat.set(base, new Entry(0, isWord ? check : base));
		for (int offset : offsets) this.dat.set(base + offset, new Entry(0, check));
		this.dat.get(check).base = base;
		return base;
	}

	public void makeDat(Vector<KeyValue> lexicon) {
		lexicon.sort(compareWords);
		int size = lexicon.size();
		String prefix = "";
		Vector<Integer> children = new Vector<>();
		this.genChildren(lexicon, 0, prefix, children);
		this.dat.get(0).base = this.assign(0, children, true);
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
			this.dat.get(this.dat.get(off).base).base = lexicon.get(i).value;
			if (i != 0 && i % 100000 == 0) System.out.println((float) i / size);
		}
	}
}
