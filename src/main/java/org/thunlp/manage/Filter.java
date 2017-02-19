package org.thunlp.manage;

import java.io.IOException;
import java.util.HashSet;

import org.thunlp.base.Dat;
import org.thunlp.base.SegmentedSentence;
import org.thunlp.base.TaggedSentence;

public class Filter {
	    Dat xu_dat;
		Dat time_dat;
		HashSet<String> posSet;
		HashSet<Integer> arabicNumSet;
		HashSet<Integer> chineseNumSet;
	    public Filter(String xuWordFile, String timeWordFile) throws IOException{
			xu_dat = new Dat(xuWordFile);
			time_dat = new Dat(timeWordFile);
			posSet = new HashSet<String>();
			arabicNumSet =new HashSet<Integer>();
			chineseNumSet =new HashSet<Integer>();
			String POS_RESERVES[] = {"n","np","ns","ni","nz","v","a","id","t","uw"};
			for(int i = 0; i < 10; i ++){
				posSet.add(POS_RESERVES[i]);
			}
			for(int i = 48; i < 58; i ++){
				arabicNumSet.add(i);
			}
			for(int i = 65296; i < 65306; i ++){
				arabicNumSet.add(i);
			}
			int chineseNums[] = {12295,19968,20108,19977,22235,20116,20845,19971,20843,20061}; 
			for(int i = 0; i < 10; i ++){
				chineseNumSet.add(chineseNums[i]);
			}
	    };
	    
	    public void adjust(SegmentedSentence sentence){
	        if(xu_dat == null || time_dat == null)return;
			int size = sentence.size();
			String word;
			int count = 0;
			boolean checkArabic = false;
			boolean checkChinese = false;

			for(int i = size - 1; i >= 0; i --){
				word = sentence.get(i);
				//if((word.size() < 2) || (xu_dat->match(word) != -1)){
				if(xu_dat.match(word) != -1){
					sentence.remove(i);
					continue;
				}
				count = 0;
				checkArabic = false;
				checkChinese = false;
				
				for(int j = 0; j < word.length(); j ++){
					if(arabicNumSet.contains(word.charAt(j))){
						checkArabic = true;
						break;
					} 
					if(chineseNumSet.contains(word.charAt(j))){
						count++;
						if(count == 2){
							checkChinese = true;
							break;
						}
					}
				}
				if(checkArabic || checkChinese || (time_dat.match(word) != -1)){
					sentence.remove(i);
					continue;
				}
			}

			word = "";
	    };

	    public void adjust(TaggedSentence sentence){
			if(xu_dat==null || time_dat==null)return;
			int size = sentence.size();
			String word;
			String tag;
			int count = 0;
			boolean checkArabic = false;
			boolean checkChinese = false;

			for(int i = size - 1; i >= 0; i --){
				word = sentence.get(i).word;
				/*
				if(word.size() < 2){
					sentence.erase(sentence.begin() + i);
					continue;
				}
				*/
				tag = sentence.get(i).tag;
				if(posSet.contains(tag)){
					if(xu_dat.match(word) != -1){				
						sentence.remove(i);
						continue;
					}
					if(tag == "t"){	
						count = 0;
						checkArabic = false;
						checkChinese = false;
				
						for(int j = 0; j < word.length(); j ++){
							if(arabicNumSet.contains(word.charAt(j))){
								checkArabic = true;
								break;
							}
							chineseNumSet.contains(word.charAt(j));
							if(chineseNumSet.contains(word.charAt(j))){
								count++;
								if(count == 2){
									checkChinese = true;
									break;
								}
							}
						}
						if(checkArabic || checkChinese || (time_dat.match(word) != -1)){
							sentence.remove(i);
							continue;
						}
					}
				}else{
					sentence.remove(i);
					continue;
				}

			}

			word="";
			tag="";
	    };

}
