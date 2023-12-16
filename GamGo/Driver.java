package GamGo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * main driver class
 */
public class Driver {
	/**
	 * main method calls methods from other classes which parses, processes,
	 * generates word counts, and an inverted index based the args
	 * 
	 * @param args command line args
	 */
	public static void main(String[] args) {
		ArgumentParser parser = new ArgumentParser(args);
		InvertedIndex index = null;
		QueryProcessorInterface queryProcessor;
		WorkQueue workqueue = null;
		ThreadedInvertedIndex multithread = new ThreadedInvertedIndex();
		if (parser.hasFlag("-threads") || parser.hasFlag("-html") || parser.hasFlag("-server")) {
			int threadsArg = parser.getInteger("-threads", 5);
			if (threadsArg < 1) {
				threadsArg = 5;
			}
			workqueue = new WorkQueue(threadsArg);
			index = multithread;
			queryProcessor = new ThreadSafeQueryProcessor(multithread, workqueue);
		} else {
			index = new InvertedIndex();
			queryProcessor = new QueryProcessor(index);
		}
		processFlags(parser, index, queryProcessor, workqueue, multithread);
		if (workqueue != null) {
			workqueue.join();
		}
	}

	/**
	 * Processes command line args and executes appropriate actions based on the
	 * specified flags
	 * 
	 * @param parser         the parsed command line arguments
	 * @param index          for text processing and data storage
	 * @param queryProcessor to handle query operations
	 * @param workqueue      for managing multithreading
	 * @param multithread    for the threaded inverted index
	 */
	private static void processFlags(ArgumentParser parser, InvertedIndex index, QueryProcessorInterface queryProcessor,
			WorkQueue workqueue, ThreadedInvertedIndex multithread) {
		WebCrawler webCrawl = null;
		int crawlNum = parser.getInteger("-crawl", 1);
		if (parser.hasFlag("-text")) {
			Path path = parser.getPath("-text");
			try {
				if (workqueue != null) {
					ThreadedTextProcessor.process((ThreadedInvertedIndex) index, path, workqueue);
				} else {
					TextProcessor.process(index, path);
				}
			} catch (NullPointerException | IOException e) {
				System.out.println("Error processing text flag: " + e.getMessage());
			} catch (InterruptedException e) {
				System.out.println("interrupted exception while text flag: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-html")) {
			try {
				String input = parser.getString("-html");
				webCrawl = new WebCrawler(multithread, workqueue, crawlNum, input);
				webCrawl.processHTML();
			} catch (NullPointerException e) {
				System.err.println("Error processing html flag: " + e.getMessage());
			} catch (MalformedURLException e) {
				System.err.println("Malformed URI Exception: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-query")) {
			Path path = parser.getPath("-query");
			try {
				queryProcessor.processQuery(path, parser.hasFlag("-partial"));
			} catch (NullPointerException | IOException e) {
				System.err.println("Error processing query flag: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-counts")) {
			Path path = parser.getPath("-counts", Path.of("counts.json"));
			try {
				index.writeCounts(path);
			} catch (IOException e) {
				System.out.println("Error writing counts: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-index")) {
			Path path = parser.getPath("-index", Path.of("index.json"));
			try {
				index.writeIndex(path);
			} catch (IOException e) {
				System.out.println("Error writing index: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-results")) {
			Path path = parser.getPath("-results", Path.of("results.json"));
			try {
				queryProcessor.writeResults(path);
			} catch (IOException e) {
				System.err.println("Error writing results: " + e.getMessage());
			}
		}
		if (parser.hasFlag("-server")) {
			int serverPort = parser.getInteger("-server", 8080);
			HeaderServer headerServer = new HeaderServer(serverPort);
			try {
				headerServer.startServer(queryProcessor);
			} catch (IOException e) {
				System.out.println("server error" + e.getMessage());
			}
		}
	}
}