package org.thunlp.thulac.preprocess;

import org.thunlp.thulac.data.POCGraph;
import org.thunlp.thulac.util.StringUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 */
public class ConvertT2SPass implements IPreprocessPass {
	private HashMap<Integer, Integer> t2sMap;

	public ConvertT2SPass(String fileName) throws IOException {
		this.t2sMap = new HashMap<>();
		this.loadT2SMap(fileName);
	}

	private void loadT2SMap(String filename) throws IOException {
		File mapFile = new File(filename);
		int recordCount = (int) (mapFile.length() >> 3);
		DataInputStream input = new DataInputStream(new FileInputStream(mapFile));
		int[] traditional = new int[recordCount];
		for (int i = 0; i < recordCount; ++i) traditional[i] = input.readInt();
		for (int i = 0; i < recordCount; ++i) {
			int simplified = input.readInt();
			this.t2sMap.put(traditional[i], simplified);
		}
		input.close();
	}

	private int getSimplifiedCodePoint(int c) {
		if (this.t2sMap.containsKey(c)) return this.t2sMap.get(c);
		return c;
	}

	private String convertT2S(String sentence) {
		int[] codePoints = StringUtils.toCodePoints(sentence);
		StringBuilder sb = new StringBuilder();
		for (int codePoint : codePoints)
			sb.appendCodePoint(this.getSimplifiedCodePoint(codePoint));
		return sb.toString();
	}

	@Override
	public String process(String raw, POCGraph ignored) {
		return this.convertT2S(raw);
	}
}
