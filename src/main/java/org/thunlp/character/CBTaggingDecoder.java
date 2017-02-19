/**
 * Created锛�May 15, 2013 6:52:50 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�CBTaggingDecoder.java  
 * description锛�  
 */
package org.thunlp.character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.thunlp.base.AlphaBeta;
import org.thunlp.base.Dat;
import org.thunlp.base.Node;
import org.thunlp.base.POCGraph;
import org.thunlp.base.SegmentedSentence;
import org.thunlp.base.TaggedSentence;
import org.thunlp.base.WordWithTag;

public class CBTaggingDecoder {

	public char separator;
	private int maxLength;
	private int len;
	private String sequence;
	private int[][] allowedLabelLists;
	private int[][] pocsToTags;
	
	private CBNGramFeature nGramFeature;
	private Dat dat;
	
	private CBModel model;
	
	private Node[] nodes;
	private int[] values;
	private AlphaBeta[] alphas;
	private AlphaBeta[] betas;
	private int bestScore;
	private int[] result;
	
	private String[] labelInfo;
	
	private int[] labelTrans;
	private int[][] labelTransPre;
	private int[][] labelTransPost;
	
	public int threshold;
	private int[] allowCom;
	
	private int tagSize;
	private int[][] labelLookingFor;
	private int[] isGoodChoice;
	
	public CBTaggingDecoder(){
		separator = '_';
		maxLength = 20000;
		len = 0;
		sequence =new String("");
		allowedLabelLists = new int[maxLength][];
		
		pocsToTags = null;
		nGramFeature = null;
		dat = null;
		nodes = new Node[maxLength];
		labelTrans = null;
		labelTransPre = null;
		labelTransPost = null;
		threshold = 0;
		
		allowCom = new int[maxLength];
		tagSize = 0;
		model = null;
		alphas = null;
		betas = null;
	}
	
	public void init(String modelFile, String datFile, String labelFile) throws IOException{
		model = new CBModel(modelFile);
		
		values = new int[maxLength*model.l_size];
		alphas = new AlphaBeta[maxLength*model.l_size];
		betas = new AlphaBeta[maxLength*model.l_size];
		result = new int[maxLength*model.l_size];
		
		for(int i = 0; i < maxLength; i ++){
			nodes[i] = new Node();
			
			int[] pre = new int[2];
			pre[0] = i - 1;
			pre[1] = -1;
			nodes[i].predecessors = pre;
			
			pre = new int[2];
			pre[0] = i + 1;
			pre[1] = -1;
			nodes[i].successors = pre;
		}
		
		dat = new Dat(datFile);
		nGramFeature = new CBNGramFeature(dat, model, values);
		
		labelInfo = new String[10000];
		Vector<Vector<Integer>> pocTags = new Vector<Vector<Integer>>();
		for(int i = 0; i < 16; i ++){
			pocTags.add(new Vector<Integer>());
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(labelFile)));
		String line = "";
		int ind = 0;
		while((line = in.readLine()) != null){
			labelInfo[ind] = line;
			int segInd = line.charAt(0) - '0';
			for(int j = 0; j < 16; j ++){
				if(((1<<segInd) & j) != 0){
					pocTags.get(j).add(ind);
				}
			}
			ind ++;
		}
		in.close();
		
		pocsToTags = new int[16][];
		for(int j = 1; j < 16; j ++){
			pocsToTags[j] = new int[pocTags.get(j).size() + 1];
			for(int k = 0; k < pocTags.get(j).size(); k ++){
				pocsToTags[j][k] = pocTags.get(j).get(k);
			}
			pocsToTags[j][pocTags.get(j).size()] = -1;
		}
		
//		for(int j = 1; j < pocsToTags[15].length; j++) {
//			System.out.print(pocsToTags[15][j]+" ");
//		}
		
		labelLookingFor = new int[model.l_size][];
		for(int i = 0; i < model.l_size; i ++){
			labelLookingFor[i] = null;
		}
		for(int i = 0; i < model.l_size; i ++){
			if(labelInfo[i].charAt(0) == '0' || labelInfo[i].charAt(0) == '3') continue;
			for(int j = 0; j <= i; j ++){
				if((labelInfo[i].substring(1).equals(labelInfo[j].substring(1))) && (labelInfo[j].charAt(0) == '0')){
					if(labelLookingFor[j] == null){
						labelLookingFor[j] = new int[2];
						labelLookingFor[j][0] = -1;
						labelLookingFor[j][1] = -1;
						tagSize ++;
					}
					labelLookingFor[j][labelInfo[i].charAt(0)-'1'] = i;
					break;
				}
			}
		}
		
		
		for(int i = 0; i < maxLength; i ++){
			allowedLabelLists[i] = null;
		}
		isGoodChoice = new int[maxLength * model.l_size];
	}
	
	public void loadLabelTrans(String fileName) throws IOException{
		FileInputStream in = new FileInputStream(fileName);
		byte[] tempbytes = new byte[4];
		in.read(tempbytes);
		int remainSize = bytesToInt(tempbytes, 0);
		tempbytes = new byte[4 * remainSize];
		in.read(tempbytes);

		labelTrans = new int[remainSize];
		int labelSize = 0;
		for(int i = 0; i < remainSize; i ++){
			labelTrans[i] = bytesToInt(tempbytes, 4 * i); 
			if(labelTrans[i] == -1){
				labelSize ++;
			}
		}
		
		labelSize /= 2;
		
		labelTransPre = new int[labelSize][];
		labelTransPost = new int[labelSize][];
		
		int preInd = 0;
		int ind = 0;
		int i = 0;
		while(i < labelSize){
			while(labelTrans[ind] != -1){
				ind ++;
			}
			ind ++;
			labelTransPre[i] = new int[ind - preInd];
			for(int j = 0; j < ind - preInd; j ++){
				labelTransPre[i][j] = labelTrans[preInd + j];
			}
			preInd = ind;
			while(labelTrans[ind] != -1){
				ind ++;
			}
			ind ++;
			labelTransPost[i] = new int[ind - preInd];
			for(int j = 0; j < ind - preInd; j ++){
				labelTransPost[i][j] = labelTrans[preInd + j];
			}
			i ++;
		}
		in.close();
	}
	
	public static int bytesToInt(byte[] bb, int index) {    
		return (int) ((((bb[index + 3] & 0xff) << 24) 
				| ((bb[index + 2] & 0xff) << 16) 
				| ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}
	
	public void dp(){
		if(allowedLabelLists[0] == null){
			allowedLabelLists[0] = pocsToTags[9];
		}
		if(allowedLabelLists[len - 1] == null){
			allowedLabelLists[len - 1] = pocsToTags[12];
		}
//		for(int i=0;i<200;i++){
//	        System.out.printf("%d ",model.ll_weights[i]);
//	    }
//		System.out.println();
		bestScore = AlphaBeta.dbDecode(model.l_size, model.ll_weights, len, nodes, values, alphas, result, labelTransPre, allowedLabelLists);
		allowedLabelLists[0] = null;
		allowedLabelLists[len - 1] = null;
//		for(int i=0;i<len;i++){
//	        System.out.printf("%s\n",labelInfo[result[i]]);
//	    }
	}
	
	public void setLabelTrans(){
		int lSize = model.l_size;
		Vector<Vector<Integer>> preLabels = new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> postLabels = new Vector<Vector<Integer>>();
		for(int i = 0; i < lSize; i ++){
			preLabels.add(new Vector<Integer>());
			postLabels.add(new Vector<Integer>());
		}
		for(int i = 0; i < lSize; i ++){
			for(int j = 0; j < lSize; j ++){
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
		labelTransPre = new int[lSize][];
//		System.out.println(preLabels.get(0));
		for(int i = 0 ; i < lSize; i ++){
			labelTransPre[i] = new int[preLabels.get(i).size() + 1];
			for(int j = 0; j < preLabels.get(i).size(); j++){
				labelTransPre[i][j] = preLabels.get(i).get(j);
			}
			labelTransPre[i][preLabels.get(i).size()] = -1;
		}
		
		labelTransPost = new int[lSize][];
		for(int i = 0 ; i < lSize; i ++){
			labelTransPost[i] = new int[postLabels.get(i).size() + 1];
			for(int j = 0; j < postLabels.get(i).size(); j++){
				labelTransPost[i][j] = postLabels.get(i).get(j);
			}
			labelTransPost[i][postLabels.get(i).size()] = -1;
		}
	}
	
	public void putValues(){
		if(len == 0) return;
		for(int i = 0; i < len; i ++){
			nodes[i].type = 0;
		}
		nodes[0].type += 1;
		nodes[len-1].type += 2;
		
		int size = len * model.l_size;
		for(int i = 0; i < size; i ++){
			values[i] = 0;
		}
		nGramFeature.putValues(sequence, len);
//		for(int i=0;i<size;i++) System.out.println(values[i]);
	}
	
	public int segment(String raw, POCGraph graph, TaggedSentence ts){
	    if(raw.length()==0) return 0;
	    
	    for(int i=0;i<(int)raw.length();i++){
	        int pocs = graph.get(i);
	        if(pocs!=0){
	        	allowedLabelLists[i]=pocsToTags[pocs];
	        }else{
	        	allowedLabelLists[i]=pocsToTags[15];
	        }
	    }
	    //std::cout<<"\n";
	    sequence="";
	    for(int i=0;i<(int)raw.length();i++){
	    	sequence+=raw.charAt(i);
//	    	System.out.println(raw.charAt(i));
	    }
	    len=(int)raw.length();
//	    for(int i=0;i<200;i++){
//	        System.out.printf("%d ",values[i]);
//	    }
//		System.out.println();
	    putValues();//检索出特征值并初始化放在values数组里
	    dp();//动态规划搜索最优解放在result数组里

	    for(int i=0;i<(int)raw.length();i++){
	    	allowedLabelLists[i]=null;
	    }
	    int offset=0;
	    ts.clear();
	    for(int i=0;i<len;i++){
	        if((i==len-1)||(labelInfo[result[i]].charAt(0)=='2')||(labelInfo[result[i]].charAt(0)=='3')){
	            ts.add(new WordWithTag(separator));
	            for(int j=offset;j<i+1;j++){
	                ts.lastElement().word+=(sequence.charAt(j));
	            }
//	            System.out.println(ts.lastElement().word);
	            offset=i+1;
	            if((labelInfo[result[i]]+1)!=null){//输出标签（如果有的话）
	                ts.lastElement().tag=labelInfo[result[i]].substring(1);
//	                System.out.printf("%s\n",labelInfo[result[i]]+1);
	            }
	            //if((i+1)<len)putchar(' ');//在分词位置输出空格
	        }
	    }
	    return 1;
	};

	
	public int get_seg_result(SegmentedSentence segged) {
		segged.clear();
		for(int i=0;i<len;i++){
			if((i==0)||(labelInfo[result[i]].charAt(0)=='0')||(labelInfo[result[i]].charAt(0)=='3')){
				segged.add("");
			}
			String tmp = segged.lastElement() + sequence.charAt(i);
			segged.remove(segged.size()-1);
			segged.add(tmp);
	    }
		return 1;
	}
	
	public void cs()
	{
		for(int j=0;j<1000;j++)
		{
			System.out.print(dat.dat.get(j).check);
			System.out.print(" ");
		}
	}
}
