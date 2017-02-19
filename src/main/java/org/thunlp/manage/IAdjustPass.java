package org.thunlp.manage;

import org.thunlp.base.TaggedWord;

import java.util.List;

/**
 *
 */
public interface IAdjustPass {
	void adjust(List<TaggedWord> sentence);
}
