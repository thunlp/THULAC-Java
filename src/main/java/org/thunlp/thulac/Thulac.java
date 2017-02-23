package org.thunlp.thulac;

import org.thunlp.adjustment.*;
import org.thunlp.base.POCGraph;
import org.thunlp.base.TaggedWord;
import org.thunlp.character.CBTaggingDecoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Thulac {
	public static void main(String[] args) throws IOException {
		// params
		String modelDir = "models/";
		char separator = '_';
		String userDict = null;

		// flags
		boolean useT2S = false;
		boolean segOnly = false;
		boolean useFilter = false;

		// IO
		Scanner in = null;
		FileChannel out = null;

		// process input args
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
					in = new Scanner(new File(args[++c]), "UTF-8");
					break;
				case "-output":
					out = new FileOutputStream(args[++c]).getChannel();
					break;
			}
		if (in == null) in = new Scanner(System.in);

		try {
			run(modelDir, separator, useT2S, segOnly, useFilter, userDict, in, out);
		} finally {
			in.close();
			out.close();
		}
	}

	public static void run(
			String modelDir, char separator,
			boolean useT2S, boolean segOnly, boolean useFilter,
			String userDict, Scanner in, FileChannel out) throws IOException {
		// segmentation
		POCGraph pocGraph = new POCGraph();
		CBTaggingDecoder cwsTaggingDecoder = new CBTaggingDecoder();
		cwsTaggingDecoder.threshold = segOnly ? 0 : 10000;
		cwsTaggingDecoder.separator = separator;
		String prefix = modelDir + (segOnly ? "cws_" : "model_c_");
		cwsTaggingDecoder.init(prefix + "model.bin", prefix + "dat.bin",
				prefix + "label.txt");
		cwsTaggingDecoder.setLabelTrans();

		// preprocess
		Preprocessor preprocessor = new Preprocessor();
		if (useT2S) preprocessor.loadT2SMap(modelDir + "t2s.dat");

		// adjustment passes
		List<IAdjustPass> passes = new ArrayList<>();
		passes.add(new PostprocessPass(modelDir + "ns.dat", "ns", false)); // nsDict
		passes.add(new PostprocessPass(modelDir + "idiom.dat", "i", false)); // idiomDict
		passes.add(new PunctuationPass(modelDir + "singlepun.dat")); // punctuation
		passes.add(new TimeWordPass()); // time word
		passes.add(new NegWordPass(modelDir + "neg.dat")); // neg word
		if (userDict != null) passes.add(new PostprocessPass(userDict, "uw", true));
		if (useFilter) // filter
			passes.add(new FilterPass(modelDir + "xu.dat", modelDir + "time.dat"));

		// main loop
		for (List<String> vec = getRaw(in); vec != null; vec = getRaw(in)) {
			StringBuilder outBuf = new StringBuilder();
			for (String raw : vec) {
				// preprocess
				raw = preprocessor.cleanup(raw, pocGraph);
				if (useT2S) raw = preprocessor.convertT2S(raw);
				if (raw.isEmpty()) continue;

				// segmentation
				List<TaggedWord> tagged = new Vector<>();
				cwsTaggingDecoder.segment(raw, pocGraph, tagged);

				// adjustment passes
				for (IAdjustPass pass : passes) pass.adjust(tagged);

				for (TaggedWord word : tagged) {
					if (segOnly) outBuf.append(word.word);
					else outBuf.append(word);
					outBuf.append(" ");
				}
			}
			outBuf.append("\n");
			if (out == null) System.out.print(outBuf.toString());
			else {
				ByteBuffer buffer = ByteBuffer.wrap(outBuf.toString().getBytes());
				out.write(buffer);
			}
		}
	}

	private static final int maxLength = 20000;
	private static final Pattern cutoffPattern =
			Pattern.compile(".*([\u3002\uff1f\uff01\uff1b;!?]|$)");

	private static List<String> getRaw(Scanner scanner) {
		if (!scanner.hasNextLine()) return null;
		String line = scanner.nextLine();

		List<String> rawStrings = new Vector<>();
		if (line.length() < maxLength) rawStrings.add(line);
		else { // cut off the line into short strings
			Matcher matcher = cutoffPattern.matcher(line);
			while (matcher.find()) rawStrings.add(matcher.group());
		}

		return rawStrings;
	}
}
