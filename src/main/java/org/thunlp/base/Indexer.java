package org.thunlp.base;

import java.util.HashMap;
import java.util.Vector;

public class Indexer<E> {
	private HashMap<E, Integer> dict;
	private Vector<E> list;

	public Indexer() {
		super();
		this.dict.clear();
	}

	public int getIndex(E key) {
		Integer value = this.dict.get(key);
		if (value == null) {
			int id = this.dict.size();
			this.dict.put(key, value);
			this.list.add(key);
			return id;
		} else {
			return value;
		}
	}

	public E getObject(int ind) {
		if (ind < 0 || ind >= this.dict.size()) return null;
		return this.list.get(ind);
	}

	public void setObject(int ind, E key) {
		this.list.set(ind, key);
	}
}
