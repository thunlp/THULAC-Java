package manage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import base.Dat;
import base.DatMaker;
import base.KeyValue;
import base.Raw;
import base.SegmentedSentence;
import base.TaggedSentence;

public class Postprocesser {
	private Dat p_dat;
	private String tag;
	public Postprocesser(String filename, String tag, boolean isTxt) throws IOException{
			this.tag = tag;
			if(isTxt)
			{
				BufferedReader buf = new BufferedReader (new InputStreamReader(new FileInputStream(filename)));
				Vector<KeyValue> lexicon=new Vector<KeyValue>();
				lexicon.add(new KeyValue());
				String str;
				int id = 0;
				while((str=buf.readLine()) != null)
	            {
					if(str.length()==0)continue;
					if(str.endsWith("\r")){
						str.substring(0, str.length()-1);
					}
					lexicon.lastElement().key=str;
					lexicon.lastElement().value=id;

					//init a new element
					lexicon.add(new KeyValue());
					id+=1;
				}
				DatMaker dm = new DatMaker();
				dm.makeDat(lexicon);
				dm.shrink();
				
				/*
				p_dat = new DAT();	
				p_dat->dat_size = dm->dat_size;
				p_dat->dat = (DAT::Entry*)calloc(sizeof(DAT::Entry), p_dat->dat_size);
				memcpy(p_dat->dat, dm->dat, sizeof(DAT::Entry)*(p_dat->dat_size));
				*/
				
				/*
				dm->save("models/user.dat");
				p_dat = new DAT("models/user.dat");
				*/
				
				p_dat = new Dat(dm.datSize, dm.dat);
				
			}else{
				p_dat = new Dat(filename);
			}
	    };
	    
	   public void adjust(SegmentedSentence sentence) {
		   if(p_dat ==null)return;
	        Vector<String> tmpVec = new Vector<String>();
	        for(int i = 0 ; i < sentence.size(); i ++){
	            String tmp = sentence.get(i);
	            if(p_dat.getInfo(tmp) >= 0) continue;

	            tmpVec.clear();
	            int j;
	            for(j = i + 1; j < sentence.size(); j ++){
	                tmp += sentence.get(j);
	                if(p_dat.getInfo(tmp) >= 0){
	                    break;
	                }
	                tmpVec.add(tmp);
	            }
	            int vecSize = (int)tmpVec.size();
	            
	            for(int k = vecSize - 1; k >= 0; k--){
	                tmp = tmpVec.get(k);
	                if(p_dat.match(tmp) != -1){
	                    for(j = i + 1; j < i + k + 2; j ++){
	                    	String stmp = sentence.get(i) + sentence.get(j);
	                        sentence.set(i,stmp);
	                    }
	                    for(j = i + k + 1; j > i; j--){
	                        sentence.remove(j);
	                    }
	                    break;
	                }
	            }

	        }
	        tmpVec.clear();
	   }

	   public void adjust(TaggedSentence sentence){
	        if(p_dat ==null)return;	        
	        Vector<String> tmpVec =new Vector<String>();
	        for(int i = 0 ; i < sentence.size(); i ++){
	            String tmp = sentence.get(i).word;
//	            System.out.println(tmp);
	            if(p_dat.getInfo(tmp) >= 0) continue;
	            
	            //std::cout<<tmp<<std::endl;

	            tmpVec.clear();
	            int j;
	            for(j = i + 1; j < sentence.size(); j ++){
	                tmp += sentence.get(j).word;
	                if(p_dat.getInfo(tmp) >= 0){
	                    break;
	                }
	                tmpVec.add(tmp);
	            }
	            int vecSize = (int)tmpVec.size();
	            
	            //std::cout<<vecSize<<std::endl;

	            for(int k = vecSize - 1; k >= 0; k--){
	                tmp = tmpVec.get(k);
	                //std::cout<<k<<":"<<tmp<<std::endl;
	                if(p_dat.match(tmp) != -1){
	                    //std::cout<<p_dat->match(tmp)<<std::endl;
	                    for(j = i + 1; j < i + k + 2; j ++){
	                        sentence.get(i).word += sentence.get(j).word;
	                    }
	                    for(j = i + k + 1; j > i; j--){
	                        sentence.remove(j);
	                    }
//	                    System.out.println(sentence.get(i).word);
//	                    System.out.println(sentence.get(i).tag);
	                    sentence.get(i).tag = tag;
	                    break;
	                }
	            }

	        }
	        tmpVec.clear();
	    };

	    public void adjustSame(TaggedSentence sentence){
	    	if(p_dat ==null)return;
	        Vector<String> tmpVec =new Vector<String>();
	        for(int i = 0 ; i < sentence.size(); i ++){
	            String tmp = sentence.get(i).word;
	            if(p_dat.getInfo(tmp) >= 0) continue;

	            if(p_dat.match(sentence.get(i).word) != -1){
	                sentence.get(i).tag = tag;
	            }

	        }
	        tmpVec.clear();
	    };
}
