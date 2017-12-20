package manage;

import base.WordWithTag;

import java.util.*;

/**
 * Created by amber on 16/11/14.
 */
public class Thulac {
    public Thulac() {}

    /**
     * 获得词性标注的结果
     * @param text
     * @return
     */
    public List<WordWithTag> getLabelResult(String text) {
        Preprocesser prepro = new Preprocesser();
        List<Integer> pocCands = new ArrayList<Integer>();
        text = prepro.clean(text, pocCands);
        List<WordWithTag> resultList = new ArrayList<WordWithTag>();

        List<WordWithTag> taggedSentence = getTagged(pocCands, text);
        if(taggedSentence == null || taggedSentence.size() == 0) {
            return resultList;
        }
        for (int i = 0; i < taggedSentence.size(); i ++) {
            resultList.add(taggedSentence.get(i));
        }
        return resultList;
    }

    public List<WordWithTag> getTagged(List<Integer> pocCands, String text) {
        if (text == null || text.length() == 0) {
            return new ArrayList<>();
        }
        List<WordWithTag> taggedList = new ArrayList<>();
        Calculation cal = new Calculation();
        taggedList = cal.getTagList(text, pocCands, taggedList);
        return taggedList;
    }

    public static void main(String args[]) {
        Thulac thulac = new Thulac();
        String text = "今天高高兴兴去上学";
        List<WordWithTag> result = thulac.getLabelResult(text);
        for (WordWithTag word : result) {
            System.out.println(word.toString());
        }
    }
}
