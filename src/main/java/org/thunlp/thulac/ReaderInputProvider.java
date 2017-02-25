package org.thunlp.thulac;

import org.thunlp.thulac.util.InputProviderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class ReaderInputProvider implements IInputProvider {
	private BufferedReader reader;

	public ReaderInputProvider(BufferedReader reader) {
		if (reader == null) throw new IllegalArgumentException("reader == null!");
		this.reader = reader;
	}

	@Override
	public List<String> provideInput() throws IOException {
		String line = this.reader.readLine();
		if (line == null) return null;
		return InputProviderUtils.getLineSegments(line);
	}

	@Override
	public void onProgramStart() {
	}

	@Override
	public void onProgramEnd() {
		try {
			this.reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
