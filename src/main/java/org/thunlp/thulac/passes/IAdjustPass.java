package org.thunlp.thulac.passes;

import org.thunlp.thulac.data.TaggedWord;

import java.util.List;

/**
 *
 */
public interface IAdjustPass {
	void adjust(List<TaggedWord> sentence);
}
