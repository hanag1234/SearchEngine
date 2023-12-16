package GamGo;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * class that extends httpservlet which is for handling servlet requests from
 * search queries
 */
public class HeaderServlet extends HttpServlet {
	/** ID used for serialization, which we are not using. */
	private static final long serialVersionUID = 202308;
	/**
	 * initialize results
	 */
	List<String> results;
	/**
	 * initialize queryProcessor
	 */
	QueryProcessorInterface queryProcessor;
	/**
	 * initialize
	 */
	private List<String> searchHistory;
	/**
	 * initialize
	 */
	private List<String> visitedResults = new ArrayList<>();
	/**
	 * initialize
	 */
	private List<String> favoriteResults = new ArrayList<>();

	/**
	 * constructor
	 * 
	 * @param queryProcessor queryProcessor interface
	 */
	public HeaderServlet(QueryProcessorInterface queryProcessor) {
		super();
		this.queryProcessor = queryProcessor;
		this.searchHistory = new ArrayList<>();
	}

	/**
	 * Handles HTTP GET requests for displaying the search
	 * 
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws ServletException If a servlet-specific error
	 * @throws IOException      If an I/O error
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		// visit param
		String visit = request.getParameter("visit");
		if (visit != null && !visit.isEmpty()) {
			LocalDateTime timestamp = LocalDateTime.now();
			visitedResults.add(visit + " - Visited at: " + timestamp.toString());
			response.sendRedirect(visit);
			return;
		}
		out.print("<html><head>");
		// css
		out.print("<style>");
		// colour mode
		out.print("body.light-mode { background-color: #FFB6C1; color:black; font-family: 'Roboto', sans-serif;}");
		out.print("body.dark-mode { background-color: #E0115F; color: black; font-family: 'Roboto', sans-serif;}");
		out.print("body { background-color: pink; }");
		out.print("</style>");
		out.print("</head><body>");
		out.print("<style>");
		out.print(
				"h1 { font-size: 48px; text-align: center; color: #333; font-family: 'Arial', sans-serif; margin-top: 20px; }");
		out.print(
				".header-container { background-color: #f2f2f2; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }");
		out.print("</style>");
		out.print("<div class='header-container'>");
		// new
		out.print("<header>");
		out.print("<img src='Images/gamgodark2.png' alt='Logo' style='max-width: 100px; margin-right: 20px;'>");
		out.print("<h1>GamGo</h1>");
		out.print("</header>");
		//
//		out.print("<h1> GamGo </h1>");
		out.print("</div>");
		// small buttons
		out.print("<style>");
		out.print("input[type='submit'].small-button { padding: 5px 10px; font-size: 12px; }");
		out.print("</style>");
		//
		out.print("<style>");
		out.print(".search-container { text-align: center; margin-top: 20px; }");
		out.print("input[type='text'] { width: 50%; padding: 10px; font-size: 20px; }");
		out.print("input[type='submit'] { padding: 10px 20px; font-size: 20px; }");
		out.print("</style>");
		out.print("<div class='search-container'>");
		out.print("<form action='/' method='POST'>");
		out.print("search: <input type='text' name='words' />");
		out.print("<input type='submit' value='Find' />");
		out.print("</form>");
		out.print("</div>");
		out.print("<button id='modeToggle'>Toggle Dark/Light Mode</button>");
		out.println("<script>");
		out.println("const toggleButton = document.getElementById('modeToggle');");
		out.println("const bodyElement = document.body;");
		out.println("const mode = sessionStorage.getItem('mode');");
		out.println("if (mode) {");
		out.println("    bodyElement.className = mode;");
		out.println("}");
		out.println("toggleButton.addEventListener('click', () => {");
		out.println("    if (bodyElement.classList.contains('dark-mode')) {");
		out.println("        bodyElement.className = 'light-mode';");
		out.println("        sessionStorage.setItem('mode', 'light-mode');");
		out.println("    } else {");
		out.println("        bodyElement.className = 'dark-mode';");
		out.println("        sessionStorage.setItem('mode', 'dark-mode');");
		out.println("    }");
		out.println("});");
		out.println("</script>");
		String urlString = request.getParameter("url");
		if (urlString != null && !urlString.isEmpty()) {
			try {
				URL url = new URL(urlString);
				Map<String, List<String>> headers = getHttpHeaders(url);
				out.println("<h2>HTTP Headers:</h2>");
				out.println("<ul>");
				for (Map.Entry<String, List<String>> header : headers.entrySet()) {
					out.print("<li>" + header.getKey() + ": ");
					for (String value : header.getValue()) {
						out.print(value);
					}
					out.println("</li>");
				}
				out.println("</ul>");
			} catch (Exception e) {
				out.println("<p>Error fetching headers: " + e.getMessage() + "</p>");
			}
		}
		// show search history
		out.print("<h2>Search History ");
		out.println("<form action='/clearHistory' method='post' style='display: inline;'>");
		out.println("<input type='hidden' name='clear' value='true'/>");
		out.println("<input type='submit' class='small-button' value='Clear History' />");
		out.println("</form></h2>");
		out.print("<ul>");
		for (String query : searchHistory) {
			out.print("<li>" + query + "</li>");
		}
		out.print("</ul>");
		// show visit results
		out.print("<h2>Visited Results ");
		out.println("<form action='/clearVisited' method='post' style='display: inline;'>");
		out.println("<input type='hidden' name='clearValue' value='true'/>");
		out.println("<input type='submit' class='small-button' value='Clear Visited' />");
		out.println("</form></h2>");
		out.print("<ul>");
		for (String url : visitedResults) {
			String[] parts = url.split(" - Visited at: ", 2);
			out.print("<li><a href=\"" + parts[0] + "\">" + parts[0] + "</a> - Visited at: " + parts[1] + "</li>");
		}
		out.print("</ul>");
		// fav results
		out.print("<h2>Favorite Results ");
		out.println("<form action='/clearFavorites' method='post' style='display: inline;'>");
		out.println("<input type='hidden' name='clearFavorites' value='true'/>");
		out.println("<input type='submit' class='small-button' value='Clear Favorites' />");
		out.println("</form></h2>");
		out.print("<ul>");
		for (String favorite : favoriteResults) {
			String decodedFavorite = URLDecoder.decode(favorite, "UTF-8");
			out.print("<li><a href=\"" + decodedFavorite + "\">" + decodedFavorite + "</a></li>");
		}
		out.print("</ul>");
		out.println("</body></html>");
	}

	/**
	 * Handles HTTP POST requests for processing search queries and displaying
	 * results
	 * 
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws ServletException If a servlet-specific error
	 * @throws IOException      If an I/O error
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// if clear history button is pressed
		if ("true".equals(request.getParameter("clear"))) {
			searchHistory.clear();
			response.sendRedirect("/");
			return;
		}
		// if the clear value button is pressed
		if ("true".equals(request.getParameter("clearValue"))) {
			visitedResults.clear();
			response.sendRedirect("/");
			return;
		}
		String favorite = request.getParameter("favorite");
		if (favorite != null && !favorite.isEmpty()) {
			if (!favoriteResults.contains(favorite)) {
				favoriteResults.add(favorite);
			}
			response.sendRedirect("/");
			return;
		}
		// Check if the clear favorites button was pressed
		if ("true".equals(request.getParameter("clearFavorites"))) {
			favoriteResults.clear();
			response.sendRedirect("/");
			return;
		}
		String words = request.getParameter("words");
		words = words == null ? "" : words;
		words = StringEscapeUtils.escapeHtml4(words);
		// add search to history with time
		LocalDateTime timestamp = LocalDateTime.now();
		String timestampedQuery = words + " - Searched at: " + timestamp.toString();
		searchHistory.add(timestampedQuery);
		// query processing
		response.setStatus(HttpServletResponse.SC_OK);
		queryProcessor.processQuery(words, true);
		var results = queryProcessor.getResults(words);
		// response
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		Set<String> displayedResults = new HashSet<>();
		for (var result : results) {
			if (!displayedResults.contains(result.getWhere())) {
				out.println("<p><a href=\"?visit=" + URLEncoder.encode(result.getWhere(), "UTF-8") + "\">"
						+ result.getWhere()
						+ "</a> - <form action='/' method='POST'><input type='hidden' name='favorite' value='"
						+ URLEncoder.encode(result.getWhere(), "UTF-8")
						+ "'/><input type='submit' value='Save to Favorites'/></form></p>");
				displayedResults.add(result.getWhere());
			}
		}
		// home
		out.println("<form action='/' method='get'>");
		out.println("<input type='submit' value='Home' />");
		out.println("</form>");
	}

	/**
	 * Opens a socket connection to the web server for the provided URL and uses the
	 * HTTP HEAD method to fetch and return ONLY the HTTP headers.
	 *
	 * @param url the URL to fetch HTTP headers for
	 * @return map of HTTP headers
	 * @throws Exception if unable to fetch for any reason
	 */
	public static Map<String, List<String>> getHttpHeaders(URL url) throws Exception {
		try (Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), UTF_8);
				BufferedReader response = new BufferedReader(input);) {
			String host = url.getHost();
			String path = url.getPath();
			if (path.isEmpty()) {
				path = "/";
			}
			request.print("HEAD " + path + " HTTP/1.1\r\n");
			request.print("Host: " + host + "\r\n");
			request.print("Connection: close\r\n");
			request.print("\r\n");
			request.flush();
			Map<String, List<String>> headers = new HashMap<>();
			String line;
			while ((line = response.readLine()) != null && !line.isEmpty()) {
				String[] header = line.split(": ", 2);
				if (header.length == 2) {
					headers.computeIfAbsent(header[0], k -> new ArrayList<>()).add(header[1]);
				}
			}
			return headers;
		}
	}
}