package org.thunlp.thulac;

import org.thunlp.base.POCGraph;
import org.thunlp.base.SegmentedSentence;
import org.thunlp.base.TaggedSentence;
import org.thunlp.base.WordWithTag;
import org.thunlp.character.CBTaggingDecoder;
import org.thunlp.manage.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Thulac {
	public static void main(String[] args) throws IOException {
		String user_specified_dict_name = null;
		String model_path_char = null;

		Character separator = '_';

		boolean useT2S = false;
		boolean seg_only = false;
		boolean useFilter = false;
		int maxLength = 20000;
		String input_file = "";
		String output_file = "";

		int c = 0;
		while (c < args.length) {
			String arg = args[c];
			switch (arg) {
				case "-t2s":
					useT2S = true;
					break;
				case "-user":
					user_specified_dict_name = args[++c];
					break;
				case "-deli":
					separator = args[++c].charAt(0);
					break;
				case "-seg_only":
					seg_only = true;
					break;
				case "-filter":
					useFilter = true;
					break;
				case "-model_dir":
					model_path_char = args[++c];
					break;
				case "-input":
					input_file = args[++c];
					break;
				case "-output":
					output_file = args[++c];
					break;
				default:
					//showhelp();
					return;
			}
			c++;
		}

		String prefix;
		if (model_path_char != null) {
			prefix = model_path_char;
			if (prefix.charAt(prefix.length() - 1) != '/') {
				prefix += "/";
			}
		} else {
			prefix = "models/";
		}

		String oiraw;
		String raw;
		POCGraph poc_cands = new POCGraph();
		TaggedSentence tagged = new TaggedSentence();
		SegmentedSentence segged = new SegmentedSentence();

		CBTaggingDecoder cws_tagging_decoder = new CBTaggingDecoder();
		CBTaggingDecoder tagging_decoder = new CBTaggingDecoder();
		if (seg_only) {
			cws_tagging_decoder.threshold = 0;
			cws_tagging_decoder.separator = separator;
			cws_tagging_decoder.init((prefix + "cws_model.bin"), (prefix + "cws_dat.bin"),
					(prefix + "cws_label.txt"));
			cws_tagging_decoder.setLabelTrans();
		} else {
			tagging_decoder.threshold = 10000;
			tagging_decoder.separator = separator;
			tagging_decoder.init((prefix + "model_c_model.bin"),
					(prefix + "model_c_dat.bin"), (prefix + "model_c_label.txt"));
			tagging_decoder.setLabelTrans();
		}

		Preprocesser preprocesser = new Preprocesser();
		preprocesser.setT2SMap((prefix + "t2s.dat"));
		Postprocesser nsDict = new Postprocesser((prefix + "ns.dat"), "ns", false);
		Postprocesser idiomDict = new Postprocesser((prefix + "idiom.dat"), "i", false);
		Postprocesser userDict = null;
		if (user_specified_dict_name != null) {
			userDict = new Postprocesser(user_specified_dict_name, "uw", true);
		}
		Punctuation punctuation = new Punctuation((prefix + "singlepun.dat"));
		TimeWord timeword = new TimeWord();
		NegWord negword = new NegWord((prefix + "neg.dat"));
		Filter filter = null;
		if (useFilter) {
			filter = new Filter((prefix + "xu.dat"), (prefix + "time.dat"));
		}

		Scanner in;
		if (!Objects.equals(input_file, ""))
			in = new Scanner(new File(input_file), "UTF-8");
		else in = new Scanner(System.in);
		PrintStream out = System.out;
		if (output_file != null && !output_file.isEmpty())
			out = new PrintStream(output_file);

		long startTime = System.currentTimeMillis(); //获取当前时间
		Vector<String> vec;
		while (true) {
			vec = getRaw(in, maxLength);
			if (vec == null) break;
//	    	if(oiraw==null) break;
			for (int i = 0; i < vec.size(); i++) {
				oiraw = vec.get(i);
				if (useT2S) {
					String traw;
					traw = preprocesser.clean(oiraw, poc_cands);
					raw = preprocesser.T2S(traw);
				} else {
					raw = preprocesser.clean(oiraw, poc_cands);
				}
				if (raw.length() > 0) {
					if (seg_only) {
						cws_tagging_decoder.segment(raw, poc_cands, tagged);
						cws_tagging_decoder.get_seg_result(segged);
						nsDict.adjust(segged);
						idiomDict.adjust(segged);
						punctuation.adjust(segged);
						timeword.adjust(segged);
						negword.adjust(segged);
						if (userDict != null) {
							userDict.adjust(segged);
						}
						if (useFilter) {
							filter.adjust(segged);
						}

						for (String seg : segged) {
							out.println(seg);
							out.print(' ');
						}
					} else {
						tagging_decoder.segment(raw, poc_cands, tagged);
						nsDict.adjust(tagged);
						idiomDict.adjust(tagged);
						punctuation.adjust(tagged);
						timeword.adjustDouble(tagged);
						negword.adjust(tagged);
						if (userDict != null) {
							userDict.adjust(tagged);
						}
						if (useFilter) {
							filter.adjust(tagged);
						}

						for (WordWithTag aTagged : tagged) aTagged.print(out);
						out.print(i == vec.size() - 1 ? '\n' : ' ');
					}
				}
			}
		}
		in.close();
		out.close();
		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
	}

	private static Vector<String> getRaw(Scanner scanner, int maxLength) {
		if (!scanner.hasNextLine()) return null;
		String ans = scanner.nextLine();
		Vector<String> ans_vec = new Vector<>();

		if (ans.length() < maxLength) {
			ans_vec.add(ans);
		} else {
			Pattern p = Pattern.compile(".*?[。？！；;!?]");
			Matcher m = p.matcher(ans);
			int num = 0, pos = 0;
			String tmp;
			while (m.find()) {
				tmp = m.group(0);
				if (num + tmp.length() > maxLength) {
					ans_vec.add(ans.substring(pos, pos + num));
					pos += num;
					num = tmp.length();
				} else {
					num += tmp.length();
				}
			}
			if (pos != ans.length()) ans_vec.add(ans.substring(pos));
		}
		return ans_vec;
	}
}
