package edu.usfca.cs272;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Creates a web server to allow users to fetch HTTP headers for a URL.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class HeaderServer {
	/**
	 * port number
	 */
	private int serverPort;

	/**
	 * Constructor
	 * 
	 * @param serverPort the port number
	 */
	public HeaderServer(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * Starts Jetty server
	 * 
	 * @param queryProcessor the query processor interface
	 * @throws IOException exception throw
	 */
	public void startServer(QueryProcessorInterface queryProcessor) throws IOException {
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(new ServletHolder(new HeaderServlet(queryProcessor)), "/");
		Server server = new Server(serverPort);
		server.setHandler(handler);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setHandler(handler);
		context.setSessionHandler(new SessionHandler());
		server.setHandler(context);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.err.println("Error starting server: " + e.getMessage());
		}
	}
}
