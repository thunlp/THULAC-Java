package org.thunlp.thulac.preprocess;

import org.thunlp.thulac.data.POCGraph;

/**
 *
 */
public interface IPreprocessPass {
	String process(String raw, POCGraph graph);
}
