package GamGo;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
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
 * ThreadSafeQueryProcessor extends QueryProcessor with thread-safe operations
 * and concurrent task execution using WorkQueue
 */
public class ThreadSafeQueryProcessor implements QueryProcessorInterface {
	/**
	 * initialize
	 */
	private final ThreadedInvertedIndex index;
	/**
	 * initialize
	 */
	private final TreeMap<String, ArrayList<ThreadedInvertedIndex.SearchResult>> results;
	/**
	 * initialize
	 */
	private final WorkQueue workqueue;
	/**
	 * initialize
	 */
	private final Stemmer stemmer;

	/**
	 * Constructor with threaded inverted index
	 * 
	 * @param index     threaded inverted index instance
	 * @param workqueue workqueue to use
	 */
	public ThreadSafeQueryProcessor(ThreadedInvertedIndex index, WorkQueue workqueue) {
		this.results = new TreeMap<>();
		this.index = index;
		this.workqueue = workqueue;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}

	/**
	 * returns sorted set of queries
	 */
	@Override
	public SortedSet<String> getQueries() {
		synchronized (this) {
			return Collections.unmodifiableSortedSet(results.navigableKeySet());
		}
	}

	/**
	 * gets the search results
	 */
	@Override
	public List<ThreadedInvertedIndex.SearchResult> getResults(String query) {
		String processedQuery = processLine(query, stemmer);
		synchronized (this) {
			ArrayList<ThreadedInvertedIndex.SearchResult> searchResults = results.get(processedQuery);
			if (searchResults != null) {
				return Collections.unmodifiableList(searchResults);
			} else {
				return Collections.emptyList();
			}
		}
	}

	/**
	 * writes the list of search results
	 */
	@Override
	public void writeResults(Path path) throws IOException {
		synchronized (this) {
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				JsonWriter.newNested(this.results, writer, 0);
			}
		}
	}

	/**
	 * process queries from path
	 * 
	 * @param path    path to use
	 * @param partial boolean for search method
	 * @throws IOException thrown exception
	 */
	@Override
	public void processQuery(Path path, boolean partial) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			while (reader.ready()) {
				String line = reader.readLine();
				workqueue.execute(new Task(line, partial));
			}
		}
		workqueue.finish();
	}

	/**
	 * processes single query line
	 */
	@Override
	public void processQuery(String line, boolean partial) {
		if (line == null || line.isBlank()) {
			return;
		}
		TreeSet<String> words = FileStemmer.uniqueStems(line);
		String processedQuery = String.join(" ", words);

		synchronized (this) {
			if (!results.containsKey(processedQuery)) {
				ArrayList<ThreadedInvertedIndex.SearchResult> searchResult = index.search(words, partial);
				results.put(processedQuery, searchResult);
			}
		}
	}

	/**
	 * to string method
	 */
	@Override
	public String toString() {
		synchronized (this) {
			return super.toString();
		}
	}

	/**
	 * nested class task for processing query line
	 */
	private class Task implements Runnable {
		/**
		 * initialize
		 */
		private final String line;
		/**
		 * initialize
		 */
		private final boolean partial;

		/**
		 * constructor
		 * 
		 * @param line    line to process
		 * @param partial true or false to what search to call
		 */
		public Task(String line, boolean partial) {
			this.line = line;
			this.partial = partial;
		}

		/**
		 * run the task
		 */
		@Override
		public void run() {
			processQuery(line, partial);
		}
	}
}
