package org.thulac;

import org.junit.Test;

import java.io.IOException;

import static org.thulac.TestHelper.resourceAt;
import static org.thulac.TestHelper.tempAt;
import static org.thulac.TestHelper.testSuite;

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
		testSuite(resourceAt("as_test01.txt"),
				resourceAt("as_test_gold01.txt"),
				tempAt("output_as_test01.txt"));
	}
}
