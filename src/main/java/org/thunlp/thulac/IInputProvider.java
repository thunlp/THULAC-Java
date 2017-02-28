package org.thunlp.thulac;

import org.thunlp.thulac.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
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
	 * {@link System#in}, using the default charset as the input encoding.
	 *
	 * @return The {@link IInputProvider} created.
	 */
	static IInputProvider createDefault() {
		return new ReaderInputProvider(new BufferedReader(
				new InputStreamReader(System.in))); // use default charset for System.in
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using UTF-8 as file encoding.
	 *
	 * @param filename
	 * 		The name of the file to retrieve input from.
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	static IInputProvider createFromFile(String filename) throws IOException {
		return createFromFile(filename, (Charset) null);
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using a given charset as encoding.
	 *
	 * @param filename
	 * 		The name of the file to retrieve input from.
	 * @param charsetName
	 * 		The optional name of the charset to use, defaulted to "UTF-8".
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 * @throws UnsupportedCharsetException
	 * 		If the charset referred to by the given name is not supported.
	 */
	static IInputProvider createFromFile(String filename, String charsetName)
			throws IOException, UnsupportedCharsetException {
		Charset charset = null;
		if (charsetName != null) charset = Charset.forName(charsetName);
		return createFromFile(filename, charset);
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using a given charset as encoding.
	 *
	 * @param filename
	 * 		The name of the file to retrieve input from.
	 * @param charset
	 * 		The optional file encoding to use, defaulted to UTF-8.
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	static IInputProvider createFromFile(String filename, Charset charset)
			throws IOException {
		if (filename == null) return null;
		// Files.newBufferedReader throws NPE is charset is null, default it to UTF-8
		if (charset == null) charset = StandardCharsets.UTF_8;
		return new ReaderInputProvider(
				Files.newBufferedReader(Paths.get(filename), charset));
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given {@link String}.
	 *
	 * @param input
	 * 		The input string.
	 *
	 * @return The {@link IInputProvider} created.
	 */
	static IInputProvider createFromString(String input) {
		if (input == null) return null;
		return new ReaderInputProvider(
				new BufferedReader(StringUtils.toReader(input, null)));
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
