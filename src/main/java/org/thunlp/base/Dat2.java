package org.thunlp.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 *
 */
public class Dat2 {
	public int[] dat;
	public int datSize;

	public Dat2(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		FileChannel channel = fis.getChannel();
		ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024)
				.order(ByteOrder.LITTLE_ENDIAN);
		bb.clear();
		this.datSize = (int) (channel.size() >> 3);
		this.dat = new int[this.datSize << 1];
		System.out.println("File size: " + channel.size() / 4);
		int len, offset = 0;
		while ((len = channel.read(bb)) != -1) {
			bb.flip();
			bb.asIntBuffer().get(this.dat, offset, len >> 2);
			offset += len >> 2;
			bb.clear();
		}
		channel.close();
	}
}
