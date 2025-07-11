package GamGo;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");
		String separateWith = "\n";
		for (Number element : elements) {
			writer.write(separateWith);
			writeIndent(writer, indent + 1);
			writer.write(element.toString());
			separateWith = ",\n";
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");
		String separateWith = "\n";
		for (Map.Entry<String, ? extends Number> entry : elements.entrySet()) {
			writer.write(separateWith);
			writeIndent(writer, indent + 1);
			writeQuote(entry.getKey(), writer, 0);
			writer.write(": ");
			writer.write(entry.getValue().toString());
			separateWith = ",\n";
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		String separateWith = "\n";
		for (Map.Entry<String, ? extends Collection<? extends Number>> entry : elements.entrySet()) {
			writer.write(separateWith);
			separateWith = ",\n";
			writeIndent(writer, indent + 1);
			writeQuote(entry.getKey(), writer, 0);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("[");
		String separateWith = "\n";
		for (Map<String, ? extends Number> element : elements) {
			writer.write(separateWith);
			separateWith = ",\n";
			writeIndent(writer, indent + 1);
			writeObject(element, writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Encodes a nested map into Json format and writes to specified writer
	 * 
	 * @param elements the nested map
	 * @param writer   writer to write data
	 * @param indent   number of spaces for indentation
	 * @throws IOException error if occurs
	 */

	public static void nested(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		if (elements.isEmpty()) {
			writer.write("\n");
		} else {
			String separateWith = "\n";
			for (var out : elements.entrySet()) {
				writer.write(separateWith);
				writeIndent(writer, indent + 1);
				writeQuote(out.getKey(), writer, 0);
				writer.write(": ");
				writeObjectArrays(out.getValue(), writer, indent + 1);
				separateWith = ",\n";
			}
			writer.write("\n");
		}
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Encodes a nested map into Json format and writes to path
	 * 
	 * @param elements the nested map
	 * @param path     the path to use
	 * @throws IOException error if occurs
	 */
	public static void nested(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			nested(elements, writer, 0);
		}
	}

	/**
	 * initialize
	 */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

	/**
	 * writes nested structure of search results to writer in json format
	 * 
	 * @param results treemap containing nested search results
	 * @param writer  writer to be written
	 * @param indent  spaces for indent
	 * @throws IOException exception thrown
	 */
	public static void newNested(Map<String, ? extends Collection<InvertedIndex.SearchResult>> results, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		String separateWith = "\n";
		for (var entry : results.entrySet()) {
			if (!entry.getKey().isEmpty()) {
				writer.write(separateWith);
				writeIndent(writer, indent + 1);
				writeQuote(entry.getKey(), writer, 0);
				writer.write(": ");
				writeSearchResults(entry.getValue(), writer, indent + 1);
				separateWith = ",\n";
			}
		}
		writer.write(!results.isEmpty() ? "\n" : "");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * writes a list of searchresults to writer in json format
	 * 
	 * @param arrayList contains search results to write
	 * @param writer    where it will be written
	 * @param indent    spaces for indent
	 * @throws IOException exception thrown
	 */
	public static void writeSearchResults(Collection<InvertedIndex.SearchResult> arrayList, Writer writer, int indent)
			throws IOException {
		writer.write("[");
		String separateWith = "\n";
		for (var result : arrayList) {
			writer.write(separateWith);
			separateWith = ",\n";
			writeIndent(writer, indent + 1);
			writeSearchResult(result, writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * writes search result object to a writer in json format
	 * 
	 * @param result searchresult object to be written
	 * @param writer where it will be written
	 * @param indent spaces for indent
	 * @throws IOException exception thrown
	 */
	private static void writeSearchResult(InvertedIndex.SearchResult result, Writer writer, int indent)
			throws IOException {
		writer.write("{\n");
		writeIndent(writer, indent + 1);
		writer.write("\"count\": " + result.getCount() + ",\n");
		writeIndent(writer, indent + 1);
		writer.write("\"score\": " + FORMATTER.format(result.getScore()) + ",\n");
		writeIndent(writer, indent + 1);
		writeQuote("where", writer, 0);
		writer.write(": ");
		writeQuote(result.getWhere(), writer, 0);
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

}
