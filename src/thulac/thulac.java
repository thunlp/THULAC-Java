package thulac;

import java.io.*;

import manage.Filter;
import manage.NegWord;
import manage.Postprocesser;
import manage.Preprocesser;
import manage.Punctuation;
import manage.TimeWord;

import base.POCGraph;
import base.SegmentedSentence;
import base.TaggedSentence;

import character.CBTaggingDecoder;

public class thulac {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException {
		String user_specified_dict_name=null;
	    String model_path_char = null ;

	    Character separator = '_';

	    boolean useT2S = false;
	    boolean seg_only = false;
	    boolean useFilter = false;
	    boolean use_second = false;
	    String input_file = "";
	    String output_file = "";
	    
	    int c = 0;
	    while(c < args.length){
	        String arg = args[c];
	        if(arg.equals("-t2s")){
	            useT2S = true;
	        }else if(arg.equals("-user")){
	            user_specified_dict_name = args[++c];
	        }else if(arg.equals("-deli")){
	            separator = args[++c].charAt(0);
	        }else if(arg.equals("-seg_only")){
	            seg_only = true;
	        }else if(arg.equals("-filter")){
	            useFilter = true;
	        }else if(arg.equals("-model_dir")){
	            model_path_char = args[++c];
	        }else if(arg.equals("-input")){
	        	input_file = args[++c];
	        }else if(arg.equals("-output")){
	        	output_file = args[++c];
	        }else {
	            //showhelp();
	        	return;
	        }
	        c ++;
	    }
	    
	    String prefix;
	    if(model_path_char != null){
	        prefix = model_path_char;
	        if(prefix.charAt(prefix.length()-1) != '/'){
	            prefix += "/";
	        }
	    }else{
	        prefix = "models/";
	    }
	    
	    String oiraw;
	    String raw =new String();
	    POCGraph poc_cands = new POCGraph();
	    TaggedSentence tagged = new TaggedSentence();
	    SegmentedSentence segged = new SegmentedSentence();
	    
	    CBTaggingDecoder cws_tagging_decoder=new CBTaggingDecoder();
	    CBTaggingDecoder tagging_decoder=new CBTaggingDecoder();
	    if(seg_only) {
	    	cws_tagging_decoder.threshold = 0;
	    	cws_tagging_decoder.separator = separator;
		    cws_tagging_decoder.init((prefix+"cws_model.bin"),(prefix+"cws_dat.bin"),(prefix+"cws_label.txt"));
		    cws_tagging_decoder.setLabelTrans();
	    } else {
	    	tagging_decoder.threshold = 10000;
	    	tagging_decoder.separator = separator;
		    tagging_decoder.init((prefix+"model_c_model.bin"),(prefix+"model_c_dat.bin"),(prefix+"model_c_label.txt"));
		    tagging_decoder.setLabelTrans();
	    }
	    
	    Preprocesser preprocesser = new Preprocesser();
	    preprocesser.setT2SMap((prefix+"t2s.dat"));
	    Postprocesser nsDict = new Postprocesser((prefix+"ns.dat"), "ns", false);
	    Postprocesser idiomDict = new Postprocesser((prefix+"idiom.dat"), "i", false);
	    Postprocesser userDict = null;
	    if(user_specified_dict_name!=null){
	        userDict = new Postprocesser(user_specified_dict_name, "uw", true);
	    }
	    Punctuation punctuation = new Punctuation((prefix+"singlepun.dat"));
	    TimeWord timeword = new TimeWord();
	    NegWord negword = new NegWord((prefix+"neg.dat"));
	    Filter filter = null;
	    if(useFilter){
	        filter = new Filter((prefix+"xu.dat"), (prefix+"time.dat"));
	    }
	    
	    BufferedReader reader = null;
        try {
        	if(input_file != "") {
        		reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input_file)), "UTF8"));
    	    }
        	else reader = new BufferedReader(new InputStreamReader(System.in));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        FileOutputStream out = null;
        if(output_file != ""){
        	out=new FileOutputStream(output_file);
        }
        
        long startTime = System.currentTimeMillis();//获取当前时间
	    while(true)
	    {
	    	oiraw=getRaw(reader);
	    	if(oiraw==null) break;
	    	
	    	if(useT2S) {
	    		String traw = new String();
	    		traw = preprocesser.clean(oiraw,poc_cands);
				raw = preprocesser.T2S(traw);
			}
	    	else{
	    		raw = preprocesser.clean(oiraw,poc_cands);
			}
//	    	System.out.print(poc_cands.toString()+"\n");
	    	if(raw.length()>0)
	    	{
	    		if(seg_only) {
	    			
	    			cws_tagging_decoder.segment(raw, poc_cands, tagged);
	    			cws_tagging_decoder.get_seg_result(segged);
	    			nsDict.adjust(segged);
		    		idiomDict.adjust(segged);
		    		if(userDict!=null){
	                    userDict.adjust(segged);
	                }
		    		punctuation.adjust(segged);
		    		timeword.adjust(segged);
		    		negword.adjust(segged);
		    		if(useFilter){
	                    filter.adjust(segged);
	                }
		    		if(out != null) {
	    				for(int i=0;i<segged.size();i++) {
	    					byte[] buff=new byte[]{};
	    					buff=segged.get(i).getBytes();
	    					try {
	    						out.write(buff,0,buff.length);
	    					} catch (IOException e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    					}
	    					out.write(' ');
	    				}
	    				out.write('\n');
	    			}
	    			else {
	    				for(int i=0;i<segged.size();i++) System.out.print(segged.get(i) + " ");
	    				System.out.print("\n");
	    			}
	    		}
	    		else {
	    			tagging_decoder.segment(raw, poc_cands, tagged);
	    			nsDict.adjust(tagged);
	    			idiomDict.adjust(tagged);
	    			if(userDict!=null){
	    				userDict.adjust(tagged);
	    			}
	    			punctuation.adjust(tagged);
	    			timeword.adjustDouble(tagged);
	    			negword.adjust(tagged);
	    			if(useFilter){
	    				filter.adjust(tagged);
	    			}
	    			
	    			if(out != null) {
		    			for(int i=0;i<tagged.size();i++) tagged.get(i).print(out);
		    			out.write('\n');
		    		}
		    		else {
		    			for(int i=0;i<tagged.size();i++) tagged.get(i).print();
		    			System.out.print("\n");
		    		}
	    		}
	    	}
	    }
	    long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
	    
	}
	public static String getRaw(BufferedReader reader)
	{
		String ans=null;
		try {
			while((ans = reader.readLine()) != null)
			{
				break;	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(ans);
		return ans;
	}
}
