package manage;

import base.Dat;
import base.WordWithTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amber on 16/11/25.
 */
public class Adjust {
    static TimeWord timeWord = new TimeWord();

    public List<WordWithTag> ajust(List<WordWithTag> taggedSentence) {
        taggedSentence = nsIdiomAdjust(taggedSentence, "ns", Constants.MODEL_DAT_NS);
        taggedSentence = nsIdiomAdjust(taggedSentence, "i", Constants.MODEL_DAT_IDIOM);
        taggedSentence = puncAdjust(taggedSentence);
        taggedSentence = timeWord.adjustDouble(taggedSentence);
        taggedSentence = negAdjust(taggedSentence);
        taggedSentence = verbAdjust(taggedSentence);
        return taggedSentence;
    }

    public static List<WordWithTag> verbAdjust(List<WordWithTag> taggedSentence) {
        Dat vd = (Dat) WordDictionary.getInstance().getFile(Constants.MODEL_DAT_VD);
        Dat vm = (Dat) WordDictionary.getInstance().getFile(Constants.MODEL_DAT_VM);
        if((vm == null)||(vd == null))return taggedSentence;
        for(int i = 0;i < taggedSentence.size()-1;i++){
            if((taggedSentence.get(i).tag == "v")&&(taggedSentence.get(i+1).tag == "v")){
                if(vm.match(taggedSentence.get(i).word)!=-1){
                    taggedSentence.get(i).tag="vm";
                }else if(vd.match(taggedSentence.get(i+1).word)!=-1){
                    taggedSentence.get(i+1).tag="vd";
                }
            }
        }
        return taggedSentence;
    }

    public static List<WordWithTag> negAdjust(List<WordWithTag> taggedSentence) {
        Dat neg = (Dat) WordDictionary.getInstance().getFile(Constants.MODEL_DAT_NEG);
        if((neg == null))return taggedSentence;
        for(int i = taggedSentence.size()-1;i >= 0;i --){
            if(neg.match(taggedSentence.get(i).word) != -1){
                WordWithTag tmpWord = new WordWithTag();
                tmpWord.word = "";
                tmpWord.word += (taggedSentence.get(i).word.charAt(1));
                tmpWord.tag = "v";
                taggedSentence.add(i + 1, tmpWord);
                int tmpInt = taggedSentence.get(i).word.charAt(0);
                taggedSentence.get(i).word = "";
                taggedSentence.get(i).word += (char)tmpInt;
                taggedSentence.get(i).tag = "d";
            }
        }
        return taggedSentence;
    }

    public static List<WordWithTag> puncAdjust(List<WordWithTag> taggedSentence) {
        Dat puncDic = (Dat) WordDictionary.getInstance().getFile(Constants.MODEL_DAT_SINGLEPUN);
        if(puncDic==null)return taggedSentence;
        List<String> tmpVec= new ArrayList<>();
        boolean findMulti = false;
        for(int i = 0 ; i < taggedSentence.size(); i ++){
            String tmp = taggedSentence.get(i).word;
            if(puncDic.getInfo(tmp) >= 0) continue;
            tmpVec.clear();
            int j;
            for(j = i + 1; j < taggedSentence.size(); j ++){
                tmp += taggedSentence.get(j).word;
                if(puncDic.getInfo(tmp) >= 0){
                    break;
                }
                tmpVec.add(tmp);
            }
            int vecSize = tmpVec.size();
            findMulti = false;
            for(int k = vecSize - 1; k >= 0; k--){
                tmp = tmpVec.get(k);
                if(puncDic.match(tmp) != -1){
                    for(j = i + 1; j < i + k + 2; j ++){
                        taggedSentence.get(i).word += taggedSentence.get(j).word;
                    }
                    for(j = i + k + 1; j > i; j--){
                        taggedSentence.remove(j);
                    }
                    taggedSentence.get(i).tag = "w";
                    findMulti = true;
                    break;
                }
            }
            if(!findMulti){
                if(puncDic.match(taggedSentence.get(i).word) != -1){
                    taggedSentence.get(i).tag = "w";
                }
            }
        }
        return taggedSentence;
    }

    public static List<WordWithTag> nsIdiomAdjust(List<WordWithTag> taggedSentence, String tag, String fileName) {
        Dat nsDic = (Dat) WordDictionary.getInstance().getFile(fileName);
        if(nsDic == null)return taggedSentence;
        List<String> tmpVec =new ArrayList<>();
        for(int i = 0 ; i < taggedSentence.size(); i ++){
            String tmp = taggedSentence.get(i).word;
            if(nsDic.getInfo(tmp) >= 0) continue;
            int j;
            for(j = i + 1; j < taggedSentence.size(); j ++){
                tmp += taggedSentence.get(j).word;
                if(nsDic.getInfo(tmp) >= 0){
                    break;
                }
                tmpVec.add(tmp);
            }
            int vecSize = tmpVec.size();

            for(int k = vecSize - 1; k >= 0; k--){
                tmp = tmpVec.get(k);
                if(nsDic.match(tmp) != -1){
                    for(j = i + 1; j < i + k + 2; j ++){
                        taggedSentence.get(i).word += taggedSentence.get(j).word;
                    }
                    for(j = i + k + 1; j > i; j--){
                        taggedSentence.remove(j);
                    }
                    taggedSentence.get(i).tag = tag;
                    break;
                }
            }
        }
        return taggedSentence;
    }
}
