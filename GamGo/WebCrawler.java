package GamGo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * web crawl class for crawling web pages from given url
 */
public class WebCrawler {
	/**
	 * initialize
	 */
	private final Set<URL> visitedURI;
	/**
	 * initialize
	 */
	private final ThreadedInvertedIndex multithread;
	/**
	 * initialize
	 */
	private WorkQueue workqueue;
	/**
	 * initialize
	 */
	private int crawlNumber;
	/**
	 * initialize
	 */
	private String input;

	/**
	 * constructor
	 * 
	 * @param multithread stores crawled data
	 * @param workqueue   work queue to use
	 * @param crawlNumber max number of urls
	 * @param input       initial url
	 */
	public WebCrawler(ThreadedInvertedIndex multithread, WorkQueue workqueue, int crawlNumber, String input) {
		this.multithread = multithread;
		this.workqueue = workqueue;
		this.crawlNumber = crawlNumber;
		this.visitedURI = new HashSet<>();
		this.input = input;
	}

	/**
	 * start the crawl process from url
	 * 
	 * @throws MalformedURLException exception thrown
	 */
	public void processHTML() throws MalformedURLException {
		URL seed = new URL(input);
		visitedURI.add(seed);
		workqueue.execute(new Task(seed));
		workqueue.finish();
	}

	/**
	 * Processes HTML clean URL, fetch HTML content, extract and process URLs, and
	 * index
	 *
	 * @param input input URL
	 * @throws MalformedURLException exception thrown
	 * @throws URISyntaxException    exception thrown
	 */
	public void processHTML(String input) throws MalformedURLException, URISyntaxException {
		URI uri = LinkFinder.cleanUri(new URI(input));
		String htmlContent = HtmlFetcher.fetch(uri.toURL());
		if (htmlContent == null) {
			return;
		}
		String clean = HtmlCleaner.stripBlockElements(htmlContent);
		ArrayList<URL> foundUrls = LinkFinder.listUrls(uri.toURL(), clean);
		processUrls(foundUrls);
		clean = HtmlCleaner.stripHtml(clean);
		indexContent(uri, clean);
	}

	/**
	 * Adds new URLs to the work queue if not visited yet and below crawl limit
	 *
	 * @param urls list of URLs
	 */
	private void processUrls(ArrayList<URL> urls) {
		synchronized (visitedURI) {
			for (URL url : urls) {
				if ((!visitedURI.contains(url)) && (visitedURI.size() < crawlNumber)) {
					visitedURI.add(url);
					workqueue.execute(new Task(url));
				}
			}
		}
	}

	/**
	 * Indexes the content of a web page
	 *
	 * @param uri     URI of the web
	 * @param content textual content of the web
	 */
	private void indexContent(URI uri, String content) {
		ThreadedInvertedIndex index = new ThreadedInvertedIndex();
		SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
		int place = 0;
		for (String word : FileStemmer.parse(content)) {
			index.addIndex(stemmer.stem(word).toString(), uri.toString(), ++place);
		}
		multithread.addDistinct(index);
	}

	/**
	 * nested class for the workqueue task
	 */
	private class Task implements Runnable {
		/**
		 * initialize
		 */
		private final URL urlToProcess;

		/**
		 * task
		 * 
		 * @param urlToProcess the url to process
		 */
		public Task(URL urlToProcess) {
			this.urlToProcess = urlToProcess;
		}

		/**
		 * run method that processes the html content
		 */
		@Override
		public void run() {
			try {
				processHTML(urlToProcess.toString());
			} catch (MalformedURLException e) {
				System.out.println("Error processing URL: " + e.getMessage());
			} catch (URISyntaxException e) {
				System.out.println("Error processing URL: " + e.getMessage());
			}

		}
	}
}