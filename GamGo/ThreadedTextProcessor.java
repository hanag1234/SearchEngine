package GamGo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class processes directories of text files using multithreading It uses a
 * work queue for tasks and processes each file to update the inverted index
 * with stemmed words
 */
public class ThreadedTextProcessor {
	/**
	 * initialize logger
	 */
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Processes all text files within path recursively given the multithread
	 * instance to update the inverted index
	 *
	 * @param multithread the multihtread instance for indexing
	 * @param path        path to use
	 * @param workqueue   workqueue to use
	 * @throws IOException thrown error IO
	 */
	public static void processDirectory(ThreadedInvertedIndex multithread, Path path, WorkQueue workqueue)
			throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path paths : stream) {
				if (Files.isDirectory(paths)) {
					processDirectory(multithread, paths, workqueue);
				} else if (TextProcessor.isText(paths)) {
					workqueue.execute(new Task(paths, multithread));
				}
			}
		}
	}

	/**
	 * Starts the processing of path for indexing. Creates a work queue and assigns
	 * tasks for each file to be processed at the same time
	 *
	 * @param multithread the multithread instance for indexing
	 * @param inputPath   path to use
	 * @param workqueue   workqueue to use
	 * @throws IOException          thrown IO error
	 * @throws InterruptedException thrown interruptedException
	 */
	public static void process(ThreadedInvertedIndex multithread, Path inputPath, WorkQueue workqueue)
			throws IOException, InterruptedException {
		if (Files.isDirectory(inputPath)) {
			processDirectory(multithread, inputPath, workqueue);
		} else {
			workqueue.execute(new Task(inputPath, multithread));
		}
		workqueue.finish();
	}

	/**
	 * Nested class represents a single task for processing a text file and
	 * implements runnable to allow execution by a thread in the work queue
	 */
	private static class Task implements Runnable {
		/**
		 * initialize
		 */
		private final Path inputPath;
		/**
		 * initialize
		 */
		private final ThreadedInvertedIndex multithread;

		/**
		 * Constructs new task for processing the text file
		 *
		 * @param inputPath   path to use
		 * @param multithread the multithread instance for indexing
		 */
		public Task(Path inputPath, ThreadedInvertedIndex multithread) {
			this.inputPath = inputPath;
			this.multithread = multithread;
		}

		/**
		 * Reads the text file, stems each word, and updates the inverted index
		 */
		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				TextProcessor.processFile(inputPath, local);
				multithread.addDistinct(local);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
