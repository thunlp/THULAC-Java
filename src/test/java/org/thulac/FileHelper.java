package org.thulac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 */
public class FileHelper {
	public static List<String> getLines(String fileName) throws IOException {
		return Files.readAllLines(Paths.get(fileName));
	}
}
