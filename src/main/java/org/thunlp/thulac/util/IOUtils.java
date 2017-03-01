package org.thunlp.thulac.util;

import org.thunlp.thulac.io.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class IOUtils {
	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from
	 * {@link System#in}, using the default charset as the input encoding.
	 *
	 * @return The {@link IInputProvider} created.
	 */
	public static IInputProvider inputFromConsole() {
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
	public static IInputProvider inputFromFile(String filename) throws IOException {
		return inputFromFile(filename, (Charset) null);
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using UTF-8 as file encoding.
	 *
	 * @param file
	 * 		The file to retrieve input from.
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	public static IInputProvider inputFromFile(File file) throws IOException {
		return inputFromFile(file, null);
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
	public static IInputProvider inputFromFile(String filename, String charsetName)
			throws IOException, UnsupportedCharsetException {
		Charset charset = null;
		if (charsetName != null) charset = Charset.forName(charsetName);
		return inputFromFile(filename, charset);
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using a given charset as encoding.
	 *
	 * @param filename
	 * 		The file to retrieve input from.
	 * @param charset
	 * 		The optional file encoding to use, defaulted to UTF-8.
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	public static IInputProvider inputFromFile(String filename, Charset charset)
			throws IOException {
		if (filename == null) return null; // new File(null) throws NPE
		return inputFromFile(new File(filename), charset);
	}

	/**
	 * Creates an instance of {@link IInputProvider} which retrieves input from the
	 * given file using a given charset as encoding.
	 *
	 * @param file
	 * 		The name of the file to retrieve input from.
	 * @param charset
	 * 		The optional file encoding to use, defaulted to UTF-8.
	 *
	 * @return The {@link IInputProvider} created.
	 *
	 * @throws IOException
	 * 		Is the file does not exist or is not readable.
	 */
	public static IInputProvider inputFromFile(File file, Charset charset)
			throws IOException {
		if (file == null) return null;
		// Files.newBufferedReader throws NPE is charset is null, default it to UTF-8
		if (charset == null) charset = StandardCharsets.UTF_8;
		return new ReaderInputProvider(
				Files.newBufferedReader(Paths.get(file.toURI()), charset));
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
	public static IInputProvider inputFromString(String input) {
		if (input == null) return null;
		return new ReaderInputProvider(
				new BufferedReader(StringUtils.toReader(input, null)));
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to
	 * {@link System#out}, using the default charset as the output encoding.
	 *
	 * @return The {@link IOutputHandler} created.
	 */
	public static IOutputHandler outputToConsole() {
		return new WriterOutputHandler(new BufferedWriter(
				new OutputStreamWriter(System.out)));
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to the
	 * given file using UTF-8 as file encoding.
	 *
	 * @param filename
	 * 		The name of the file to output to.
	 *
	 * @return The {@link IOutputHandler} created.
	 *
	 * @throws IOException
	 * 		Is the file cannot be created or is not writable.
	 */
	public static IOutputHandler outputToFile(String filename) throws IOException {
		return outputToFile(filename, (Charset) null);
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to the
	 * given file using UTF-8 as file encoding.
	 *
	 * @param file
	 * 		The file to output to.
	 *
	 * @return The {@link IOutputHandler} created.
	 *
	 * @throws IOException
	 * 		Is the file cannot be created or is not writable.
	 */
	public static IOutputHandler outputToFile(File file) throws IOException {
		return outputToFile(file, null);
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to the
	 * given file using a given charset as encoding.
	 *
	 * @param filename
	 * 		The name of the file to output to.
	 * @param charsetName
	 * 		The optional name of the charset to use, defaulted to "UTF-8".
	 *
	 * @return The {@link IOutputHandler} created.
	 *
	 * @throws IOException
	 * 		Is the file cannot be created or is not writable.
	 * @throws UnsupportedCharsetException
	 * 		If the charset referred to by the given name is not supported.
	 */
	public static IOutputHandler outputToFile(String filename, String charsetName)
			throws IOException, UnsupportedCharsetException {
		Charset charset = null;
		if (charsetName != null) charset = Charset.forName(charsetName);
		return outputToFile(filename, charset);
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to the
	 * given file using a given charset as encoding.
	 *
	 * @param filename
	 * 		The name of the file to output to.
	 * @param charset
	 * 		The optional file encoding to use, defaulted to UTF-8.
	 *
	 * @return The {@link IOutputHandler} created.
	 *
	 * @throws IOException
	 * 		Is the file cannot be created or is not writable.
	 */
	public static IOutputHandler outputToFile(String filename, Charset charset)
			throws IOException {
		if (filename == null) return null; // new File(null) throws NPE
		return outputToFile(new File(filename), charset);
	}

	/**
	 * Creates an instance of {@link IOutputHandler} which writes output to the
	 * given file using a given charset as encoding.
	 *
	 * @param file
	 * 		The file to output to.
	 * @param charset
	 * 		The optional file encoding to use, defaulted to UTF-8.
	 *
	 * @return The {@link IOutputHandler} created.
	 *
	 * @throws IOException
	 * 		Is the file cannot be created or is not writable.
	 */
	public static IOutputHandler outputToFile(File file, Charset charset)
			throws IOException {
		if (file == null) return null;
		// Files.newBufferedReader throws NPE is charset is null, default it to UTF-8
		if (charset == null) charset = StandardCharsets.UTF_8;
		return new WriterOutputHandler(
				Files.newBufferedWriter(Paths.get(file.toURI()), charset));
	}

	/**
	 * Creates an instance of {@link StringOutputHandler} which writes output to an
	 * {@link String} in memory.<br>
	 * It is typical to use this method like this:
	 * <pre><code>
	 * StringOutputHandler output = IOUtils.outputToString();
	 * Thulac.split(input, output, segOnly); // or anything else
	 * String outputStr = output.getString();
	 * </code></pre>
	 *
	 * @return The {@link StringOutputHandler} created.
	 */
	public static StringOutputHandler outputToString() {
		return new StringOutputHandler();
	}
}
