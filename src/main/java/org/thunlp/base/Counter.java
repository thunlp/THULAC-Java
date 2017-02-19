/**
 * Created锛�May 9, 2013 2:32:10 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�Counter.java  
 * description锛�  
 */
package org.thunlp.base;

import java.util.HashMap;

public class Counter<KeyType> extends HashMap<KeyType, Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void update(KeyType key){
		Integer value = get(key);
		if(value == null){
			value = 0;
		}
		put(key, value + 1);
	}
}
