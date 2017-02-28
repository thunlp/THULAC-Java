package org.thunlp.thulac.postprocess;

import org.thunlp.thulac.data.TaggedWord;

import java.util.List;

/**
 *
 */
public interface IPostprocessPass {
	void process(List<TaggedWord> sentence);
}
