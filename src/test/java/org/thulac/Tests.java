package org.thulac;

import org.junit.Test;
import org.thunlp.thulac.Thulac;

import java.io.IOException;

import static org.thulac.TestHelper.*;

/**
 *
 */
public class Tests {
	@Test
	public void test1() throws IOException {
		testSuite(resourceAt("data_input.txt"),
				resourceAt("data_seg.txt"),
				tempAt("output.txt"));
	}

	@Test
	public void test2() throws IOException {
		run(resourceAt("as_test01.txt"),
				tempAt("output_as_test01.txt"), false);
	}

	@Test
	public void test3() throws IOException {
		System.out.println(Thulac.split("今天，中国人民站起来了！", true));
	}
}
