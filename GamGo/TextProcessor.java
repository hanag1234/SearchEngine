package GamGo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * class for processing, extracting stems, and building an inverted index
 */
public class TextProcessor {
	/**
	 * used to traverse the directory
	 * 
	 * @param index index to use
	 * @param path  path to use
	 * @throws IOException exception thrown
	 */
	public static void processDirectory(InvertedIndex index, Path path) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path paths : stream) {
				if (Files.isDirectory(paths)) {
					processDirectory(index, paths);
				} else if (isText(paths)) {
					processFile(paths, index);
				}
			}
		}
	}

	/**
	 * Check to see if it is a textfile
	 * 
	 * @param file path object file to be checked
	 * @return true if it is a text or txt
	 */
	public static boolean isText(Path file) {
		String fileName = file.toString().toLowerCase();
		return fileName.endsWith(".txt") || fileName.endsWith(".text");
	}

	/**
	 * process the inverted index with its path
	 * 
	 * @param index     index to use
	 * @param inputPath path to use
	 * @throws IOException exception thrown
	 */
	public static void process(InvertedIndex index, Path inputPath) throws IOException {
		if (Files.isDirectory(inputPath)) {
			processDirectory(index, inputPath);
		} else {
			processFile(inputPath, index);
		}
	}

	/**
	 * process the file, break it into stemmed words, and update inverted index with
	 * positions and file counts
	 * 
	 * @param path  path to use
	 * @param index index to use
	 * @throws IOException exception
	 */
	public static void processFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
			int position = 0;
			String location = path.toString();
			while (reader.ready()) {
				line = reader.readLine();
				for (String stemmedWords : FileStemmer.parse(line)) {
					index.addIndex(stemmer.stem(stemmedWords).toString(), location, ++position);
				}
			}
		}
	}
}