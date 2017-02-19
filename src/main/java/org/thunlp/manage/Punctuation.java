package org.thunlp.manage;

import org.thunlp.base.Dat;
import org.thunlp.base.TaggedSentence;

import java.io.IOException;
import java.util.Vector;

public class Punctuation {
	Dat p_dat;

	public Punctuation(String filename) throws IOException {
		this.p_dat = new Dat(filename);
	}

	    /*
		void adjust(TaggedSentence& sentence){
	        if(!p_dat)return;
	        for(int i = 0 ; i < sentence.size(); i ++){
	            Word tmp = sentence[i].word;

				if(p_dat->match(tmp) != -1){
					sentence[i].tag = "w";
				}
	        }
	    };
	    */

//	    public void adjust(SegmentedSentence sentence){
//	        if(p_dat == null)return;
//	        Vector<String> tmpVec = new Vector<String>();
//	        for(int i = 0 ; i < sentence.size(); i ++){
//	            String tmp = sentence.get(i);
//	            if(p_dat.getInfo(tmp) >= 0) continue;
//
//	            //std::cout<<tmp<<std::endl;
//
//	            tmpVec.clear();
//	            int j;
//	            for(j = i + 1; j < sentence.size(); j ++){
//	                tmp += sentence.get(j);
//	                if(p_dat.getInfo(tmp) >= 0){
//	                    break;
//	                }
//	                tmpVec.add(tmp);
//	            }
//	            int vecSize = (int)tmpVec.size();
//
//	            //std::cout<<vecSize<<std::endl;
//	            for(int k = vecSize - 1; k >= 0; k--){
//	                tmp = tmpVec.get(k);
//	                //std::cout<<k<<":"<<tmp<<std::endl;
//	                if(p_dat.match(tmp) != -1){
//	                    //std::cout<<p_dat->match(tmp)<<std::endl;
//	                    for(j = i + 1; j < i + k + 2; j ++){
//	                    	String stmp = sentence.get(i) + sentence.get(j);
//	                        sentence.set(i,stmp);
//	                    }
//	                    for(j = i + k + 1; j > i; j--){
//	                        sentence.remove(j);
//	                    }
//	                    break;
//	                }
//	            }
//	        }
//	        tmpVec.clear();
//	    };

	public void adjust(TaggedSentence sentence) {
		if (this.p_dat == null) return;
		Vector<String> tmpVec = new Vector<>();
		boolean findMulti = false;
		for (int i = 0; i < sentence.size(); i++) {
			String tmp = sentence.get(i).word;
			if (this.p_dat.getInfo(tmp) >= 0) continue;

			//std::cout<<tmp<<std::endl;

			tmpVec.clear();
			int j;
			for (j = i + 1; j < sentence.size(); j++) {
				tmp += sentence.get(j).word;
				if (this.p_dat.getInfo(tmp) >= 0) {
					break;
				}
				tmpVec.add(tmp);
			}
			int vecSize = (int) tmpVec.size();

			//std::cout<<vecSize<<std::endl;
			findMulti = false;
			for (int k = vecSize - 1; k >= 0; k--) {
				tmp = tmpVec.get(k);
				//std::cout<<k<<":"<<tmp<<std::endl;
				if (this.p_dat.match(tmp) != -1) {
					//std::cout<<p_dat->match(tmp)<<std::endl;
					for (j = i + 1; j < i + k + 2; j++) {
						sentence.get(i).word += sentence.get(j).word;
					}
					for (j = i + k + 1; j > i; j--) {
						sentence.remove(j);
					}
					sentence.get(i).tag = "w";
					findMulti = true;
					break;
				}
			}

			if (!findMulti) {
				if (this.p_dat.match(sentence.get(i).word) != -1) {
					sentence.get(i).tag = "w";
				}
			}

		}
		tmpVec.clear();
	}

}
