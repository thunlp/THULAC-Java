package org.thunlp.thulac;

import org.thunlp.base.POCGraph;
import org.thunlp.base.TaggedWord;
import org.thunlp.character.CBTaggingDecoder;
import org.thunlp.adjustment.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Thulac {
	public static void main(String[] args) throws IOException {
		// input args
		String prefix = "models/";
		Character separator = '_';
		boolean useT2S = false;
		boolean segOnly = false;
		boolean useFilter = false;
		Scanner in = null;
		PrintStream out = System.out;
		Postprocesser userDict = null;
		for (int c = 0; c < args.length; ++c)
			switch (args[c]) {
				case "-t2s":
					useT2S = true;
					break;
				case "-user":
					userDict = new Postprocesser(args[++c], "uw", true);
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
					prefix = args[++c];
					if (prefix.charAt(prefix.length() - 1) != '/')
						prefix += '/';
					break;
				case "-input":
					in = new Scanner(new File(args[++c]), "UTF-8");
					break;
				case "-output":
					out = new PrintStream(args[++c]);
					break;
			}
		if (in == null) in = new Scanner(System.in);

		POCGraph pocCands = new POCGraph();
		List<TaggedWord> tagged = new Vector<>();

		CBTaggingDecoder cwsTaggingDecoder = new CBTaggingDecoder();
		cwsTaggingDecoder.threshold = segOnly ? 0 : 10000;
		cwsTaggingDecoder.separator = separator;
		if (segOnly)
			cwsTaggingDecoder.init(prefix + "cws_model.bin", prefix + "cws_dat.bin",
					prefix + "cws_label.txt");
		else cwsTaggingDecoder.init(prefix + "model_c_model.bin",
				prefix + "model_c_dat.bin", prefix + "model_c_label.txt");
		cwsTaggingDecoder.setLabelTrans();

		Preprocesser preprocesser = new Preprocesser();
		preprocesser.setT2SMap((prefix + "t2s.dat"));
		Postprocesser nsDict = new Postprocesser((prefix + "ns.dat"), "ns", false);
		Postprocesser idiomDict = new Postprocesser((prefix + "idiom.dat"), "i", false);

		Punctuation punctuation = new Punctuation((prefix + "singlepun.dat"));
		TimeWord timeword = new TimeWord();
		NegWord negword = new NegWord((prefix + "neg.dat"));
		Filter filter = null;
		if (useFilter) {
			filter = new Filter((prefix + "xu.dat"), (prefix + "time.dat"));
		}

		for (
				Vector<String> vec = getRaw(in);
				vec != null;
				vec = getRaw(in)) {
			for (String raw : vec) {
				raw = preprocesser.clean(raw, pocCands);
				if (useT2S) raw = preprocesser.T2S(raw);
				if (raw.isEmpty()) continue;

				cwsTaggingDecoder.segment(raw, pocCands, tagged);
				nsDict.adjust(tagged);
				idiomDict.adjust(tagged);
				punctuation.adjust(tagged);
				timeword.adjustDouble(tagged);
				negword.adjust(tagged);
				if (userDict != null) userDict.adjust(tagged);
				if (useFilter) filter.adjust(tagged);

				for (TaggedWord word : tagged) {
					if (segOnly)
						out.print(word.word);
					else out.print(word);
					out.print(' ');
				}
			}
			out.println();
		}
		in.close();
		out.close();
	}

	private static final int maxLength = 20000;
	private static final Pattern punctuations =
			Pattern.compile(".{0," + (maxLength - 1) + "}([。？！；;!?]|$)");

	private static Vector<String> getRaw(Scanner scanner) {
		if (!scanner.hasNextLine()) return null;
		String line = scanner.nextLine();

		Vector<String> rawStrings = new Vector<>();
		if (line.length() < maxLength)
			rawStrings.add(line);
		else {
			Matcher matcher = punctuations.matcher(line);
			while (matcher.find()) rawStrings.add(matcher.group());
		}
		return rawStrings;
	}
}
