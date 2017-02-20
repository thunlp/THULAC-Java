/**
 * Created锛�May 15, 2013 6:52:50 PM
 * Project锛�ThulacJava
 *
 * @author cxx
 * @since JDK 1.6.0_13
 * filename锛�CBTaggingDecoder.java
 * description锛�
 */
package org.thunlp.character;

import org.thunlp.base.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

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

	public CBTaggingDecoder() {
		this.separator = '_';
		this.maxLength = 20000;
		this.len = 0;
		this.sequence = new String("");
		this.allowedLabelLists = new int[this.maxLength][];

		this.pocsToTags = null;
		this.nGramFeature = null;
		this.dat = null;
		this.nodes = new Node[this.maxLength];
		this.labelTrans = null;
		this.labelTransPre = null;
		this.labelTransPost = null;
		this.threshold = 0;

		this.allowCom = new int[this.maxLength];
		this.tagSize = 0;
		this.model = null;
		this.alphas = null;
		this.betas = null;
	}

	public void init(String modelFile, String datFile, String labelFile) throws
			IOException {
		this.model = new CBModel(modelFile);

		this.values = new int[this.maxLength * this.model.l_size];
		this.alphas = new AlphaBeta[this.maxLength * this.model.l_size];
		this.betas = new AlphaBeta[this.maxLength * this.model.l_size];
		this.result = new int[this.maxLength * this.model.l_size];

		for (int i = 0; i < this.maxLength; i++) {
			this.nodes[i] = new Node();

			int[] pre = new int[2];
			pre[0] = i - 1;
			pre[1] = -1;
			this.nodes[i].predecessors = pre;

			pre = new int[2];
			pre[0] = i + 1;
			pre[1] = -1;
			this.nodes[i].successors = pre;
		}

		this.dat = new Dat(datFile);
		this.nGramFeature = new CBNGramFeature(this.dat, this.model, this.values);

		this.labelInfo = new String[10000];
		Vector<Vector<Integer>> pocTags = new Vector<>();
		for (int i = 0; i < 16; i++) {
			pocTags.add(new Vector<>());
		}
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(labelFile)));
		String line = "";
		int ind = 0;
		while ((line = in.readLine()) != null) {
			this.labelInfo[ind] = line;
			int segInd = line.charAt(0) - '0';
			for (int j = 0; j < 16; j++) {
				if (((1 << segInd) & j) != 0) {
					pocTags.get(j).add(ind);
				}
			}
			ind++;
		}
		in.close();

		this.pocsToTags = new int[16][];
		for (int j = 1; j < 16; j++) {
			this.pocsToTags[j] = new int[pocTags.get(j).size() + 1];
			for (int k = 0; k < pocTags.get(j).size(); k++) {
				this.pocsToTags[j][k] = pocTags.get(j).get(k);
			}
			this.pocsToTags[j][pocTags.get(j).size()] = -1;
		}

//		for(int j = 1; j < pocsToTags[15].length; j++) {
//			System.out.print(pocsToTags[15][j]+" ");
//		}

		this.labelLookingFor = new int[this.model.l_size][];
		for (int i = 0; i < this.model.l_size; i++) {
			this.labelLookingFor[i] = null;
		}
		for (int i = 0; i < this.model.l_size; i++) {
			if (this.labelInfo[i].charAt(0) == '0' || this.labelInfo[i].charAt(0) == '3')
				continue;
			for (int j = 0; j <= i; j++) {
				if ((this.labelInfo[i].substring(1).equals(
						this.labelInfo[j].substring(1))) && (this.labelInfo[j].charAt(
						0) == '0')) {
					if (this.labelLookingFor[j] == null) {
						this.labelLookingFor[j] = new int[2];
						this.labelLookingFor[j][0] = -1;
						this.labelLookingFor[j][1] = -1;
						this.tagSize++;
					}
					this.labelLookingFor[j][this.labelInfo[i].charAt(0) - '1'] = i;
					break;
				}
			}
		}


		for (int i = 0; i < this.maxLength; i++) {
			this.allowedLabelLists[i] = null;
		}
		this.isGoodChoice = new int[this.maxLength * this.model.l_size];
	}

	public void loadLabelTrans(String fileName) throws IOException {
		FileInputStream in = new FileInputStream(fileName);
		byte[] tempbytes = new byte[4];
		in.read(tempbytes);
		int remainSize = bytesToInt(tempbytes, 0);
		tempbytes = new byte[4 * remainSize];
		in.read(tempbytes);

		this.labelTrans = new int[remainSize];
		int labelSize = 0;
		for (int i = 0; i < remainSize; i++) {
			this.labelTrans[i] = bytesToInt(tempbytes, 4 * i);
			if (this.labelTrans[i] == -1) {
				labelSize++;
			}
		}

		labelSize /= 2;

		this.labelTransPre = new int[labelSize][];
		this.labelTransPost = new int[labelSize][];

		int preInd = 0;
		int ind = 0;
		int i = 0;
		while (i < labelSize) {
			while (this.labelTrans[ind] != -1) {
				ind++;
			}
			ind++;
			this.labelTransPre[i] = new int[ind - preInd];
			for (int j = 0; j < ind - preInd; j++) {
				this.labelTransPre[i][j] = this.labelTrans[preInd + j];
			}
			preInd = ind;
			while (this.labelTrans[ind] != -1) {
				ind++;
			}
			ind++;
			this.labelTransPost[i] = new int[ind - preInd];
			for (int j = 0; j < ind - preInd; j++) {
				this.labelTransPost[i][j] = this.labelTrans[preInd + j];
			}
			i++;
		}
		in.close();
	}

	public static int bytesToInt(byte[] bb, int index) {
		return (int) ((((bb[index + 3] & 0xff) << 24)
				| ((bb[index + 2] & 0xff) << 16)
				| ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}

	public void dp() {
		if (this.allowedLabelLists[0] == null) {
			this.allowedLabelLists[0] = this.pocsToTags[9];
		}
		if (this.allowedLabelLists[this.len - 1] == null) {
			this.allowedLabelLists[this.len - 1] = this.pocsToTags[12];
		}
//		for(int i=0;i<200;i++){
//	        System.out.printf("%d ",model.ll_weights[i]);
//	    }
//		System.out.println();
		this.bestScore = AlphaBeta.dbDecode(this.model.l_size, this.model.ll_weights,
				this.len, this.nodes, this.values, this.alphas, this.result,
				this.labelTransPre, this.allowedLabelLists);
		this.allowedLabelLists[0] = null;
		this.allowedLabelLists[this.len - 1] = null;
//		for(int i=0;i<len;i++){
//	        System.out.printf("%s\n",labelInfo[result[i]]);
//	    }
	}

	public void setLabelTrans() {
		int lSize = this.model.l_size;
		Vector<Vector<Integer>> preLabels = new Vector<>();
		Vector<Vector<Integer>> postLabels = new Vector<>();
		for (int i = 0; i < lSize; i++) {
			preLabels.add(new Vector<>());
			postLabels.add(new Vector<>());
		}
		for (int i = 0; i < lSize; i++) {
			for (int j = 0; j < lSize; j++) {
				int ni = this.labelInfo[i].charAt(0) - '0';
				int nj = this.labelInfo[j].charAt(0) - '0';
				boolean iIsEnd = ((ni == 2) || (ni == 3));
				boolean jIsBegin = ((nj == 0) || (nj == 3));
				boolean sameTag = this.labelInfo[i].substring(1)
						.equals(this.labelInfo[j].substring(1));
				if (sameTag) {
					if ((ni == 0 && nj == 1) ||
							(ni == 0 && nj == 2) ||
							(ni == 1 && nj == 2) ||
							(ni == 1 && nj == 1) ||
							(ni == 2 && nj == 0) ||
							(ni == 2 && nj == 3) ||
							(ni == 3 && nj == 3) ||
							(ni == 3 && nj == 0)) {
						preLabels.get(j).add(i);
						postLabels.get(i).add(j);
					}
				} else {
					if (iIsEnd && jIsBegin) {
						preLabels.get(j).add(i);
						postLabels.get(i).add(j);
					}
				}
			}
		}
		this.labelTransPre = new int[lSize][];
//		System.out.println(preLabels.get(0));
		for (int i = 0; i < lSize; i++) {
			this.labelTransPre[i] = new int[preLabels.get(i).size() + 1];
			for (int j = 0; j < preLabels.get(i).size(); j++) {
				this.labelTransPre[i][j] = preLabels.get(i).get(j);
			}
			this.labelTransPre[i][preLabels.get(i).size()] = -1;
		}

		this.labelTransPost = new int[lSize][];
		for (int i = 0; i < lSize; i++) {
			this.labelTransPost[i] = new int[postLabels.get(i).size() + 1];
			for (int j = 0; j < postLabels.get(i).size(); j++) {
				this.labelTransPost[i][j] = postLabels.get(i).get(j);
			}
			this.labelTransPost[i][postLabels.get(i).size()] = -1;
		}
	}

	public void putValues() {
		if (this.len == 0) return;
		for (int i = 0; i < this.len; i++) {
			this.nodes[i].type = 0;
		}
		this.nodes[0].type += 1;
		this.nodes[this.len - 1].type += 2;

		int size = this.len * this.model.l_size;
		for (int i = 0; i < size; i++) {
			this.values[i] = 0;
		}
		this.nGramFeature.putValues(this.sequence, this.len);
//		for(int i=0;i<size;i++) System.out.println(values[i]);
	}

	public int segment(String raw, POCGraph graph, List<TaggedWord> ts) {
		if (raw.length() == 0) return 0;

		for (int i = 0; i < (int) raw.length(); i++) {
			int pocs = graph.get(i);
			if (pocs != 0) {
				this.allowedLabelLists[i] = this.pocsToTags[pocs];
			} else {
				this.allowedLabelLists[i] = this.pocsToTags[15];
			}
		}
		//std::cout<<"\n";
		this.sequence = "";
		for (int i = 0; i < (int) raw.length(); i++) {
			this.sequence += raw.charAt(i);
//	    	System.out.println(raw.charAt(i));
		}
		this.len = (int) raw.length();
//	    for(int i=0;i<200;i++){
//	        System.out.printf("%d ",values[i]);
//	    }
//		System.out.println();
		putValues();// calculate eigenvalue and initialize and store them in values
		dp();// DP search for the best answer and store it in result

		for (int i = 0; i < (int) raw.length(); i++) {
			this.allowedLabelLists[i] = null;
		}
		int offset = 0;
		ts.clear();
		for (int i = 0; i < this.len; i++) {
			if ((i == this.len - 1) || (this.labelInfo[this.result[i]].charAt(
					0) == '2') || (this.labelInfo[this.result[i]].charAt(0) == '3')) {
				ts.add(new TaggedWord(this.separator));
				for (int j = offset; j < i + 1; j++) {
					ts.get(ts.size() - 1).word += (this.sequence.charAt(j));
				}
//	            System.out.println(ts.lastElement().word);
				offset = i + 1;
				if ((this.labelInfo[this.result[i]] + 1) != null) { // output tags if any
					ts.get(ts.size() - 1).tag = this.labelInfo[this.result[i]].substring(
							1);
//	                System.out.printf("%s\n",labelInfo[result[i]]+1);
				}
				//if((i+1)<len)putchar(' ');// output a whitespace at the position of
				// the segmentation
			}
		}
		return 1;
	}

	public void cs() {
		for (int j = 0; j < 1000; j++) {
			System.out.print(this.dat.dat.get(j).check);
			System.out.print(" ");
		}
	}
}
