package org.thunlp.adjustment;

import org.thunlp.base.TaggedWord;

import java.util.List;

/**
 *
 */
public interface IAdjustPass {
	void adjust(List<TaggedWord> sentence);
}
