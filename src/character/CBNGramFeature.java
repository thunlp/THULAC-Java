/**
 * Created锛�May 9, 2013 12:19:30 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�CBNGramFeature.java  
 * description锛�  
 */
package character;

import java.util.Vector;

import base.Counter;
import base.Dat;
import base.Entry;
import base.Indexer;


public class CBNGramFeature {

	private static int SENTENCE_BOUNDARY='#';
	private int SEPERATOR;
	private int maxLength;
	private int[] uniBases;
	private int[] biBases;
	private int[] values;
	private int datSize;
	private Vector<Entry> dat;
	private CBModel model;
	
	public CBNGramFeature(){
		SEPERATOR = ' ';
		uniBases = null;
		biBases = null;
	}
	
	public CBNGramFeature(Dat myDat, CBModel model, int[] values){
		SEPERATOR = ' ';
		datSize = myDat.getDatSize();
		dat = myDat.getDat();
		this.model = model;
		maxLength = 10000;
		uniBases = new int[maxLength + 2];
		biBases = new int[maxLength + 4];
		this.values = values;
	}
	
	public void featureGeneration(String seq, Indexer<String> indexer, Counter<String> bigramCounter){
		int mid = 0;
		int left = 0;
		int left2 = 0;
		int right = 0;
		int right2 = 0;
		String key;
		String bigram;
		for(int i = 0; i < seq.length(); i ++){
			mid = seq.charAt(i);
			left = (i > 0) ? (seq.charAt(i-1)) : (SENTENCE_BOUNDARY);
			left2 = (i >= 2) ? (seq.charAt(i - 2)) : (SENTENCE_BOUNDARY);
			right = (i < seq.length() - 1) ? seq.charAt(i + 1) : (SENTENCE_BOUNDARY);
			right2 = (i < seq.length() - 2) ? seq.charAt(i + 2) : (SENTENCE_BOUNDARY);
			
			if(bigramCounter != null){
				if(i == 0){
					bigram = ((char)left2 ) + "" + ((char)left);
					bigramCounter.update(bigram);
					bigram = ((char)left ) + "" + ((char)mid);
					bigramCounter.update(bigram);
					bigram = ((char)mid) + "" + ((char)right);
					bigramCounter.update(bigram);
				}else{
					bigram = ((char)right) + "" + ((char)right2);
					bigramCounter.update(bigram);
				}
			}
			
			key = ((char)mid)+((char)SEPERATOR) + "1";
			indexer.getIndex(key);
			
			key = ((char)left)+((char)SEPERATOR) + "2";
			indexer.getIndex(key);
			
			key = ((char)right)+((char)SEPERATOR) + "3";
			indexer.getIndex(key);
			
			key = ((char)left)+((char)mid)+((char)SEPERATOR) + "1";
			indexer.getIndex(key);
			key = ((char)mid)+((char)right)+((char)SEPERATOR) + "2";
			indexer.getIndex(key);
			key = ((char)left2)+((char)left)+((char)SEPERATOR) + "1";
			indexer.getIndex(key);
			key = ((char)right)+((char)right2)+((char)SEPERATOR) + "1";
			indexer.getIndex(key);
		}
	}
	
	private void addValues(int valueOffset, int base, int del, int[] pAllowedLable){
		int ind = dat.get(base).base + del;
		if(ind >= datSize || dat.get(ind).check != base){
			return;
		}
		int offset = dat.get(ind).base;
		int weightOffset = offset * model.l_size;
		int allowedLabel;
		if(model.l_size == 4){
			values[valueOffset] += model.fl_weights[weightOffset];
			values[valueOffset + 1] += model.fl_weights[weightOffset + 1];
			values[valueOffset + 2] += model.fl_weights[weightOffset + 2];
			values[valueOffset + 3] += model.fl_weights[weightOffset + 3];
		}else{
			if(pAllowedLable != null){
				for(int i = 0; i < pAllowedLable.length; i ++){
					allowedLabel = pAllowedLable[i];
					values[valueOffset + allowedLabel] += model.fl_weights[weightOffset + allowedLabel];
				}
			}else{
				for(int i = 0; i < model.l_size; i ++){
					values[valueOffset + i] += model.fl_weights[weightOffset + i];
				}
			}
		}
	}
	
	private Vector<Integer> findBases(int datSize, int ch1, int ch2){
		Vector<Integer> result = new Vector<Integer>();
		int uniBase;
		int biBase;
		if(ch1 > 32 && ch1 < 128) ch1+=65248;
		if(ch2 > 32 && ch2 < 128) ch2+=65248;
		if(ch1 >= datSize || dat.get(ch1).check != 0){
			uniBase = -1;
			biBase = -1;
			result.clear();
			result.add(uniBase);
			result.add(biBase);
			return result;
		}
		uniBase = dat.get(ch1).base + SEPERATOR;
		//System.out.print(ch1);
		//System.out.println(ch2);
		int ind = dat.get(ch1).base + ch2;
		if(ind >= datSize || dat.get(ind).check != ch1){
			biBase = -1;
			result.clear();
			result.add(uniBase);
			result.add(biBase);
			return result;
		}
		biBase = dat.get(ind).base + SEPERATOR;
		result.clear();
		result.add(uniBase);
		result.add(biBase);
		return result;
	}
	
	private void updateWeight(int valueOffset, int base, int del, int label, int delta, long steps){
		int ind = dat.get(base).base + del;
		if(ind >= datSize || dat.get(ind).check != base) return;
		int offset = dat.get(ind).base;
		model.update_fl_weights(offset, label, delta, steps);
	}
	
	public int putValues(String sequence, int len){
		if(len >= maxLength){
			System.out.println("larger than max");
			return 1;
		}
		Vector<Integer> result = findBases(datSize, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
		uniBases[0] = result.get(0);
		biBases[0] = result.get(1);
		
		result = findBases(datSize, SENTENCE_BOUNDARY,sequence.charAt(0));
		uniBases[0] = result.get(0);
		biBases[1] = result.get(1);
		//System.out.println((int)sequence.charAt(0));
		for(int i = 0 ; i + 1 < len; i ++){
			result = findBases(datSize, sequence.charAt(i), sequence.charAt(i+1));
			uniBases[i+1] = result.get(0);
			biBases[i+2] = result.get(1);
		}
		
		result = findBases(datSize, (int)sequence.charAt(len - 1), SENTENCE_BOUNDARY);
		uniBases[len] = result.get(0);
		biBases[len+1] = result.get(1);
		
		result = findBases(datSize, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
		uniBases[len+1] = result.get(0);
		biBases[len+2] = result.get(1);
		
		int base = 0;
		for(int i = 0; i < len; i ++){
			int valueOffset = i * model.l_size;
			if((base = uniBases[i + 1]) != -1){
				addValues(valueOffset, base, 49, null);
			}
			if((base = uniBases[i]) != -1){
				addValues(valueOffset, base, 50, null);
			}
			if((base = uniBases[i + 2]) != -1){
				addValues(valueOffset, base, 51, null);
			}
			if((base = biBases[i + 1]) != -1){
				addValues(valueOffset, base, 49, null);
			}
			if((base = biBases[i + 2]) != -1){
				addValues(valueOffset, base, 50, null);
			}
			if((base = biBases[i]) != -1){
				addValues(valueOffset, base, 51, null);
			}
			if((base = biBases[i + 3]) != -1){
				addValues(valueOffset, base, 52, null);
			}
		}
		//System.out.println(values[0]);
		return 0;
	}
	
	public int updateWeights(String seq, int len, int[] results, int delta, long steps){
		Vector<Integer> result = findBases(datSize, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
		uniBases[0] = result.get(0);
		biBases[0] = result.get(1);
		
		result = findBases(datSize, SENTENCE_BOUNDARY, seq.charAt(0));
		uniBases[0] = result.get(0);
		biBases[1] = result.get(1);
		
		for(int i = 0 ; i + 1 < len; i ++){
			result = findBases(datSize, seq.charAt(i), seq.charAt(i+1));
			uniBases[i+1] = result.get(0);
			biBases[i+2] = result.get(1);
		}
		
		result = findBases(datSize, seq.charAt(len - 1), SENTENCE_BOUNDARY);
		uniBases[len] = result.get(0);
		biBases[len+1] = result.get(1);
		
		result = findBases(datSize, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
		uniBases[len+1] = result.get(0);
		biBases[len+2] = result.get(1);
		
		int base = 0;
		for(int i = 0; i < len; i ++){
			int valueOffset = i * model.l_size;
			if((base = uniBases[i + 1]) != -1){
				updateWeight(valueOffset, base, 49, results[i], delta, steps);
			}
			if((base = uniBases[i]) != -1){
				updateWeight(valueOffset, base, 50, results[i], delta, steps);
			}
			if((base = uniBases[i + 2]) != -1){
				updateWeight(valueOffset, base, 51, results[i], delta, steps);
			}
			if((base = biBases[i + 1]) != -1){
				updateWeight(valueOffset, base, 49, results[i], delta, steps);
			}
			if((base = biBases[i + 2]) != -1){
				updateWeight(valueOffset, base, 50, results[i], delta, steps);
			}
			if((base = biBases[i]) != -1){
				updateWeight(valueOffset, base, 51, results[i], delta, steps);
			}
			if((base = biBases[i + 3]) != -1){
				updateWeight(valueOffset, base, 52, results[i], delta, steps);
			}
		}
		return 0;
	}
	

}
