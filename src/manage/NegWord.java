package manage;

import java.io.IOException;

import base.Dat;
import base.SegmentedSentence;
import base.TaggedSentence;
import base.WordWithTag;

public class NegWord {
	Dat neg_dat;
	public NegWord(String filename) throws IOException{
			neg_dat = new Dat(filename);
	    };
	    
	   public void adjust(SegmentedSentence sentence){
			if(neg_dat == null)return;
			for(int i=sentence.size()-1;i>=0;i--){
				if(neg_dat.match(sentence.get(i)) != -1){
			    	String tmpWord = "";
					tmpWord += (sentence.get(i).charAt(1));
					sentence.add(i + 1, tmpWord);
					int tmpInt = sentence.get(i).charAt(0);
					sentence.set(i, ""+tmpInt);
				}
			}
	    };

	   public void adjust(TaggedSentence sentence){
			if((neg_dat==null))return;	
			for(int i=sentence.size()-1;i>=0;i--){
				if(neg_dat.match(sentence.get(i).word) != -1){
			    	WordWithTag tmpWord= new WordWithTag(sentence.get(i).separator);
					tmpWord.word="";
					tmpWord.word+=(sentence.get(i).word.charAt(1));
					tmpWord.tag = "v";
					sentence.add(i + 1, tmpWord);
					int tmpInt = sentence.get(i).word.charAt(0);
					sentence.get(i).word="";
					sentence.get(i).word+=(char)tmpInt;
					sentence.get(i).tag = "d";
				}
			}
	    };

}
