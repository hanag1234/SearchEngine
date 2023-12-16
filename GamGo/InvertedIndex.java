package GamGo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * InvertedIndex class
 */
public class InvertedIndex {
	/**
	 * initialize
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;
	/**
	 * initialize
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * initialize new instance
	 * 
	 */
	public InvertedIndex() {
		this.invertedIndex = new TreeMap<>();
		this.counts = new TreeMap<>();
	}

	/**
	 * Adds entries from another InvertedIndex into this InvertedIndex
	 *
	 * @param otherIndex InvertedIndex where entries will be added to this
	 *                   InvertedIndex //
	 */
	public void addDistinct(InvertedIndex otherIndex) {
		for (var otherEntry : otherIndex.invertedIndex.entrySet()) {
			String otherWord = otherEntry.getKey();
			var otherMap = otherEntry.getValue();
			var thisMap = this.invertedIndex.get(otherWord);
			if (thisMap == null) {
				this.invertedIndex.put(otherWord, new TreeMap<>(otherMap));
			} else {
				for (var otherPathEntry : otherMap.entrySet()) {
					String otherPath = otherPathEntry.getKey();
					TreeSet<Integer> otherPositions = otherPathEntry.getValue();
					if (!thisMap.containsKey(otherPath)) {
						thisMap.put(otherPath, new TreeSet<>(otherPositions));
					} else {
						TreeSet<Integer> existing = thisMap.get(otherPath);
						existing.addAll(otherPositions);
					}
				}
			}
		}
		for (var otherCountEntry : otherIndex.counts.entrySet()) {
			String path = otherCountEntry.getKey();
			int otherCount = otherCountEntry.getValue();
			this.counts.put(path, this.counts.getOrDefault(path, 0) + otherCount);
		}
	}

	/**
	 * adds entry to the inverted index
	 * 
	 * @param word     string word to use
	 * @param filePath path to use
	 * @param place    integer place
	 */
	public void addIndex(String word, String filePath, int place) {
		invertedIndex.putIfAbsent(word, new TreeMap<>());
		invertedIndex.get(word).putIfAbsent(filePath, new TreeSet<>());
		if (invertedIndex.get(word).get(filePath).add(place)) {
			this.counts.putIfAbsent(filePath, 0);
			this.counts.put(filePath, this.counts.get(filePath) + 1);
		}
	}

	/**
	 * to string method
	 */
	@Override
	public String toString() {
		return invertedIndex.toString();
	}

	/**
	 * adds list of words to inverted index with given location and starting
	 * position
	 * 
	 * @param words    words to add
	 * @param location location of word found
	 * @param start    starting position
	 */
	public void addAll(List<String> words, String location, int start) {
		for (String word : words) {
			addIndex(word, location, start);
		}
	}

	/**
	 * checks if there is a count for given location
	 * 
	 * @param location location to check
	 * @return true if count has a location
	 */
	public boolean hasCount(String location) {
		return counts.containsKey(location);
	}

	/**
	 * checks if inverted index contains a entry for given word
	 * 
	 * @param word word to use
	 * @return true if word is present
	 */
	public boolean hasWord(String word) {
		return invertedIndex.containsKey(word);
	}

	/**
	 * checks if inverted index has entry for given word and location
	 * 
	 * @param word     word to check
	 * @param location location to check
	 * @return true if location and word are present
	 */
	public boolean hasLocation(String word, String location) {
		return invertedIndex.containsKey(word) && invertedIndex.get(word).containsKey(location);
	}

	/**
	 * check if inverted index has specified position for given word and location
	 * 
	 * @param word     word to check
	 * @param location location to check
	 * @param position position to check
	 * @return true if all are present
	 */
	public boolean hasPosition(String word, String location, int position) {
		return hasLocation(word, location) && invertedIndex.get(word).get(location).contains(position);
	}

	/**
	 * write the inverted index for specified file path in Json format
	 * 
	 * @param path path to use
	 * @throws IOException exception
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.nested(this.invertedIndex, path);
	}

	/**
	 * write counts map to file path in Json format
	 * 
	 * @param path path to use
	 * @throws IOException exception thrown
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeObject(this.counts, path);
	}

	/**
	 * Returns number of words in inverted index
	 * 
	 * @return the number of words
	 */
	public int numWords() {
		return getWords().size();
	}

	/**
	 * returns the word counts in inverted index
	 * 
	 * @return the counts
	 */
	public int numCounts() {
		return getCounts().size();
	}

	/**
	 * return the number of positions where an inputed word appears in specified
	 * location
	 * 
	 * @param word     word to find
	 * @param location location to look
	 * @return positions number of positions
	 */
	public int numPositions(String word, String location) {
		return getPositions(word, location).size();
	}

	/**
	 * return the number of paths where an inputed word appears
	 * 
	 * @param word word to find
	 * @return int paths
	 */
	public int numPaths(String word) {
		return getPaths(word).size();
	}

	/**
	 * return unmodifiable sorted set of words in inverted index
	 * 
	 * @return unmodifiable sorted set
	 */
	public SortedSet<String> getWords() {
		return Collections.unmodifiableSortedSet(invertedIndex.navigableKeySet());
	}

	/**
	 * return an unmodifiable sorted set of paths where a given word appears in
	 * inverted index
	 * 
	 * @param word word to look
	 * @return unmodifiable sorted set
	 */
	public SortedSet<String> getPaths(String word) {
		if (hasWord(word)) {
			return Collections.unmodifiableSortedSet(invertedIndex.get(word).navigableKeySet());
		} else {
			return Collections.emptySortedSet();
		}
	}

	/**
	 * return an unmodifiable sorted set of positions where a word is given with a
	 * location
	 * 
	 * @param word     word to look
	 * @param location place to look
	 * @return unmodifiable sorted set
	 */
	public SortedSet<Integer> getPositions(String word, String location) {
		if (hasLocation(word, location)) {
			return Collections.unmodifiableSortedSet(invertedIndex.get(word).get(location));
		}
		return Collections.emptySortedSet();
	}

	/**
	 * returns unmodifiable sorted map of the counts
	 * 
	 * @return unmodifiable sorted map
	 */
	public SortedMap<String, Integer> getCounts() {
		return Collections.unmodifiableSortedMap(counts);
	}

	/**
	 * get sorted set of locations where the word appears in the inverted index
	 * 
	 * @param word word to find
	 * @return sorted set of locations
	 */
	public SortedSet<String> getLocation(String word) {
		TreeMap<String, TreeSet<Integer>> location;
		location = this.invertedIndex.get(word);
		if (location != null) {
			return Collections.unmodifiableSortedSet(location.navigableKeySet());
		} else {
			return Collections.emptySortedSet();
		}
	}

	/**
	 * A convenience method to choose between exact and partial search
	 *
	 * @param queries The set of queries
	 * @param partial If true does partial search. If false does exact search
	 * @return The search results as a list.
	 */
	public ArrayList<SearchResult> search(Set<String> queries, boolean partial) {
		return partial ? searchPartial(queries) : exactSearch(queries);
	}

	/**
	 * does exact search on given query words set
	 * 
	 * @param queries set of query words
	 * @return set of search results matching query words
	 */
	public ArrayList<SearchResult> exactSearch(Set<String> queries) {
		ArrayList<SearchResult> searchResults = new ArrayList<>();
		Map<String, SearchResult> lookup = new HashMap<>();
		for (String query : queries) {
			processWord(query, lookup, searchResults);
		}
		Collections.sort(searchResults);
		return searchResults;
	}

	/**
	 * does partial search on given set of words
	 * 
	 * @param words words to search
	 * @return list of search results
	 */
	public ArrayList<SearchResult> searchPartial(Set<String> words) {
		ArrayList<SearchResult> searchResult = new ArrayList<>();
		Map<String, SearchResult> lookup = new HashMap<>();
		for (String begin : words) {
			SortedMap<String, TreeMap<String, TreeSet<Integer>>> tailMap = invertedIndex.tailMap(begin);
			for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> entry : tailMap.entrySet()) {
				String word = entry.getKey();
				if (!word.startsWith(begin)) {
					break;
				}
				processWord(word, lookup, searchResult);
			}
		}
		Collections.sort(searchResult);
		return searchResult;
	}

	/**
	 * Processes the word and updates the search results based on its occurrences in
	 * the inverted index
	 * 
	 * @param word          word to process
	 * @param lookup        map
	 * @param searchResults searchResults list
	 */
	private void processWord(String word, Map<String, SearchResult> lookup, List<SearchResult> searchResults) {
		TreeMap<String, TreeSet<Integer>> locations = invertedIndex.get(word);
		if (locations == null)
			return;
		for (Map.Entry<String, TreeSet<Integer>> locationEntry : locations.entrySet()) {
			String location = locationEntry.getKey();
			if (lookup.containsKey(location)) {
				SearchResult oldResult = lookup.get(location);
				oldResult.update(locationEntry.getValue().size());
			} else {
				SearchResult result = new SearchResult(location);
				result.update(locationEntry.getValue().size());
				searchResults.add(result);
				lookup.put(location, result);
			}
		}
	}

	/**
	 * Nested class that provides methods to get and set as well as a method to
	 * compare two 'SearchResult' objects
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/**
		 * initialize
		 * 
		 */
		private int count;
		/**
		 * initialize
		 * 
		 */
		private double score;
		/**
		 * initialize
		 * 
		 */
		private final String where;

		/**
		 * constructor
		 * 
		 * @param where location where search query method
		 */
		public SearchResult(String where) {
			this.count = 0;
			this.where = where;
			this.score = (double) counts.get(where);
		}

		/**
		 * get the counts
		 * 
		 * @return count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * get the score
		 * 
		 * @return score
		 */
		public double getScore() {
			return score;
		}

		/**
		 * get the location
		 * 
		 * @return where
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * Update count and recalculates score based on new count and where occurrences
		 *
		 * @param amount amount to add
		 */
		private void update(int amount) {
			this.count += amount;
			this.score = (double) count / counts.get(where);
		}

		/**
		 * compares searchResult
		 * 
		 * @return A negative if this SearchResult is less than SearchResult, a positive
		 *         if it is greater, and zero if they are equal
		 */
		@Override
		public int compareTo(SearchResult other) {
			int scoreComparison = Double.compare(other.getScore(), this.getScore());
			if (scoreComparison != 0) {
				return scoreComparison;
			}
			int countComparison = Integer.compare(other.getCount(), this.getCount());
			if (countComparison != 0) {
				return countComparison;
			}
			return this.getWhere().compareToIgnoreCase(other.getWhere());
		}
	}
}
