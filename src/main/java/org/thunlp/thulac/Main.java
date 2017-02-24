package org.thunlp.thulac;

import java.io.IOException;

/**
 *
 */
public class Main {
	public static void main(String[] args) throws IOException {
		String modelDir = "models/";
		char separator = '_';
		String userDict = null;
		boolean useT2S = false;
		boolean segOnly = false;
		boolean useFilter = false;
		IInputProvider input = null;
		IOutputHandler output = null;

		for (int c = 0; c < args.length; ++c)
			switch (args[c]) {
				case "-t2s":
					useT2S = true;
					break;
				case "-user":
					userDict = args[++c];
					break;
				case "-deli":
					separator = args[++c].charAt(0);
					break;
				case "-seg_only":
					segOnly = true;
					break;
				case "-filter":
					useFilter = true;
					break;
				case "-model_dir":
					modelDir = args[++c];
					if (modelDir.charAt(modelDir.length() - 1) != '/')
						modelDir += '/';
					break;
				case "-input":
					input = IInputProvider.createFromFile(args[++c]);
					break;
				case "-output":
					output = IOutputHandler.createFromFile(args[++c]);
					break;
			}
		if (input == null) input = IInputProvider.createDefault();
		if (output == null) output = IOutputHandler.createDefault();

		Thulac.split(modelDir, separator, userDict, useT2S, segOnly, useFilter,
				input, output);
	}
}
