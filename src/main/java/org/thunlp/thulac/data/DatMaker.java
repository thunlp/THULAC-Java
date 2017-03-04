package org.thunlp.thulac.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * A class used to construct instances of {@link Dat} from user-specified dictionary
 * files. It extends {@link Dat} to avoid unnecessary array copies and to increase
 * performance.<br>
 * A confusing algorithm is used to construct the two-array Trie Tree used by
 * {@link Dat}, see in-line comments for more information.
 */
public class DatMaker extends Dat {
	// a record of a word with an id
	private static class Record {
		public String word;
		public int id;

		public Record() {
			this("", 0);
		}

		public Record(String key, int value) {
			this.word = key;
			this.id = value;
		}
	}

	// the comparator used to compare Record pairs comparing their words
	private static Comparator<Record> RECORDS_COMPARATOR =
			new Comparator<Record>() {
				@Override
				public int compare(Record a, Record b) {
					return a.word.compareTo(b.word);
				}
			};

	/**
	 * Read (or more precisely, construct) an instance of {@link Dat} from the given
	 * {@link InputStream}. This is used to generate {@link Dat} from a user-specified
	 * dictionary, which consists of multiple lines, each one representing a word in the
	 * dictionary.
	 *
	 * @param in
	 * 		The {@link InputStream} to read.
	 *
	 * @return The generated {@link Dat}.
	 *
	 * @throws IOException
	 * 		If an I/O error happen.
	 */
	public static Dat readFromInputStream(InputStream in) throws IOException {
		List<String> words = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String str;
		while ((str = reader.readLine()) != null) words.add(str);
		reader.close();

		DatMaker dat = new DatMaker();
		dat.buildDat(words);
		return dat;
	}

	/**
	 * Read (or more precisely, construct) an instance of {@link Dat} from the given
	 * file. This is used to generate {@link Dat} from a user-specified dictionary,
	 * which consists of multiple lines, each one representing a word in the dictionary.
	 *
	 * @param filename
	 * 		The name of the file.
	 *
	 * @return The generated {@link Dat}.
	 *
	 * @throws IOException
	 * 		If the given file does not exist or is not readable.
	 */
	public static Dat readFromTxtFile(String filename) throws IOException {
		return readFromInputStream(new FileInputStream(filename));
	}

	// The idea of the algorithm which generates a Dat instance from the input string
	// might be a bit difficult to grab, therefore let me explain it in a whole before
	// we talk about the details.
	// If you ask me, I would say that this algorithm is pretty complicated however
	// genius. It makes use of the unused space of the original double-array Trie Tree
	// to store a double-linked list, while not having to modify even one line of code
	// in Dat.java. That is, it is fully compatible with the standard double-array Tree
	// Tree data structure. Here, "make use of the unused space" means that this
	// algorithm achieves its goal without having to require extra storage space.
	// (expect for the fields head and tail, which are perfectly O(1) and can be simply
	// ignored)

	// First of all, I find it helpful to first make some definitions.
	// this.dat is the only storage block used by this algorithm, and it is actually an
	// array of ELEMENTS. An ELEMENT contains two values, called BASE and CHECK, and
	// the this.dat, which contains only integers, looks like this:
	// ELEMENTS[0].BASE, ELEMENTS[0].CHECK, ELEMENTS[1].BASE, ELEMENTS[1].CHECK, ...
	// We also define that this.datSize is the total number of ELEMENTS, requiring
	// this.dat.length to be 2 * this.datSize.
	// In the following part in this article, I will refer to BASE and CHECK as the
	// FIELDS of an ELEMENT, for example, "the BASE FIELD of ELEMENT[4]".

	// Now we know that in this.dat two different data structures are stored, there
	// leaves a question that, how do the program (or other programs like Dat.java)
	// distinguish one from the other?
	// The answer is, the sign of the ELEMENTS' FIELDS.
	// ELEMENTS whose CHECK FIELD is positive, belong to the double-array Trie Tree,
	// while those whose CHECK FIELD is negative belong to the double-linked list.
	// We use the term USED to define that an ELEMENT belongs to the Trie Tree, or
	// UNUSED to defined the other situation.

	// After that, we can come to the actual data structured.
	// FIELDS of USED ELEMENTS follow the definitions of the double-array Trie Tree
	// completely. (Here I assume that you have the basic knowledge of the Tree Tree,
	// if not, consult Google) For the current stage S and input character C, we have:
	// ELEMENTS[ELEMENTS[S].BASE + C].CHECK = S
	// ELEMENTS[S].BASE + C = T
	// where T is the next stage the DFA described by the Trie Tree should jump to.

	// How the double-linked list is stored is the trickiest part.
	// We know that in a double-linked list there are multiple NODES, each containing two
	// pointers PREV and NEXT. Hoping that you will be familiar to the c-style arrow (->)
	// operator, the list conforms to the following equations:
	// NODE->NEXT->PREV = NODE
	// NODE->PREV->NEXT = NODE
	// Looking back, we have claimed that UNUSED ELEMENTS belong to the double-linked
	// list and have a negative CHECK FIELD. We also know that in actual
	// implementations the PREV and NEXT pointers are often described as indices
	// pointing to the array where the NODES are stored. What I am going to tell you
	// now is that NODES and ELEMENTS are actually the same, as:
	// ELEMENTS[ -ELEMENTS[i].CHECK ].BASE = i
	// ELEMENTS[ -ELEMENTS[i].BASE ].CHECK = i
	// I suppose that this will express the idea clear enough. Please notice the minus
	// sign in these equations, this means that the BASE FIELD of UNUSED ELEMENTS are
	// also negative. (To find out why isn't this used to distinguish USED and UNUSED
	// ELEMENTS, please read on)

	// But, wait a moment. The double-array Trie Tree have a root ELEMENT, here being
	// ELEMENTS[0], however the double-linked array should also have a HEAD NODE and a
	// TAIL NODE, where are they then? If you have already looked down the code, you
	// might have an faint impression of the fields this.head and this.tail. If I say
	// that I have not been waiting for you to make the OHHHHHH sound, I would be
	// telling a lie. Nevertheless, for those who still haven't understood, don't worry.
	// The value of this.head is always non-positive, because -this.head points to the
	// first NODE in the double-linked list, and the same applies for this.tail,
	// with that -this.tail points to the last NODE.
	// So why isn't BASE used to distinguish them after all? That is because, that the
	// BASE FIELD of first NODE in the list is always 1 instead of something negative.

	// After so many explanations of the data structure, we can finally come to the
	// actual behavior of this algorithm.
	// The buildDat() method takes a list of strings as input and sort them in alphabet
	// order. The next step is to break strings - char sequences - into a tree of
	// characters, as described in the Trie Tree and achieved by findChildren().
	// We know that the Trie Tree is a representation of an DFA, therefore a stage has
	// to be generated for each node in the tree. Such a stage, stored as ELEMENTS,
	// have the BASE and CHECK FIELDS. The CHECK field is assigned when the parent stage
	// of the current one is generated, however there still leaves the BASE FIELD to
	// assign.

	// Implemented in allocate(), the way the algorithm searches for the available
	// BASE FIELD is as follows:
	// 1. Set variable BASE to this.head.
	// 2. Determine whether BASE is available. (If all ELEMENTS[BASE + C] are UNUSED
	//    for every C of the children nodes of the current one.
	// 3. If BASE is available, return BASE, otherwise, set BASE to the next UNUSED
	//    ELEMENT, using the double-linked list.
	// In this process, if no available BASE is found, the size of this.dat is expanded
	// twice, invoking the expandDat() method, which also maintains the
	// double-linked list in the newly allocated ELEMENTS.

	// And after an available BASE has been found for the current stage, markAsUsed()
	// is called with BASE and all BASE + C, updating the double-linked list, after which
	// all ELEMENTS[BASE + C].CHECK are set to S and ELEMENTS[S].BASE is set to BASE.
	// ELEMENTS[S].CHECK is set to S if stage BASE can be the end of a word, or BASE
	// otherwise. This is written in populate().
	// For each word in lexicon, its corresponding leaf node in the Trie Tree will have
	// its BASE field set to another value associated with the word.

	// Finally, method packDat() is invoked to minimize the size of this.dat and reduce
	// memory usage.

	private int head;
	private int tail;

	private DatMaker() {
		super(1);

		// initialize the double-linked list: head = 0, next = 1
		this.dat[0] = 1;
		this.dat[1] = -1;
		this.head = this.tail = 0;
	}

	// mark element as used by modifying the double-linked list
	private void markAsUsed(int index) {
		// -base -> the previous element, -check -> the next element
		int base = this.dat[index << 1], check = this.dat[(index << 1) + 1];

		// if the the next element already used, print an error message
		if (check >= 0) throw new RuntimeException("Cell reused! Index: " + index);

		// maintain the double-linked list
		if (base == 1) this.head = check;
		else this.dat[((-base) << 1) + 1] = check;
		if (check == -this.datSize) this.tail = base;
		else this.dat[(-check) << 1] = base;

		this.dat[(index << 1) + 1] = index; // positive check: element used
	}

	// expand size of this.dat
	private void expandDat() {
		int oldSize = this.datSize;

		// alloc & copy
		this.datSize *= 2;
		int[] newDat = new int[this.dat.length << 1];
		System.arraycopy(this.dat, 0, newDat, 0, this.dat.length);
		this.dat = newDat;

		// expand the double-linked list
		for (int i = 0; i < oldSize; i++) {
			int pos = (oldSize + i) << 1;
			newDat[pos] = -(oldSize + i - 1);
			newDat[pos + 1] = -(oldSize + i + 1);
		}
		this.dat[oldSize << 1] = this.tail;
		this.dat[((-this.tail) << 1) + 1] = -oldSize;
		this.tail = -(oldSize * 2 - 1); // set tail to the last element
	}

	// remove unused elements to save memory
	private void packDat() {
		// calculate minimum size
		int last = this.datSize - 1;
		for (; this.dat[(last << 1) + 1] < 0; --last) ;
		this.datSize = last + 1;

		// truncate this.dat
		int[] newDat = new int[this.datSize << 1];
		System.arraycopy(this.dat, 0, newDat, 0, this.datSize << 1);
		this.dat = newDat;
	}

	// allocate elements according to offsets and return BASE
	private int allocate(List<Integer> offsets) {
		int size = offsets.size();
		int base = -this.head; // initially head of the double-linked list
		while (true) {
			// expand this.dat as needed
			if (base == this.datSize) this.expandDat();
			if (size != 0) {
				// sorted, offsets.get(size - 1) is the greatest
				int requiredSize = base + offsets.get(size - 1);
				while (requiredSize >= this.datSize) this.expandDat();
			}

			boolean available = true; // check availability
			if (this.dat[(base << 1) + 1] >= 0) available = false; // ELEMENTS[BASE] USED
			else {
				// if any ELEMENTS[BASE + C] is USED, available = false
				int i = 0;
				for (; i < size && this.dat[(base + offsets.get(i) << 1) + 1] < 0; i++) ;
				if (i < size) available = false;
			}

			if (available) { // if BASE is available, update double-linked list
				this.markAsUsed(base);
				for (int offset : offsets) this.markAsUsed(base + offset);

				return base;
			}

			// find next BASE to check availability
			int newBase = -this.dat[(base << 1) + 1];
			if (newBase == this.datSize) this.expandDat(); // ensure capacity
			base = newBase;
		}
	}

	// find characters in lexicon which might follow the prefix
	private List<Integer> findChildren(List<Record> lexicon, int start, String prefix) {
		List<Integer> children = new ArrayList<>();
		int length = prefix.length(), currentChild = -1;
		for (int i = start, size = lexicon.size(); i < size; ++i) {
			String word = lexicon.get(i).word;
			if (!word.startsWith(prefix)) return children;
			if (word.length() == length) continue;
			int nextCh = word.charAt(length);
			if (nextCh != currentChild) children.add(currentChild = nextCh);
		}
		return children;
	}

	// populate BASE and CHECK FIELDS of allocated BASE and BASE + C
	// @param isWord Whether the end of a word has been reached.
	private int populate(int check, List<Integer> offsets, boolean isWord) {
		int base = this.allocate(offsets);

		this.dat[base << 1] = 0;
		this.dat[(base << 1) + 1] = isWord ? check : base;

		for (int offset : offsets) { // update Trie Tree
			int pos = base + offset << 1;
			this.dat[pos] = 0;
			this.dat[pos + 1] = check; // ELEMENTS[ELEMENTS[S].BASE + C].CHECK = S
		}
		this.dat[check << 1] = base; // ELEMENTS[CHECK].BASE = BASE

		return base;
	}

	// build the Dat structure with a word list as input
	private void buildDat(List<String> words) {
		// construct lexicon
		Vector<Record> lexicon = new Vector<>();
		lexicon.add(new Record());
		for (int i = 0, size = words.size(); i < size; ++i)
			lexicon.add(new Record(words.get(i), i));
		lexicon.sort(RECORDS_COMPARATOR); // sort input

		// root elements
		this.dat[0] = this.populate(0, this.findChildren(lexicon, 0, ""), true);

		for (int i = 0, size = lexicon.size(); i < size; i++) {
			String word = lexicon.get(i).word;

			int off = this.getInfo(word);
			if (off <= 0) off = word.length(); // if dat already contains word

			// iterate through characters after offset and add new entries
			for (int offset = off; offset <= word.length(); offset++) {
				String prefix = word.substring(0, offset);
				int pBase = -this.getInfo(prefix); // should always be positive
				this.populate(pBase, this.findChildren(lexicon, i, prefix),
						offset == word.length()); // on word end
			}

			off = -this.getInfo(word); // should always be positive
			this.dat[this.dat[off << 1] << 1] = lexicon.get(i).id; // leaf node value
		}

		this.packDat();
	}
}
