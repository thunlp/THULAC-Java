/**
 * Created：May 5, 2013 3:45:34 PM  
 * Project：ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename：DatMaker.java  
 * description：  
 */
package base;

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
			
			int minSize = (firstKey.length() < secondKey.length()) ? firstKey.length() : secondKey.length();
			for(int i = 0; i < minSize; i ++){
				if(firstKey.charAt(i) > secondKey.charAt(i)) return 1;
				if(firstKey.charAt(i) < secondKey.charAt(i)) return -1;
			}
			if(firstKey.length() < secondKey.length()){
				return -1;
			}else if(firstKey.length() > secondKey.length()){
				return 1;
			}else{
				return 0;
			}
		}
	};
	
	private int head;
	private int tail;
	
	public DatMaker() {
		datSize = 1;
		dat = new Vector<Entry>();
		Entry entry = new Entry();
		entry.base = 1;
		entry.check = -1;
		dat.add(entry);
		head = 0;
		tail = 0;
	}
	
	
	/**
	 * <p>Title:use</p>
	 * <p>Description: use [ind] as an entry<p>
	 * @param ind
	 */
	public void use(int ind){
		if(dat.get(ind).check >= 0) System.out.println("cell reused!!");
		if(dat.get(ind).base == 1){
			head = dat.get(ind).check;
		}else{
			dat.get(-dat.get(ind).base).check = dat.get(ind).check;
		}
		if(dat.get(ind).check == -datSize){
			tail = dat.get(ind).base;
		}else{
			dat.get(-dat.get(ind).check).base = dat.get(ind).base;
		}
		dat.get(ind).check = ind;
	}
	
	public void extend(){
		int oldSize = datSize;
		datSize *= 2;
		for(int i = 0; i < oldSize; i ++){
			Entry entry = new Entry();
			entry.base = - (oldSize + i - 1);
			entry.check = - (oldSize + i + 1);
			dat.add(entry);
		}
		dat.get(oldSize).base = tail;
		if(-tail > 0) dat.get(-tail).check = - oldSize;
		tail = - (oldSize * 2 - 1);
	}
	
	public void shrink(){
		int last = datSize - 1;
		while(dat.get(last).check < 0){
			dat.remove(last);
			last --;
		}
		datSize = last + 1;
	}
	
	public int alloc(Vector<Integer> offsets){
		int size = offsets.size();
		int base = - head;
		while(true){
			if(base == datSize) extend();
			if(size != 0){
				while((base + offsets.get(size - 1)) >= datSize){
					extend();
				}
			}
			boolean flag = true;
			if(dat.get(base).check >= 0){
				flag = false;
			}else{
				for(int i = 0 ; i < size; i ++){
					if(dat.get(base + offsets.get(i)).check >= 0){// used
						flag = false;
						break;
					}
				}
			}
			if(flag){
				use(base);
				for(int i = 0; i < size; i ++){
					use(base + offsets.get(i));
				}
				return base;//got it and return it
			}
			if(dat.get(base).check == -datSize){
				extend();
			}
			base = -dat.get(base).check;
		}
	}
	
	public void genChildren(Vector<KeyValue> lexicon, int start, String prefix, Vector<Integer> children){
		children.clear();
		int l = prefix.length();
		for(int ind = start; ind < lexicon.size(); ind ++){
			String word = lexicon.get(ind).key;
			if(word.length() < l){
				return;
			}
			for(int i = 0; i < l; i ++){
				if(word.charAt(i) != prefix.charAt(i)){
					return;
				}
			}
			if(word.length() > l){
				if(children.isEmpty() || (((int)word.charAt(l)) != children.lastElement())){
					children.add((int)word.charAt(l));
				}
			}
		}
	}
	
	public int assign(int check, Vector<Integer> offsets, boolean isWord){
		int base = alloc(offsets);
		dat.get(base).base = 0;
		if(isWord){
			dat.get(base).check = check;
		}else{
			dat.get(base).check = base;
		}
		
		for(int i = 0; i < offsets.size(); i ++){
			dat.get(base + offsets.get(i)).base = 0;
			dat.get(base + offsets.get(i)).check = check;
		}
		dat.get(check).base = base;
		
		return base;
	}
	
	public void makeDat(Vector<KeyValue> lexicon){
		Collections.sort(lexicon, compareWords);
		int size = lexicon.size();
		String prefix = "";
		Vector<Integer> children = new Vector<Integer>();
		genChildren(lexicon, 0, prefix, children);
		int base = assign(0, children, true);
		dat.get(0).base = base;
		for(int i = 0; i < size; i ++){
			String word = lexicon.get(i).key;
			int off = getInfo(word);
			if(off <= 0){
				off = word.length();
			}
			for(int offset = off; offset <= word.length(); offset ++){
				prefix = word.substring(0, offset);
				int pBase = - getInfo(prefix);
				genChildren(lexicon, i, prefix, children);
				base = assign(pBase, children, (offset == word.length()));
			}
			off = -getInfo(word);
			dat.get(dat.get(off).base).base = lexicon.get(i).value;
			if((i != 0) && (i % 100000 == 0)){
				System.out.println(((double)i/(double)size));
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		DatMaker dm = new DatMaker();
		Vector<KeyValue> lexicon = new Vector<KeyValue>();
		
		String filename = "res/pun.txt";
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"UTF8"));
		String line = "";
		int id = 0;
		while((line = in.readLine()) != null){
			if(line.equals("")){
				continue;
			}
			
			lexicon.add(new KeyValue(line.trim(), id));
			id ++;
		}
		in.close();
		System.out.println(lexicon.size()+" words are loaded.");
		dm.makeDat(lexicon);
		dm.shrink();
		System.out.println("size of DAT "+dm.getDatSize());
		dm.save("res/javaPun.dat");
	}
}
