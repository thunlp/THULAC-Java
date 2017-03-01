package org.thunlp.thulac;

import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class Tests {
	// The input and compare files used in these tests are not contained in the repo
	// due to copyright issues, users should provide their own files to be used in tests.

	@Test
	public void test1() throws IOException {
		TestHelper.run(TestHelper.resourceAt("input_1.txt"),
				TestHelper.tempAt("output_1.txt"), false);
	}


	@Test
	public void test2() throws IOException {
		TestHelper.testSuite(TestHelper.resourceAt("input_2.txt"),
				TestHelper.resourceAt("compare_2.txt"),
				TestHelper.tempAt("output_2.txt"));
	}

	@Test
	public void test3() throws IOException {
		// non-Chinese users may see the following line rendered strangely,
		// nevertheless it is only a simple Chinese sentence.
		System.out.println(Thulac.split("今天，中国人民站起来了！", true));
	}
}
