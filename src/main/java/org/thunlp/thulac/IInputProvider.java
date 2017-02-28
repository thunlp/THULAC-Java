package org.thunlp.thulac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * An interface used to provide input for {@link Thulac}. Implementations of this
 * interface should contain its own context, since {@link #provideInput()} does not
 * pass any kind of parameter. It is recommended that implementations read input from a
 * stream, e.g., from a file of from the console ({@code System.in}).
 */
public interface IInputProvider extends IProgramStateListener {
	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from
	 * {@link System#in}.
	 *
	 * @return The {@link IInputProvider} created.
	 */
	static IInputProvider createDefault() {
		return new ReaderInputProvider(new BufferedReader(
				new InputStreamReader(System.in)));
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file.
	 *
	 * @param filename
	 * 		The name of the file to output to.
	 *
	 * @return The {@link IInputProvider} created.
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	static IInputProvider createFromFile(String filename) throws IOException {
		if (filename == null) return null;
		return new ReaderInputProvider(Files.newBufferedReader(Paths.get(filename)));
	}

	/**
	 * Provide a {@link List} of {@link String} which contains the input for the
	 * segmentation program to process. By contract, the return value of this method,
	 * joined with whitespaces (U+0020) should logically represent a line from the input,
	 * though this is not compulsory. A {@code null} return value will be regarded as
	 * an EOF and the program will terminate. A {@link List} is used because it is
	 * recommended to split an enormous line into separate line segments based on the
	 * punctuations.
	 *
	 * @return The input to the segmentation program.
	 */
	List<String> provideInput() throws IOException;
}
