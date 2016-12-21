/**
 * Created：May 8, 2013 4:43:30 PM  
 * Project：ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename：KeyValue.java  
 * description：  
 */
package base;

public class KeyValue {
	public String key;
	public int value;
	
	public KeyValue(){
		key = "";
		value = 0;
	}

	public KeyValue(String key, int value) {
		super();
		this.key = key;
		this.value = value;
	}
	
}
