package GamGo;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * class for processing query
 */
public class QueryProcessor implements QueryProcessorInterface {
	/**
	 * sorted map to store results
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results;
	/**
	 * the inverted index
	 */
	private final InvertedIndex index;
	/**
	 * stemmer to use
	 */
	private final Stemmer stemmer;

	/**
	 * initialize a new instance of inverted index
	 * 
	 * @param index inverted index
	 */
	public QueryProcessor(InvertedIndex index) {
		this.results = new TreeMap<>();
		this.index = index;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}

	@Override
	/**
	 * Returns an unmodifiable sorted set of queries
	 *
	 * @return an unmodifiable view of the sorted set of queries
	 */
	public SortedSet<String> getQueries() {
		return Collections.unmodifiableSortedSet(results.navigableKeySet());
	}

	@Override
	/**
	 * Processes the given query to form a key that matches the format used in the
	 * results map. It then retrieves and returns the corresponding search results.
	 *
	 * @param query the query
	 * @return a list of search results for query. if there are no results, an empty
	 *         list is returned
	 */
	public List<InvertedIndex.SearchResult> getResults(String query) {
		String processedQuery = processLine(query, stemmer);
		List<InvertedIndex.SearchResult> queryResults = results.get(processedQuery);
		if (queryResults != null) {
			return Collections.unmodifiableList(queryResults);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	/**
	 * write the search results to path
	 * 
	 * @param path path to use
	 * @throws IOException thrown exception
	 */
	public void writeResults(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			JsonWriter.newNested(this.results, writer, 0);
		}
	}

	@Override
	/**
	 * Process a single query line
	 * 
	 * @param line    the query line to process
	 * @param partial boolean indicating what search to use
	 */
	public void processQuery(String line, boolean partial) {
		if (line == null || line.isBlank()) {
			return;
		}
		TreeSet<String> words = FileStemmer.uniqueStems(line, stemmer);
		String processedQuery = String.join(" ", words);
		if (!results.containsKey(processedQuery)) {
			ArrayList<InvertedIndex.SearchResult> searchResult;
			searchResult = index.search(words, partial);
			results.put(processedQuery, searchResult);
		}
	}

	/**
	 * to string method for results
	 */
	@Override
	public String toString() {
		return results.toString();
	}

}