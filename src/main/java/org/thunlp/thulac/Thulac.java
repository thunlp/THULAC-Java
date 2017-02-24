package org.thunlp.thulac;

import org.thunlp.adjustment.*;
import org.thunlp.base.POCGraph;
import org.thunlp.base.TaggedWord;
import org.thunlp.character.CBTaggingDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Thulac {
	/**
	 * Run the segmentation program with argument {@code segOnly} and default values
	 * for all others.
	 *
	 * @param input
	 * 		The {@link IInputProvider} instance to provide input.
	 * @param output
	 * 		The {@link IOutputHandler} instance to handle output.
	 * @param segOnly
	 * 		Whether to output only segments.
	 *
	 * @throws IOException
	 * 		If I/O of either {@code input}, {@code output} or one of the model files
	 * 		resulted in an exception.
	 */
	public static void split(IInputProvider input, IOutputHandler output, boolean segOnly)
			throws IOException {
		split("models/", '_', null, false, segOnly, false, input, output);
	}

	/**
	 * Run the segmentation program with full arguments.
	 *
	 * @param modelDir
	 * 		The directory under which the model files are located.
	 * @param separator
	 * 		The separator to use to separate words and tags.
	 * @param userDict
	 * 		The optional file name of the user-specified dictionary.
	 * @param useT2S
	 * 		Whether to transfer traditional Chinese to simplified Chinese before
	 * 		segmentation.
	 * @param segOnly
	 * 		Whether to output only segments.
	 * @param useFilter
	 * 		Whether to use filters while processing.
	 * @param input
	 * 		The {@link IInputProvider} instance to provide input.
	 * @param output
	 * 		The {@link IOutputHandler} instance to handle output.
	 *
	 * @throws IOException
	 * 		If I/O of either {@code input}, {@code output} or one of the model files
	 * 		resulted in an exception.
	 */
	public static void split(
			String modelDir, char separator, String userDict,
			boolean useT2S, boolean segOnly, boolean useFilter,
			IInputProvider input, IOutputHandler output) throws IOException {
		try {
			input.onProgramStart();
			output.onProgramStart();

			// segmentation
			POCGraph pocGraph = new POCGraph();
			CBTaggingDecoder cwsTaggingDecoder = new CBTaggingDecoder();
			cwsTaggingDecoder.threshold = segOnly ? 0 : 10000;
			cwsTaggingDecoder.separator = separator;
			String prefix = modelDir + (segOnly ? "cws_" : "model_c_");
			cwsTaggingDecoder.init(prefix + "model.bin", prefix + "dat.bin",
					prefix + "label.txt");
			cwsTaggingDecoder.setLabelTrans();

			// preprocessor
			Preprocessor preprocessor = new Preprocessor();
			if (useT2S) preprocessor.loadT2SMap(modelDir + "t2s.dat");

			// adjustment passes
			List<IAdjustPass> passes = new ArrayList<>();
			passes.add(new PostprocessPass(modelDir + "ns.dat", "ns", false));
			passes.add(new PostprocessPass(modelDir + "idiom.dat", "i", false));
			passes.add(new PunctuationPass(modelDir + "singlepun.dat"));
			passes.add(new TimeWordPass());
			passes.add(new NegWordPass(modelDir + "neg.dat"));
			if (userDict != null) passes.add(new PostprocessPass(userDict, "uw", true));
			if (useFilter) // filter
				passes.add(new FilterPass(modelDir + "xu.dat", modelDir + "time.dat"));

			// main loop
			for (List<String> vec = input.provideInput(); vec != null; vec = input.provideInput()) {
				output.handleLineStart();
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

					output.handleLineSegment(tagged, segOnly);
				}
				output.handleLineEnd();
			}
		} finally {
			input.onProgramEnd();
			output.onProgramEnd();
		}
	}
}
