package org.thunlp.thulac.io;

import org.thunlp.thulac.data.TaggedWord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class WriterOutputHandler implements IOutputHandler {
	private BufferedWriter writer;
	private StringBuilder sb;

	public WriterOutputHandler(BufferedWriter writer) {
		if (writer == null) throw new IllegalArgumentException("writer == null!");
		this.writer = writer;
		this.sb = new StringBuilder();
	}

	@Override
	public void handleLineSegment(List<TaggedWord> words, boolean segOnly)
			throws IOException {
		if (segOnly) {
			for (TaggedWord word : words) {
				this.sb.append(word.word);
				this.sb.append(" ");
			}
		} else {
			for (TaggedWord word : words) {
				this.sb.append(word);
				this.sb.append(" ");
			}
		}
	}

	@Override
	public void handleLineStart() throws IOException {
		this.sb.setLength(0);
	}

	@Override
	public void handleLineEnd() throws IOException {
		this.sb.append("\n");
		this.writer.write(this.sb.toString());
	}

	@Override
	public void onProgramStart() {
	}

	@Override
	public void onProgramEnd() {
		try {
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
