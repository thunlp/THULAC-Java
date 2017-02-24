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
    public boolean segOnly = false;
    public boolean useFilter = false;
    public String userLexicon;
    public Character separator;
    public String modelPath;
    public POCGraph pocCands;
    public TaggedSentence tagged;
    public SegmentedSentence segged;
    public CBTaggingDecoder cwsTaggingDecoder;
    public CBTaggingDecoder taggingDecoder;
    public Preprocesser preprocesser;
    public Postprocesser nsDict;
    public Postprocesser idiomDict;
    public Postprocesser userDict;
    public Punctuation punctuation;
    public TimeWord timeWord;
    public NegWord negWord;
    public Filter filter;
    public String raw;
    public BufferedReader reader;
    public int maxLength = 50000;

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
                userLexicon = args[++c];
            }else if(arg.equals("-deli")){
                separator = args[++c].charAt(0);
            }else if(arg.equals("-seg_only")){
                segOnly = true;
            }else if(arg.equals("-filter")){
                useFilter = true;
            }else if(arg.equals("-model_dir")){
                modelPath = args[++c];
            }else {
                //showhelp();
                return;
            }
            c ++;
        }

        String prefix;
        if(modelPath != null){
            prefix = modelPath;
            if(prefix.charAt(prefix.length()-1) != '/'){
                prefix += "/";
            }
        }else{
            prefix = "models/";
        }

        String oiraw;
        pocCands = new POCGraph();
        tagged = new TaggedSentence();
        segged = new SegmentedSentence();

        cwsTaggingDecoder=new CBTaggingDecoder();
        taggingDecoder=new CBTaggingDecoder();
        if(segOnly) {
            cwsTaggingDecoder.threshold = 0;
            cwsTaggingDecoder.separator = separator;
            cwsTaggingDecoder.init((prefix+"cws_model.bin"),(prefix+"cws_dat.bin"),(prefix+"cws_label.txt"));
            cwsTaggingDecoder.setLabelTrans();
        } else {
            taggingDecoder.threshold = 10000;
            taggingDecoder.separator = separator;
            taggingDecoder.init((prefix+"model_c_model.bin"),(prefix+"model_c_dat.bin"),(prefix+"model_c_label.txt"));
            taggingDecoder.setLabelTrans();
        }

        preprocesser = new Preprocesser();
        preprocesser.setT2SMap((prefix+"t2s.dat"));
        nsDict = new Postprocesser((prefix+"ns.dat"), "ns", false);
        idiomDict = new Postprocesser((prefix+"idiom.dat"), "i", false);
        userDict = null;
        if(userLexicon!=null){
            userDict = new Postprocesser(userLexicon, "uw", true);
        }
        punctuation = new Punctuation((prefix+"singlepun.dat"));
        timeWord = new TimeWord();
        negWord = new NegWord((prefix+"neg.dat"));
        filter = null;
        if(useFilter){
            filter = new Filter((prefix+"xu.dat"), (prefix+"time.dat"));
        }


        long startTime = System.currentTimeMillis();//获取当前时间



    }
    public static String getRaw(BufferedReader reader) {
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

    public static String buffer2string(BufferedReader reader, int maxLength) {
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

    public static Vector<String> getRaw(String ans, int maxLength) {
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

    public Vector<Vector<String>> cut(String oiraw) {
        Vector<Vector<String>> output = new Vector<Vector<String>>();
        String[] line = oiraw.split("\n");
        for(String l : line) {
            Vector<Vector<String>> cuttedLine = cutline(l);
            for(Vector<String> word : cuttedLine) {
                output.add(word);
            }
            Vector<String> slash = new Vector<String>();
            slash.add("\n");
            slash.add("");
            output.add(slash);
        }
        return output;
    }

    public Vector<Vector<String>> cutline(String oiraw) {
        Vector<Vector<String>> output = new Vector();
        Vector<String> cuttedOiraw = getRaw(oiraw, maxLength);
        for(String sentense : cuttedOiraw) {
            if(useT2S) {
                String traw = new String();
                traw = preprocesser.clean(sentense,pocCands);
                raw = preprocesser.T2S(traw);
            }
            else{

                raw = preprocesser.clean(sentense,pocCands);
            }
            if(raw.length()>0) {
                if (segOnly) {

                    cwsTaggingDecoder.segment(raw, pocCands, tagged);
                    cwsTaggingDecoder.get_seg_result(segged);
                    nsDict.adjust(segged);
                    idiomDict.adjust(segged);
                    if (userDict != null) {
                        userDict.adjust(segged);
                    }
                    punctuation.adjust(segged);
                    timeWord.adjust(segged);
                    negWord.adjust(segged);
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
                    taggingDecoder.segment(raw, pocCands, tagged);
                    nsDict.adjust(tagged);
                    idiomDict.adjust(tagged);
                    if (userDict != null) {
                        userDict.adjust(tagged);
                    }
                    punctuation.adjust(tagged);
                    timeWord.adjustDouble(tagged);
                    negWord.adjust(tagged);
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

    public String cut2string(String oiraw) {
        String vec[] = oiraw.split("\n");
        StringBuilder output = new StringBuilder();
        for(String l : vec) {
            Vector<Vector<String>> line = cutline(l);
            if(segOnly) {
                for(int j=0; j<line.size(); j++) {
                    output.append(line.get(j).get(0));
                    output.append(" ");
                }
            }
            else {
                for(int j=0; j<line.size(); j++) {
                    output.append(line.get(j).get(0));
                    output.append(line.get(j).get(1));
                    output.append(" ");
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
            System.out.println("读取文件失败");
            e.printStackTrace();
        }
        fw = new FileWriter(output_file);
        while(true) {
            String oiraw = buffer2string(reader, maxLength);
            if(oiraw == null) break;
            String output = cut2string(oiraw);
            try {
                fw.write(output);
            } catch (IOException e) {
                System.out.println("写入文件失败");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        fw.close();
//        out.write('\n');
    }
}
//
