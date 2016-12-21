package manage;

import base.Dat;
import base.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by amber on 16/11/25.
 */
public class WordDictionary {
    static Logger logger = LoggerFactory.getLogger(WordDictionary.class);
    private static refactor.WordDictionary singleton;
    public Map<String, Object> wordsMap = new HashMap<String, Object>();
    private static final String PATH_PREFIX = "models/";

    /** 加载词性标注所需模型文件 **/
    public static final String MODEL_BIN_MODEL     = PATH_PREFIX + "model_c_model.bin";
    public static final String MODEL_BIN_DAT       = PATH_PREFIX + "model_c_dat.bin";
    public static final String MODEL_TXT_LABEL     = PATH_PREFIX + "model_c_label.txt";
    public static final String MODEL_DAT_T2S       = PATH_PREFIX + "t2s.dat";
    public static final String MODEL_DAT_NS        = PATH_PREFIX + "ns.dat";
    public static final String MODEL_DAT_IDIOM     = PATH_PREFIX + "idiom.dat";
    public static final String MODEL_DAT_SINGLEPUN = PATH_PREFIX + "singlepun.dat";
    public static final String MODEL_DAT_NEG       = PATH_PREFIX + "neg.dat";
    public static final String MODEL_DAT_VM        = PATH_PREFIX + "vM.dat";
    public static final String MODEL_DAT_VD        = PATH_PREFIX + "vD.dat";

    public static final HashMap<Integer,Integer> t2s = new HashMap<Integer,Integer>();
    public static final HashMap<Integer,Integer> s2t = new HashMap<Integer,Integer>();
    public static String[] labelInfo           = new String[10000];
    public static int[][] pocsToTags           = new int[16][];
    public int cbModellSize             = 0;
    public int cbModelfSize             = 0;
    public int[] llWeights;
    public int[] flWeights;

    public static refactor.WordDictionary getInstance(){
        if (singleton == null) {
            synchronized (refactor.WordDictionary.class) {
                if (singleton == null) {
                    singleton = new refactor.WordDictionary();
                    return singleton;
                }
            }
        }
        return singleton;
    }

    private WordDictionary(){
        try {
            this.loadDict();
        } catch (IOException e) {
            throw new RuntimeException("WordDictionary error occurs while init " );
        }
    }

    public void loadDict() throws IOException {
        long start = System.currentTimeMillis();
        setT2SMap();
        loadLabelTxt();
        wordsMap.put(Constants.MODEL_DAT_NS, loadDatDic(MODEL_DAT_NS));
        wordsMap.put(Constants.MODEL_DAT_IDIOM, loadDatDic(MODEL_DAT_IDIOM));
        wordsMap.put(Constants.MODEL_DAT_SINGLEPUN, loadDatDic(MODEL_DAT_SINGLEPUN));
        wordsMap.put(Constants.MODEL_DAT_NEG, loadDatDic(MODEL_DAT_NEG));
        wordsMap.put(Constants.MODEL_DAT_VM, loadDatDic(MODEL_DAT_VM));
        wordsMap.put(Constants.MODEL_DAT_VD, loadDatDic(MODEL_DAT_VD));
        wordsMap.put(Constants.MODEL_BIN_MODEL, loadCBModelDic());
        wordsMap.put(Constants.MODEL_BIN_DAT, loadDatDic(MODEL_BIN_DAT));
        long end = System.currentTimeMillis();
        logger.info("-------------- loadDict finished " + (end - start) + "ms -------------");
    }

    public Object getFile(String fileName) {
        return wordsMap.get(fileName);
    }

    public Dat loadDatDic(String filename){
        try {
            File file = new File(refactor.WordDictionary.class.getClassLoader().getResource(filename).getFile());
//            File file = new File(filename);
            int datSize = (int)(file.length() / 8);
            FileInputStream in = new FileInputStream(file);
            byte[] tempbytes = new byte[8 * datSize];
            Vector<Entry> dat = new Vector<Entry>();
            in.read(tempbytes);
            for(int i = 0; i < datSize; i ++){
                Entry entry = new Entry();
                entry.base = bytesToInt(tempbytes, 8 * i);
                dat.add(entry);
                dat.get(i).check = bytesToInt(tempbytes, 8 * i + 4);
            }
            Dat newDat = new Dat(datSize, dat);
            return newDat;
        } catch (Exception e) {
            throw new RuntimeException("WordDictionary Could not find file: " + filename);
        }
    }

    public CBModel loadCBModelDic() {
        InputStream in = refactor.WordDictionary.class.getClassLoader().getResourceAsStream(MODEL_BIN_MODEL);
//        File file = new File(MODEL_BIN_MODEL);
//        FileInputStream in = null;
//        try {
//            in = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        CBModel cbModel = null;

        byte[] tempbytes = new byte[4];
        try {
            in.read(tempbytes);
            cbModellSize = bytesToInt(tempbytes, 0);
            in.read(tempbytes);
            cbModelfSize = bytesToInt(tempbytes, 0);

            cbModel = new CBModel(cbModellSize, cbModelfSize);
            llWeights = new int[cbModellSize * cbModellSize];
            tempbytes = new byte[4 * llWeights.length];
            in.read(tempbytes);
            for(int i = 0; i < llWeights.length; i ++){
                llWeights[i] = bytesToInt(tempbytes, 4 * i);
            }
            flWeights = new int[cbModelfSize * cbModellSize];
            tempbytes = new byte[4 * flWeights.length];
            in.read(tempbytes);
            for(int i = 0; i < flWeights.length; i ++){
                flWeights[i] = bytesToInt(tempbytes, 4 * i);
            }

            cbModel.ll_weights = llWeights;
            cbModel.fl_weights = flWeights;
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cbModel;
    }

    public void setT2SMap() {
        File file = new File(refactor.WordDictionary.class.getClassLoader().getResource(MODEL_DAT_T2S).getFile());
        int datSize = (int)(file.length() / 8);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int[] tra = new int[datSize];
        int[] sim = new int[datSize];
        byte[] tempbytes = new byte[4 * datSize];
        try {
            in.read(tempbytes);
            for(int i=0;i<datSize;i++)
            {
                tra[i] = bytesToInt(tempbytes, 4 * i);
            }
            in.read(tempbytes);
            for(int i=0;i<datSize;i++)
            {
                sim[i] = bytesToInt(tempbytes, 4 * i);
            }
            for(int i = 0; i < datSize; i ++){
                t2s.put(tra[i], sim[i]);
                s2t.put(sim[i], tra[i]);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLabelTxt() {
        Vector<Vector<Integer>> pocTags = new Vector<Vector<Integer>>();
        for(int i = 0; i < 16; i ++){
            pocTags.add(new Vector<Integer>());
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(refactor.WordDictionary.class.getClassLoader().getResourceAsStream(MODEL_TXT_LABEL),"utf-8"));
            String raw = "";
            int ind = 0;
            while((raw = in.readLine()) != null){
                labelInfo[ind] = raw;
                int segInd = raw.charAt(0) - '0';
                for(int j = 0; j < 16; j ++){
                    if(((1<<segInd) & j) != 0){
                        pocTags.get(j).add(ind);
                    }
                }
                ind ++;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int j = 1; j < 16; j ++){
            pocsToTags[j] = new int[pocTags.get(j).size() + 1];
            for(int k = 0; k < pocTags.get(j).size(); k ++){
                pocsToTags[j][k] = pocTags.get(j).get(k);
            }
            pocsToTags[j][pocTags.get(j).size()] = -1;
        }
    }

    public static int bytesToInt(byte[] bb, int index) {
        return (((((int)bb[index + 3] & 0xff) << 24)
                | (((int)bb[index + 2] & 0xff) << 16)
                | (((int)bb[index + 1] & 0xff) << 8) | (((int)bb[index + 0] & 0xff) << 0)));
    }
}
