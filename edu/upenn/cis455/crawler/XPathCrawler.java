package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.storage.DBWrapper;


public class XPathCrawler {

	static String initialURL = "";
	static DBWrapper dbInstance = null;
	static int maxSize;
	static int numFiles;
	static Queue<String> urlQueue;
	static HashSet<String> previouslySearched = new HashSet<String>();
	static SimpleDateFormat universalFormat = null;
	static Date currentDate = null;

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		if (args.length == 3 || args.length == 4) {
			initialURL = args[0];
			dbInstance = new DBWrapper(args[1]);
			maxSize = Integer.parseInt(args[2]) * 100000;
			if (args.length == 4)
				numFiles = Integer.parseInt(args[3]);
			initializeCrawl();
		} else {
			usage();
		}
	}

	
	/**
	 * Initializes global crawler variables & crawler queue
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void initializeCrawl() throws UnknownHostException, IOException {
		// initializing date format
		universalFormat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss"); 
		currentDate = new Date();

		// initialze the queue and add it's first link
		urlQueue = new LinkedList<String>();
		urlQueue.add(initialURL);

		// start crawling
		run();
	}

	/**
	 * Starts the crawler process
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void run() throws UnknownHostException, IOException {
		// while the queue is still full, keep crawling
		while (!urlQueue.isEmpty()) {
			String nextUrl = urlQueue.poll();
			processUrl(nextUrl);
			previouslySearched.add(nextUrl);
		}

		// when the queue is empty exit the program
		exit();

	}

	private static void processUrl(String url) throws UnknownHostException,
			IOException {
		if (!previouslySearched.contains(url)) {
			if (url.startsWith("https://")) {
				processHttpsRequest(url);
			} else {

				// open up a socket and send a HEAD request
				URLInfo currentURL = new URLInfo(url);
				Socket headS = new Socket(currentURL.getHostName(), currentURL.getPortNo());
				PrintWriter headOutput = new PrintWriter(headS.getOutputStream());

				// sent first request to url
				headOutput.println("HEAD " + currentURL.getFilePath() + " HTTP/1.1");
				headOutput.println("Host: localhost");
				headOutput.println("User-Agent: cis455crawler");
				headOutput.println("");
				headOutput.flush();

				// get input
				BufferedReader headInput = new BufferedReader(new InputStreamReader(headS.getInputStream()));
				String res = "";
				String line = "";
				while (!(line = headInput.readLine()).equalsIgnoreCase("")) {
					res += line + "\n";
				}

				// closing the head socket
				headOutput.close();
				headInput.close();
				headS.close();

				System.out.println(res);

				// if content type is right
				if (checkContentTypeHttp(res) && checkSizeHttp(res) && checkModifiedHttp(url, res)) {

					// opening up a socket for a GET request
					Socket getS = new Socket(currentURL.getHostName(), currentURL.getPortNo());
					PrintWriter getOutput = new PrintWriter(getS.getOutputStream());

					// send another request
					getOutput.println("GET " + currentURL.getFilePath() + " HTTP/1.1");
					getOutput.println("Host: localhost");
					getOutput.println("User-Agent: cis455crawler");
					getOutput.println("");
					getOutput.flush();
					

				} else {
					return;
				}
				// Check size --> small enough
				// Check contains --> contained
				// Check if-modified since --> not modified
				// Modify lastAccessed of record
				// Download, update document and time, look for links + add to
				// queue
				// Download, create new document and time record, look for links
				// + add to queue
				// Don't do anything

			}
		}
		return;
	}
	
	/**
	 * Returns whether or not the content type of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content type is one to be examined, false if not
	 */
	private static boolean checkContentTypeHttp(String res) {
		return true;
	}
	
	/**
	 * Returns whether or not the content length of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content length is short enough, false if not
	 */
	private static boolean checkSizeHttp(String res) {
		return true;
	}
	
	/**
	 * Returns whether or not the last modified since of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content is recent enough, false if not
	 */
	private static boolean checkModifiedHttp(String url, String res) {
		return true;
	}
	
	/**
	 * Processes urls that start with https://
	 * @param url - https url to process
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void processHttpsRequest(String url) throws UnknownHostException, IOException {
		// open up a socket to send a HEAD request
		HttpsURLConnection headConnect = (HttpsURLConnection) new URL(url).openConnection();
		headConnect.setRequestMethod("HEAD");
		int contentLength = headConnect.getContentLength();
		long lastModified = headConnect.getLastModified();
		String contentType = headConnect.getContentType();

		// disconnect after getting relevant info
		headConnect.disconnect();
		
		// if content type is appropriate, it's small enough, and has been modified since last crawled
		if (checkContentTypeHttps(contentType) && 
				checkSizeHttps(contentLength) && checkModifiedHttps(url, lastModified)) {
			
			// open up a new connection
			HttpsURLConnection getConnect = (HttpsURLConnection) new URL(url).openConnection();
			getConnect.setRequestMethod("GET");
			
			// Setting up JTidy
			Tidy domParse = new Tidy();
			domParse.setForceOutput(true);
			domParse.setShowErrors(0);
			domParse.setQuiet(true);
			
			// Checking if html or xml
			boolean	isHTML = !getConnect.getContentType().contains("xml");
			
			// HTML Parsing
			Document docTemp = null;
			
			// XML Parsing
			DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
			DocumentBuilder	dom = null;
			
			// Parsing HTML
			if (isHTML) {
				try {
					docTemp = domParse.parseDOM(getConnect.getInputStream(), null);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				searchForUrls(docTemp);
			
			// Parsing XML
			} else {
				try {
					dom = builder.newDocumentBuilder();
					docTemp = dom.parse(getConnect.getInputStream());
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
				searchForUrls(docTemp);
			}
		
		// if content isn't what it expects
		} else {
			return;
		}
	}

	/**
	 * Returns whether or not the content type of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content type is one to be examined, false if not
	 */
	private static boolean checkContentTypeHttps(String res) {
		if (res.contains("text/xml")) {
			return true;
		} else if (res.contains("text/html")) {
			return true;
		} else {
			return res.contains("+xml");
		}
	}

	/**
	 * Returns whether or not the last modified since of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content is recent enough, false if not
	 */
	private static boolean checkSizeHttps(int length) {
		return maxSize >= length;
	}

	/**
	 * Returns whether or not the last modified since of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content is recent enough, false if not
	 */
	private static boolean checkModifiedHttps(String url, long date) {
		
		// get DB's data
		long lastModified = dbInstance.getLastAccess(url);
		
		// not in DB
		if (lastModified == 0) {
			return true;
		} else {
			return (lastModified < date);
		}
	}
	
	private static void searchForUrls(Document d) {
		System.out.println("I'm searching for URLs!");
	}

	private static void exit() {

	}

	public static void usage() {
		System.out.println("Execution: java Crawler <url> <database-location> <max file size> [# of files to retrieve]");
		System.out.println("<url>                 the url to start the web crawl at");
		System.out.println("<database-location>   location of datastore for crawler data");
		System.out.println("<max file size>       maximum size of a file in MB to be scanned by the crawler");
		System.out.println("[# of files]          number of files to crawl before exiting the crawler");
	}

	
}
