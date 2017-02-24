package org.thunlp.thulac;

import org.thunlp.thulac.cb.CBTaggingDecoder;
import org.thunlp.thulac.data.POCGraph;
import org.thunlp.thulac.data.TaggedWord;
import org.thunlp.thulac.postprocess.*;
import org.thunlp.thulac.preprocess.IPreprocessPass;
import org.thunlp.thulac.preprocess.PreprocessPass;
import org.thunlp.thulac.preprocess.ConvertT2SPass;

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
			POCGraph graph = new POCGraph();
			CBTaggingDecoder taggingDecoder = new CBTaggingDecoder();
			taggingDecoder.threshold = segOnly ? 0 : 10000;
			taggingDecoder.separator = separator;
			String prefix = modelDir + (segOnly ? "cws_" : "model_c_");
			taggingDecoder.init(prefix + "model.bin", prefix + "dat.bin",
					prefix + "label.txt");
			taggingDecoder.setLabelTrans();

			// preprocess passes
			List<IPreprocessPass> pre = new ArrayList<>();
			pre.add(new PreprocessPass());
			if (useT2S) pre.add(new ConvertT2SPass(modelDir + "t2s.dat"));

			// postprocess passes
			List<IPostprocessPass> post = new ArrayList<>();
			post.add(new PostprocessPass(modelDir + "ns.dat", "ns", false));
			post.add(new PostprocessPass(modelDir + "idiom.dat", "i", false));
			post.add(new PunctuationPass(modelDir + "singlepun.dat"));
			post.add(new TimeWordPass());
			post.add(new NegWordPass(modelDir + "neg.dat"));
			if (userDict != null) post.add(new PostprocessPass(userDict, "uw", true));
			if (useFilter)
				post.add(new FilterPass(modelDir + "xu.dat", modelDir + "time.dat"));

			// main loop
			for (
					List<String> lineSegments = input.provideInput();
					lineSegments != null;
					lineSegments = input.provideInput()) {
				output.handleLineStart();
				for (String raw : lineSegments) {
					for (IPreprocessPass pass : pre) raw = pass.process(raw, graph);

					List<TaggedWord> tagged = new Vector<>();
					taggingDecoder.segment(raw, graph, tagged);

					for (IPostprocessPass pass : post) pass.process(tagged);

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
