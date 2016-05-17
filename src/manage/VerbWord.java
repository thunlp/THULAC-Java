package manage;

import java.io.IOException;

import base.Dat;
import base.TaggedSentence;

public class VerbWord {
	    Dat vM_dat;
		Dat vD_dat;
		String tag_v;
	    public VerbWord(String filename, String filename2) throws IOException{
			vM_dat = new Dat(filename);
			vD_dat = new Dat(filename2);
			tag_v = "v";
	    };
	    

	    public void adjust(TaggedSentence sentence){
			if((vM_dat==null)||(vD_dat==null))return;
			for(int i=0;i<sentence.size()-1;i++){
				if((sentence.get(i).tag==tag_v)&&(sentence.get(i+1).tag==tag_v)){
					if(vM_dat.match(sentence.get(i).word)!=-1){
						sentence.get(i).tag="vm";
					}else if(vD_dat.match(sentence.get(i+1).word)!=-1){
						sentence.get(i+1).tag="vd";
					}
				}
			}
	    };


}
