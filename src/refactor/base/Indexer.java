/**
 * Created：May 9, 2013 2:08:05 PM  
 * Project：ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename：Indexer.java  
 * description：  
 */
package base;

import java.util.HashMap;
import java.util.Vector;

public class Indexer <E> {
	private HashMap<E, Integer> dict;
	private Vector<E> list;
	
	public Indexer() {
		super();
		dict.clear();
	}
	
	public int getIndex(E key){
		Integer value = dict.get(key);
		if(value == null){
			int id = dict.size();
			dict.put(key, value);
			list.add(key);
			return id;
		}else{
			return value;
		}
	}
	
	public E getObject(int ind){
		if(ind < 0 || ind >= dict.size()) return null;
		return list.get(ind);
	}
	
	public void setObject(int ind, E key){
		list.set(ind, key);
	}
}
