package org.thunlp.thulac.data;

import org.thunlp.thulac.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 *
 */
public class Dat {
	/**
	 * An int array is used here to avoid the performance lost of endlessly calling
	 * methods in {@link java.util.Vector}. {@link Dat} does not manage the size of this
	 * array, access out of the bounds of {@code dat} will throw an
	 * {@link ArrayIndexOutOfBoundsException}, while applications should extend the
	 * size of this array by itself if they wish to add new elements.
	 */
	public int[] dat;
	public int datSize;

	protected Dat(int size) {
		this.dat = new int[size << 1];
		this.datSize = size;
	}

	public Dat(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		FileChannel channel = fis.getChannel();
		ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024)
				.order(ByteOrder.LITTLE_ENDIAN);
		bb.clear();
		this.datSize = (int) (channel.size() >> 3);
		this.dat = new int[this.datSize << 1];
		int len, offset = 0;
		while ((len = channel.read(bb)) != -1) {
			bb.flip();
			bb.asIntBuffer().get(this.dat, offset, len >> 2);
			offset += len >> 2;
			bb.clear();
		}
		channel.close();
	}

	public int match(String word) {
		int ind = 0;
		int base = 0;
		int[] codePoints = StringUtil.toCodePoints(word);
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

	public int getDatSize() {
		return this.datSize;
	}

	public int[] getDat() {
		return this.dat;
	}
}
