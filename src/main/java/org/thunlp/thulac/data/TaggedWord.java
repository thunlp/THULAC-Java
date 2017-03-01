package org.thunlp.thulac.data;

/**
 * A class which represent a tagged word, that is, a word with a tag.
 */
public class TaggedWord {
	public String word;
	public String tag;

	/**
	 * The separator between word and tag, used while printing this {@link TaggedWord}.
	 */
	public char separator;

	public TaggedWord(char separator) {
		this.separator = separator;
		this.word = "";
	}

	/**
	 * Converts this {@link TaggedWord} to {@link String}. Format: word + separator + tag.
	 *
	 * @return The converted {@link String}.
	 */
	@Override
	public String toString() {
		return this.word + this.separator + this.tag;
	}
}
