package org.thunlp.thulac.preprocess;

import org.thunlp.thulac.data.POCGraph;
import org.thunlp.thulac.util.StringUtils;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * A preprocess pass which convert traditional Chinese characters to simplified ones,
 * used when switch {@code -t2s} exists in the command line.
 */
public class ConvertT2SPass implements IPreprocessPass {
	private final Map<Integer, Integer> t2sMap;

	public ConvertT2SPass(String fileName) throws IOException {
	    // t2s map format: recordCount * DWORD traditional +
		//                 recordCount * DWORD simplified
		// -> 8 * recordCount bytes in total
		try (FileChannel channel = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			// the data is little endian
			IntBuffer b = buffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
			int recordCount = (int) (channel.size() >> 3);
			t2sMap = new HashMap<>(recordCount);
			for (int i = 0; i < recordCount; i++) {
				t2sMap.put(b.get(i), b.get(i + recordCount));
			}
		}
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
