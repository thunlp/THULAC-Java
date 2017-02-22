package thulac;

import java.io.*;
import java.util.Vector;
import java.util.regex.*;

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

public class Thulac {

    /**
     * @param input
     * @throws IOException
     */
    public boolean useT2S = false;
    public boolean seg_only = false;
    public boolean useFilter = false;
    public String user_specified_dict_name;
    public Character separator;
    public String model_path_char;
//    public String output_file;
//    public String input_file;
    public POCGraph poc_cands;
    public TaggedSentence tagged;
    public SegmentedSentence segged;
    public CBTaggingDecoder cws_tagging_decoder;
    public CBTaggingDecoder tagging_decoder;
    public Preprocesser preprocesser;
    public Postprocesser nsDict;
    public Postprocesser idiomDict;
    public Postprocesser userDict;
    public Punctuation punctuation;
    public TimeWord timeword;
    public NegWord negword;
    public Filter filter;
    public String raw;
    public BufferedReader reader;
    public int maxLength = 50000;
    public FileOutputStream out;


    public Thulac (String input) throws IOException {
        separator = '_';
        raw = new String();
        int c = 0;
        String[] args = input.split(" ");
        while(c < args.length){
            String arg = args[c];
            if(arg.equals("")) {
                c++;
                continue;
            }
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
//            }else if(arg.equals("-input")){
//                input_file = args[++c];
//            }else if(arg.equals("-output")){
//                output_file = args[++c];
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
        poc_cands = new POCGraph();
        tagged = new TaggedSentence();
        segged = new SegmentedSentence();

        cws_tagging_decoder=new CBTaggingDecoder();
        tagging_decoder=new CBTaggingDecoder();
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

        preprocesser = new Preprocesser();
        preprocesser.setT2SMap((prefix+"t2s.dat"));
        nsDict = new Postprocesser((prefix+"ns.dat"), "ns", false);
        idiomDict = new Postprocesser((prefix+"idiom.dat"), "i", false);
        userDict = null;
        if(user_specified_dict_name!=null){
            userDict = new Postprocesser(user_specified_dict_name, "uw", true);
        }
        punctuation = new Punctuation((prefix+"singlepun.dat"));
        timeword = new TimeWord();
        negword = new NegWord((prefix+"neg.dat"));
        filter = null;
        if(useFilter){
            filter = new Filter((prefix+"xu.dat"), (prefix+"time.dat"));
        }


        long startTime = System.currentTimeMillis();//获取当前时间



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

    public Vector<Vector<String>> cut(String oiraw) throws IOException {
        Vector<Vector<String>> output = new Vector<Vector<String>>();
        String vec[] = oiraw.split("\n");
        for(int i=0; i<vec.length; i++) {
            Vector<Vector<String>> line = cutline(vec[i]);
            for(int j=0; j<line.size(); j++) {
                output.add(line.get(j));
            }
            Vector<String> slash = new Vector<String>();
            slash.add("\n");
            slash.add("");
            output.add(slash);
        }
        return output;
    }

    public Vector<Vector<String>> cutline(String oiraw) throws IOException {
        Vector<Vector<String>> output = new Vector();
        Vector<String> cutted_oiraw = getRaw(oiraw, maxLength);
        for(int i=0; i<cutted_oiraw.size(); i++) {
            if(useT2S) {
                String traw = new String();
                traw = preprocesser.clean(oiraw,poc_cands);
                raw = preprocesser.T2S(traw);
            }
            else{

                raw = preprocesser.clean(oiraw,poc_cands);
            }
            if(raw.length()>0) {
                if (seg_only) {

                    cws_tagging_decoder.segment(raw, poc_cands, tagged);
                    cws_tagging_decoder.get_seg_result(segged);
                    nsDict.adjust(segged);
                    idiomDict.adjust(segged);
                    if (userDict != null) {
                        userDict.adjust(segged);
                    }
                    punctuation.adjust(segged);
                    timeword.adjust(segged);
                    negword.adjust(segged);
                    if (useFilter) {
                        filter.adjust(segged);
                    }
                    for (int j = 0; j < segged.size(); j++) {
                        Vector<String> tmp = new Vector();
                        tmp.add(segged.get(j));
                        tmp.add("");
                        output.add(tmp);

                    }
                } else {
                    tagging_decoder.segment(raw, poc_cands, tagged);
                    nsDict.adjust(tagged);
                    idiomDict.adjust(tagged);
                    if (userDict != null) {
                        userDict.adjust(tagged);
                    }
                    punctuation.adjust(tagged);
                    timeword.adjustDouble(tagged);
                    negword.adjust(tagged);
                    if (useFilter) {
                        filter.adjust(tagged);
                    }
                    for (int j = 0; j < tagged.size(); j++) {
                        Vector<String> tmp = new Vector();
                        tmp.add(tagged.get(j).word);
                        tmp.add(tagged.get(j).tag);
                        output.add(tmp);
                    }
                }
            }
        }

        return output;
    }

    public String cut2string(String oiraw) throws IOException {
        String vec[] = oiraw.split("\n");
        StringBuilder output = new StringBuilder();
        for(int i=0; i<vec.length; i++) {
            Vector<Vector<String>> line = cutline(vec[i]);
            if(seg_only) {
                for(int j=0; j<line.size(); j++) {
                    output.append(line.get(j).get(0));
                    output.append(' ');
                }
            }
            else {
                for(int j=0; j<line.size(); j++) {
                    output.append(line.get(j).get(0));
                    output.append(line.get(j).get(1));
                    output.append(' ');
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    public void run() throws IOException {
        while (true) {
            String oiraw = getRaw(reader);
            if (oiraw == null) break;
            System.out.println(cut(oiraw));
        }
    }

    public void cut_f(String input_file, String output_file) throws IOException {
        reader = null;
        FileWriter fw;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input_file)), "UTF8"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(output_file != null){
            fw = new FileWriter(output_file);
        }
        else {
            System.out.println("没有输出文件名，默认为outputfile.txt");
            fw = new FileWriter("outputfile.txt");
        }
        while(true) {
            String oiraw = buffer2string(reader, maxLength);
            if(oiraw == null) break;
            String output = cut2string(oiraw);
            try {
                fw.write(output);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        fw.close();
//        out.write('\n');
    }

    public static String buffer2string(BufferedReader reader, int maxLength)
    {
        String ans=null;
        Vector<String> ans_vec = new Vector<String>();
        try {
            while((ans = reader.readLine()) != null)
            {
                break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ans;
    }

    public static Vector<String> getRaw(String ans, int maxLength)
    {
        Vector<String> ans_vec = new Vector<String>();
        if(ans == null) return ans_vec;
        if(ans.length() < maxLength) {
            ans_vec.add(ans);
        }
        else {
            Pattern p = Pattern.compile(".*?[。？！；;!?]");
            Matcher m = p.matcher(ans);
            int num = 0, pos = 0;
            String tmp;
            while(m.find()) {
                tmp = m.group(0);
                if(num + tmp.length() > maxLength) {
                    ans_vec.add(ans.substring(pos, pos+num));
                    pos += num;
                    num = tmp.length();
                }
                else {
                    num += tmp.length();
                }
            }
            if(pos != ans.length()) ans_vec.add(ans.substring(pos));
        }
        return ans_vec;
    }
}
//
