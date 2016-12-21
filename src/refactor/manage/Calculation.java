package manage;

import base.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by amber on 16/11/22.
 */
public class Calculation {
    private static WordDictionary wordDict = WordDictionary.getInstance();
    static int lsize = wordDict.cbModellSize;
    static int[] flWeights = wordDict.flWeights;
    static int[] llWeights = wordDict.llWeights;
    private static int MAX_LENGTH = 10000;
    private static int SENTENCE_BOUNDARY='#';
    private static int SEPERATOR = ' ';

    public static List<WordWithTag> getTagList(String text, List<Integer> graph, List<WordWithTag> taggedList) {
        String labelInfo[] = wordDict.labelInfo;
        int length         = text.length();
        int offset         = 0;
        int[] result       = getResult(text, graph);

        for (int i = 0;i < text.length(); i ++) {
            if ((i == length - 1) || (labelInfo[result[i]].charAt(0) == '2') || (labelInfo[result[i]].charAt(0) == '3')) {
                taggedList.add(new WordWithTag());
                for(int j = offset;j < i + 1; j ++) {
                    taggedList.get(taggedList.size() - 1).word += (text.charAt(j));
                }
                offset = i + 1;
                if((labelInfo[result[i]]+1) != null){//输出标签（如果有的话）
                    taggedList.get(taggedList.size() - 1).tag = labelInfo[result[i]].substring(1);
                }
            }
        }
        Adjust ajust = new Adjust();
        taggedList = ajust.ajust(taggedList);
        return taggedList;
    }

    public static int[] getResult(String text, List<Integer> graph) {
        int length = text.length();
        int[][] allowedLabelLists = allowedLabelLists(length, graph);
        int[] values = getValues(text);
        int[] result = new int[length * lsize];
        Node[] nodes = getNodes(length);
        AlphaBeta[] alphas = new AlphaBeta[length * lsize];
        int[][] preLabel = getLabelPre();
        result = dbDecode(lsize, llWeights, length, nodes, values, alphas, result, preLabel, allowedLabelLists);
        return result;
    }

    public static int[] getValues(String text) {
        int length   = text.length();
        if (length > MAX_LENGTH) {
            return new int[0];
        }
        int size     = lsize * length;
        int values[] = new int[2*size];
        values       = putValues(values, text, lsize);
        return values;
    }

    public static int[][] getLabelPre() {
        String labelInfo[] = wordDict.labelInfo;
        List<List<Integer>> preLabels = new ArrayList<List<Integer>>();
        List<List<Integer>> postLabels = new ArrayList<>();
        for(int i = 0; i < lsize; i ++){
            preLabels.add(new Vector<Integer>());
            postLabels.add(new Vector<Integer>());
        }
        for(int i = 0; i < lsize; i ++){
            for(int j = 0; j < lsize; j ++){
                int ni = labelInfo[i].charAt(0) - '0';
                int nj = labelInfo[j].charAt(0) - '0';
                boolean iIsEnd = ((ni == 2) || (ni == 3));
                boolean jIsBegin = ((nj == 0) || (nj == 3));
                boolean sameTag = labelInfo[i].substring(1).equals(labelInfo[j].substring(1));
                if(sameTag){
                    if((ni == 0 && nj == 1) ||
                            (ni == 0 && nj == 2) ||
                            (ni == 1 && nj == 2) ||
                            (ni == 1 && nj == 1) ||
                            (ni == 2 && nj == 0) ||
                            (ni == 2 && nj == 3) ||
                            (ni == 3 && nj == 3) ||
                            (ni == 3 && nj == 0)){
                        preLabels.get(j).add(i);
                        postLabels.get(i).add(j);
                    }
                }else{
                    if(iIsEnd && jIsBegin){
                        preLabels.get(j).add(i);
                        postLabels.get(i).add(j);
                    }
                }
            }
        }
        int[][] labelTransPre = new int[lsize][];
        for(int i = 0 ; i < lsize; i ++){
            labelTransPre[i] = new int[preLabels.get(i).size() + 1];
            for(int j = 0; j < preLabels.get(i).size(); j++){
                labelTransPre[i][j] = preLabels.get(i).get(j);
            }
            labelTransPre[i][preLabels.get(i).size()] = -1;
        }
        return labelTransPre;
    }

    public static int[] putValues(int[] values, String text, int size) {
        Dat dat = (Dat) wordDict.getFile(Constants.MODEL_BIN_DAT);
        int length = text.length();
        if(text.length() >= MAX_LENGTH){
            System.err.println("The text is too long...");
            return new int[0];
        }
        List<Integer> result = findBases(dat, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
        int[] uniBases         = new int[length + 2];
        int[] biBases          = new int[length + 4];
        uniBases[0]            = result.get(0);
        biBases[0]             = result.get(1);
        result                 = findBases(dat, SENTENCE_BOUNDARY,text.charAt(0));
        uniBases[0]            = result.get(0);
        biBases[1]             = result.get(1);

        for(int i = 0 ; i + 1 < text.length(); i ++){
            result          = findBases(dat, text.charAt(i), text.charAt(i+1));
            uniBases[i + 1] = result.get(0);
            biBases[i + 2]  = result.get(1);
        }

        result              = findBases(dat, (int)text.charAt(length - 1), SENTENCE_BOUNDARY);
        uniBases[length]    = result.get(0);
        biBases[length + 1] = result.get(1);

        result               = findBases(dat, SENTENCE_BOUNDARY, SENTENCE_BOUNDARY);
        uniBases[length + 1] = result.get(0);
        biBases[length + 2]  = result.get(1);

        int base = 0;
        for(int i = 0; i < length; i ++){
            int valueOffset = i * size;
            if((base = uniBases[i + 1]) != -1){
                values = addValues(valueOffset, base, 49, null, values);
            }
            if((base = uniBases[i]) != -1){
                values = addValues(valueOffset, base, 50, null, values);
            }
            if((base = uniBases[i + 2]) != -1){
                values = addValues(valueOffset, base, 51, null, values);
            }
            if((base = biBases[i + 1]) != -1){
                values = addValues(valueOffset, base, 49, null, values);
            }
            if((base = biBases[i + 2]) != -1){
                values = addValues(valueOffset, base, 50, null, values);
            }
            if((base = biBases[i]) != -1){
                values = addValues(valueOffset, base, 51, null, values);
            }
            if((base = biBases[i + 3]) != -1){
                values = addValues(valueOffset, base, 52, null, values);
            }
        }
        return values;
    }

    private static List<Integer> findBases(Dat dat, int ch1, int ch2){
        List<Integer> result = new ArrayList<>();
        int datSize = dat.getDatSize();
        List<Entry> vdat = dat.getDat();
        int uniBase;
        int biBase;
        if(ch1 > 32 && ch1 < 128) ch1+=65248;
        if(ch2 > 32 && ch2 < 128) ch2+=65248;
        if(ch1 >= datSize || vdat.get(ch1).check != 0){
            uniBase = -1;
            biBase = -1;
            result.clear();
            result.add(uniBase);
            result.add(biBase);
            return result;
        }
        uniBase = vdat.get(ch1).base + SEPERATOR;
        int ind = vdat.get(ch1).base + ch2;
        if(ind >= datSize || vdat.get(ind).check != ch1){
            biBase = -1;
            result.clear();
            result.add(uniBase);
            result.add(biBase);
            return result;
        }
        biBase = vdat.get(ind).base + SEPERATOR;
        result.clear();
        result.add(uniBase);
        result.add(biBase);
        return result;
    }

    private static int[] addValues(int valueOffset, int base, int del, int[] pAllowedLable, int[] values){
        Dat dat = (Dat) wordDict.getFile(Constants.MODEL_BIN_DAT);
        List<Entry> vdat = dat.getDat();
        int ind = vdat.get(base).base + del;
        int datSize = dat.getDatSize();
        if(ind >= datSize || vdat.get(ind).check != base){
            return values;
        }
        int offset = vdat.get(ind).base;
        int weightOffset = offset * lsize;
        int allowedLabel;
        if(lsize == 4){
            values[valueOffset] += flWeights[weightOffset];
            values[valueOffset + 1] += flWeights[weightOffset + 1];
            values[valueOffset + 2] += flWeights[weightOffset + 2];
            values[valueOffset + 3] += flWeights[weightOffset + 3];
        }else{
            if(pAllowedLable != null){
                for(int i = 0; i < pAllowedLable.length; i ++){
                    allowedLabel = pAllowedLable[i];
                    values[valueOffset + allowedLabel] += flWeights[weightOffset + allowedLabel];
                }
            }else{
                for(int i = 0; i < lsize; i ++){
                    values[valueOffset + i] += flWeights[weightOffset + i];
                }
            }
        }
        return values;
    }

    public static int[][] allowedLabelLists(int length, List<Integer> graph) {
        int [][] allowedLabelLists = new int[length][];
        int[][] pocsToTags = wordDict.pocsToTags;
        for (int i = 0; i < length; i ++) {
            allowedLabelLists[i] = null;
        }
        for(int i = 0; i < length;i++){
            int pocs = graph.get(i);
            if(pocs != 0){
                allowedLabelLists[i] = pocsToTags[pocs];
            }else{
                allowedLabelLists[i] = pocsToTags[15];
            }
        }
        return allowedLabelLists;
    }

    public static Node[] getNodes(int length) {
        if(length == 0) return new Node[0];
        Node[] nodes = new Node[length];
        for(int i = 0; i < length; i ++){
            nodes[i] = new Node();
            nodes[i].type = 0;
            int[] pre = new int[2];
            pre[0] = i - 1;
            pre[1] = -1;
            nodes[i].predecessors = pre;

            pre = new int[2];
            pre[0] = i + 1;
            pre[1] = -1;
            nodes[i].successors = pre;
        }
        nodes[0].type += 1;
        nodes[length-1].type += 2;
        return nodes;
    }

    public static int[] dbDecode(int l_size, int[] llWeights, int nodeCount, Node[] nodes, int[] values, AlphaBeta[] alphas,
                                 int[] result, int[][] preLabels, int[][] allowedLabelLists){
        int nodeId;
        int[] pNodeId;
        int[] pPreLabel;
        int[] pAllowedLabel;
        int k;
        int j;
        AlphaBeta tmp;
        AlphaBeta best = new AlphaBeta();
        best.nodeId = -1;
        AlphaBeta preAlpha;

        int score;
        int index = 0;
        int index2 = 0;
        int index3 = 0;

        for(int i = 0; i < nodeCount * l_size; i ++)
        {
            alphas[i]=new AlphaBeta();
            alphas[i].nodeId = -2;
        }
        for(int i = 0; i < nodeCount; i ++){
            pAllowedLabel = allowedLabelLists != null ? allowedLabelLists[i] : null;
            j = -1;
            int maxValue = 0;
            boolean hasMaxValue = false;
            if(pAllowedLabel != null){
                index = 0;
                while((j = pAllowedLabel[index]) != -1){
                    index ++;
                    if(!hasMaxValue || (maxValue < values[i*l_size +j])){
                        hasMaxValue = true;
                        maxValue = values[i*l_size + j];
                    }
                }
                index = 0;
                j = -1;
                while((j = pAllowedLabel[index]) != -1){
                    index ++;
                    tmp = alphas[i*l_size + j];
                    tmp.value = 0;
                    pNodeId = nodes[i].predecessors;
                    pPreLabel = preLabels != null ? preLabels[j] : null;
                    index2 = 0;
                    while((nodeId = pNodeId[index2]) >= 0){
                        index2 ++;
                        k = -1;
                        if(pPreLabel != null){
                            index3 = 0;
                            while((k = pPreLabel[index3]) != -1){
                                index3 ++;
                                preAlpha = alphas[nodeId * l_size + k];
                                if(preAlpha.nodeId == -2) continue;
                                score = preAlpha.value + llWeights[k*l_size + j];
                                if((tmp.nodeId<0) || (score > tmp.value)){
                                    tmp.value = score;
                                    tmp.nodeId = nodeId;
                                    tmp.labelId = k;
                                }
                            }
                        }else{
                            k ++;
                            while(k != l_size){
                                preAlpha = alphas[nodeId * l_size + k];
                                if(preAlpha.nodeId == -2) continue;
                                score = preAlpha.value + llWeights[k*l_size + j];
                                if((tmp.nodeId<0) || (score > tmp.value)){
                                    tmp.value = score;
                                    tmp.nodeId = nodeId;
                                    tmp.labelId = k;
                                }
                                k ++;
                            }
                        }
                    }
                    tmp.value += values[i*l_size + j];
                    if((nodes[i].type == 1) || (nodes[i].type == 3)){
                        tmp.nodeId = -1;
                    }
                    if(nodes[i].type >= 2){
                        if((best.nodeId == -1) || best.value < tmp.value){
                            best.value = tmp.value;
                            best.nodeId = i;
                            best.labelId = j;
                        }
                    }
                }

            }else{
                j ++;
                while(j != l_size){
                    if(!hasMaxValue || (maxValue < values[i*l_size +j])){
                        hasMaxValue = true;
                        maxValue = values[i*l_size + j];
                    }
                    j ++;
                }
                j = 0;
                while(j != l_size){
                    tmp = alphas[i*l_size + j];
                    tmp.value = 0;
                    pNodeId = nodes[i].predecessors;
                    pPreLabel = preLabels != null ? preLabels[j] : null;
                    index2 = 0;
                    while((nodeId = pNodeId[index2]) >= 0){
                        index2 ++;
                        k = -1;
                        if(pPreLabel != null){
                            index3 = 0;
                            while((k = pPreLabel[index3]) != -1){
                                index3 ++;
                                preAlpha = alphas[nodeId * l_size + k];
                                if(preAlpha.nodeId == -2) continue;
                                score = preAlpha.value + llWeights[k*l_size + j];
                                if((tmp.nodeId<0) || (score > tmp.value)){
                                    tmp.value = score;
                                    tmp.nodeId = nodeId;
                                    tmp.labelId = k;
                                }

                            }
                        }else{
                            k ++;
                            while(k != l_size){
                                preAlpha = alphas[nodeId * l_size + k];
                                if(preAlpha.nodeId == -2) continue;
                                score = preAlpha.value + llWeights[k*l_size + j];
                                if((tmp.nodeId<0) || (score > tmp.value)){
                                    tmp.value = score;
                                    tmp.nodeId = nodeId;
                                    tmp.labelId = k;
                                }
                                k ++;
                            }
                        }
                    }
                    tmp.value += values[i*l_size + j];
                    if((nodes[i].type == 1) || (nodes[i].type == 3)){
                        tmp.nodeId = -1;
                    }
                    if(nodes[i].type >= 2){
                        if((best.nodeId == -1) || best.value < tmp.value){
                            best.value = tmp.value;
                            best.nodeId = i;
                            best.labelId = j;
                        }
                    }
//					System.out.println(""+tmp.value+" "+tmp.nodeId+" "+tmp.labelId);
                    j ++;
                }

            }
        }
        tmp = best;
        while(tmp.nodeId >= 0){
            result[tmp.nodeId] = tmp.labelId;
            tmp = alphas[tmp.nodeId * l_size + tmp.labelId];
        }
        return result;
    }
}
