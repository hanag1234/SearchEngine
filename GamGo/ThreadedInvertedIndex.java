package GamGo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * class ThreadedInvertedIndex that has all methods from inverted index but
 * using locks
 */
public class ThreadedInvertedIndex extends InvertedIndex {
	/**
	 * initialize lock
	 */
	private final MultiReaderLock lock;

	/**
	 * initialize new instance
	 * 
	 */
	public ThreadedInvertedIndex() {
		this.lock = new MultiReaderLock();
	}

	/**
	 * adds entry to the inverted index
	 * 
	 * @param word     string word to use
	 * @param filePath path to use
	 * @param place    integer place
	 */
	@Override
	public void addIndex(String word, String filePath, int place) {
		lock.writeLock().lock();
		try {
			super.addIndex(word, filePath, place);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * to string method
	 */
	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * adds list of words to inverted index with given location and starting
	 * position
	 * 
	 * @param words    words to add
	 * @param location location of word found
	 * @param start    starting position
	 */
	@Override
	public void addAll(List<String> words, String location, int start) {
		lock.writeLock().lock();
		try {
			super.addAll(words, location, start);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * checks if there is a count for given location
	 * 
	 * @param location location to check
	 * @return true if count has a location
	 */
	@Override
	public boolean hasCount(String location) {
		lock.readLock().lock();
		try {
			return super.hasCount(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * checks if inverted index contains a entry for given word
	 * 
	 * @param word word to use
	 * @return true if word is present
	 */
	@Override
	public boolean hasWord(String word) {
		lock.readLock().lock();
		try {
			return super.hasWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * checks if inverted index has entry for given word and location
	 * 
	 * @param word     word to check
	 * @param location location to check
	 * @return true if location and word are present
	 */
	@Override
	public boolean hasLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.hasLocation(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * check if inverted index has specified position for given word and location
	 * 
	 * @param word     word to check
	 * @param location location to check
	 * @param position position to check
	 * @return true if all are present
	 */
	@Override
	public boolean hasPosition(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.hasPosition(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * write the inverted index for specified file path in Json format
	 * 
	 * @param path path to use
	 * @throws IOException exception
	 */
	@Override
	public void writeIndex(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeIndex(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * write counts map to file path in Json format
	 * 
	 * @param path path to use
	 * @throws IOException exception thrown
	 */
	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns number of words in inverted index
	 * 
	 * @return the number of words
	 */
	@Override
	public int numWords() {
		lock.readLock().lock();
		try {
			return super.numWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * returns the word counts in inverted index
	 * 
	 * @return the counts
	 */
	@Override
	public int numCounts() {
		lock.readLock().lock();
		try {
			return super.numCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * return the number of positions where an inputed word appears in specified
	 * location
	 * 
	 * @param word     word to find
	 * @param location location to look
	 * @return positions number of positions
	 */
	@Override
	public int numPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.numPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * return the number of paths where an inputed word appears
	 * 
	 * @param word word to find
	 * @return int paths
	 */
	@Override
	public int numPaths(String word) {
		lock.readLock().lock();
		try {
			return super.numPaths(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * return unmodifiable sorted set of words in inverted index
	 * 
	 * @return unmodifiable sorted set
	 */
	@Override
	public SortedSet<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * return an unmodifiable sorted set of paths where a given word appears in
	 * inverted index
	 * 
	 * @param word word to look
	 * @return unmodifiable sorted set
	 */
	@Override
	public SortedSet<String> getPaths(String word) {
		lock.readLock().lock();
		try {
			return super.getPaths(word);
		} finally {
			lock.readLock().unlock();
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
	@Override
	public SortedSet<Integer> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * returns unmodifiable sorted map of the counts
	 * 
	 * @return unmodifiable sorted map
	 */

	@Override
	public SortedMap<String, Integer> getCounts() {
		lock.readLock().lock();
		try {
			return super.getCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * get sorted set of locations where the word appears in the inverted index
	 * 
	 * @param word word to find
	 * @return sorted set of locations
	 */
	@Override
	public SortedSet<String> getLocation(String word) {
		lock.readLock().lock();
		try {
			return super.getLocation(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addDistinct(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addDistinct(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Performs exact search on the set of queries
	 *
	 * @param queries set of queries to perform exact search
	 * @return An arrayList of SearchResult objects
	 */
	@Override
	public ArrayList<SearchResult> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a partial search on the set of words
	 *
	 * @param words set of words
	 * @return An ArrayList of SearchResult objects
	 */
	@Override
	public ArrayList<SearchResult> searchPartial(Set<String> words) {
		lock.readLock().lock();
		try {
			return super.searchPartial(words);
		} finally {
			lock.readLock().unlock();
		}
	}

}
