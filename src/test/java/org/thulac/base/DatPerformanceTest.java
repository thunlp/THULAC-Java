package org.thulac.base;

import org.junit.Test;
import org.thunlp.base.Dat;
import org.thunlp.base.Dat2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class DatPerformanceTest {
	@Test
	public void test() throws IOException {
		String filename = "models/cws_dat3.bin";
		System.out.println("Dat file name: " + filename);
		System.out.printf("Dat file size: %.2fMB\n",
				Files.size(Paths.get(filename)) / 1048576f);

		long time = -System.currentTimeMillis();
		Dat dat = new Dat(filename);
		time += System.currentTimeMillis();
		System.out.printf("Old Dat() consumed %d milliseconds.\n", time);
		System.out.printf("First 10 data: %d, %d, %d, %d, %d, %d, %d, %d, %d, %d\n",
				dat.dat.get(0).base, dat.dat.get(0).check,
				dat.dat.get(1).base, dat.dat.get(1).check,
				dat.dat.get(2).base, dat.dat.get(2).check,
				dat.dat.get(3).base, dat.dat.get(3).check,
				dat.dat.get(4).base, dat.dat.get(4).check);
		dat = null;
		System.gc();

		time = -System.currentTimeMillis();
		Dat2 dat2 = new Dat2(filename);
		time += System.currentTimeMillis();
		System.out.printf("New Dat() consumed %d milliseconds.\n", time);
		System.out.printf("First 10 data: %d, %d, %d, %d, %d, %d, %d, %d, %d, %d\n",
				dat2.dat[0], dat2.dat[1],
				dat2.dat[2], dat2.dat[3],
				dat2.dat[4], dat2.dat[5],
				dat2.dat[6], dat2.dat[7],
				dat2.dat[8], dat2.dat[9]);
	}
}
