package GamGo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;

/**
 * interface for QueryProcessor and ThreadSafeQueryProcessor
 */
public interface QueryProcessorInterface {
	/**
	 * retrieves sorted set of processed queries
	 * 
	 * @return sorted set of strings
	 */
	SortedSet<String> getQueries();

	/**
	 * retrieves the search results
	 * 
	 * @param query the query string
	 * @return list of search results
	 */
	List<? extends InvertedIndex.SearchResult> getResults(String query);

	/**
	 * writes the results to path
	 * 
	 * @param path path to use
	 * @throws IOException thrown exception
	 */
	void writeResults(Path path) throws IOException;

	/**
	 * Returns a string representation of the query processor
	 *
	 * @return a string
	 */
	@Override
	String toString();

	/**
	 * Processes a given line by stemming its words and then joins them into a
	 * single string
	 * 
	 * @param line    input string with words to process
	 * @param stemmer the stemmer to use for processing
	 * @return A string where each word is stemmed and then joined together with
	 *         spaces
	 */
	default String processLine(String line, Stemmer stemmer) {
		TreeSet<String> words = FileStemmer.uniqueStems(line, stemmer);
		return String.join(" ", words);
	}

	/**
	 * Processes search queries from a given path.
	 * 
	 * @param path    path to use
	 * @param partial boolean telling if should or not partial search
	 * @throws IOException thrown exception
	 */
	default void processQuery(Path path, boolean partial) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			while (reader.ready()) {
				String line = reader.readLine();
				processQuery(line, partial);
			}
		}
	}

	/**
	 * Processes a single query line.
	 * 
	 * @param line    the query line to process
	 * @param partial boolean indicating what search to use
	 */
	void processQuery(String line, boolean partial);
}
