package org.thunlp.thulac.data;

import org.thunlp.thulac.util.BufferUtils;
import org.thunlp.thulac.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class which loads data files from disk and provide necessary operations.
 */
public class Dat {
	/**
	 * An int array is used here to avoid the performance lost of endlessly calling
	 * methods in {@link java.util.Vector}. {@link Dat} does not manage the size of this
	 * array, access out of the bounds of {@code dat} will throw an {@link
	 * ArrayIndexOutOfBoundsException}, while applications should extend the size of this
	 * array itself if they wish to add new elements.<br>
	 * Originally {@code dat} is of type {@code List<Entry>}, where entry contains two
	 * integer fields: {@code base} and {@code check}. For performance reasons, the
	 * original {@code dat} is removed and replaced with the current {@code int[]}. Since
	 * previously {@code dat.size()} should be equal to {@code datSize}, length of {@code
	 * dat} should be greater than or equal to {@code datSize * 2}, or {@code datSize
	 * << 1}.<br>
	 * There a following translations from the original way of accessing data stored in
	 * {@code dat} to the current one.<br>
	 * <ul>
	 * <li>{@code dat.get(n).base -> dat[i << 1]}</li>
	 * <li>{@code dat.get(n).check -> dat[(i << 1) + 1]}</li>
	 * <li>{@code dat.get(n).base = newBase -> dat[i << 1] = newBase}</li>
	 * <li>{@code dat.get(n).check = newCheck -> dat[(i << 1) + 1] = newCheck}</li>
	 * </ul>
	 * To visit all elements in {@code dat}, use:<br>
	 * <pre><code>
	 * for (int i = 0; i < datSize; ++i) {
	 *     int base = dat[i << 1];
	 *     int check = dat[(i << 1) + 1];
	 *     // do something ...
	 * }
	 * </code></pre>
	 */
	public int[] dat;
	/**
	 * Its value should always be less than or equal to {@code dat.length / 2}.
	 */
	public int datSize;

	protected Dat(int size) {
		this.dat = new int[size << 1]; // see javadoc of dat
		this.datSize = size;
	}

	public Dat(String filename) throws IOException {
		SeekableByteChannel channel = Files.newByteChannel(Paths.get(filename));
		// dat file format: DWORD base + DWORD check -> 8 bytes per record
		this.datSize = (int) (channel.size() >> 3);
		this.dat = new int[this.datSize << 1];

		// strange though, dat files are stored little endian
		ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024)
				.order(ByteOrder.LITTLE_ENDIAN);
		bb.clear();
		// return value should always be true, thus ignored
		BufferUtils.readInts(channel, bb, this.dat);
		channel.close();
	}

	public int match(String word) {
		int ind = 0;
		int base = 0;
		int[] codePoints = StringUtils.toCodePoints(word);
		for (int c : codePoints) {
			ind = this.dat[ind << 1] + c;
			if (ind >= this.datSize || this.dat[(ind << 1) + 1] != base) return -1;
			base = ind;
		}
		ind = this.dat[base << 1];
		return ind < this.datSize && this.dat[(ind << 1) + 1] == base ? ind : -1;
	}

	public int getInfo(String prefix) {
		int ind = 0;
		int base = 0;
		for (int i = 0; i < prefix.length(); i++) {
			ind = this.dat[ind << 1] + prefix.charAt(i);
			if (ind >= this.datSize || this.dat[(ind << 1) + 1] != base) return i;
			base = ind;
		}
		return -base;
	}
}
