package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.storage.DBWrapper;


public class XPathCrawler {

	static String initialUrl = "";
	static String currentUrl = "";
	static String currentProtocol = "";
	static DBWrapper dbInstance = null;
	static int maxSize;
	static int numFiles;
	static Queue<String> urlQueue;
	static HashSet<String> previouslySearched = new HashSet<String>();
	static SimpleDateFormat universalFormat = null;
	static Date currentDate = null;
	static HashMap<String, RobotsTxtInfo> robotsMap = new HashMap<String, RobotsTxtInfo>();

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		if (args.length == 3 || args.length == 4) {
			initialUrl = args[0];
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
		urlQueue.add(initialUrl);

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
			// storing information about the url
			String nextUrl = urlQueue.poll();
			currentUrl = nextUrl;
			if (nextUrl.startsWith("https://")) {
				currentProtocol = "https://";
			} else {
				currentProtocol = "http://";
			}
			// checking politeness prior to downloading
			if (politeness(nextUrl)) {
				processUrl(nextUrl);
			} 
			previouslySearched.add(nextUrl);
		}

		// when the queue is empty exit the program
		exit();
	}

	/**
	 * Processes the url, adding links if html, checking xpath if xml
	 * @param url - url to process
	 * @throws UnknownHostException
	 * @throws IOException
	 */
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
				headOutput.println("User-agent: cis455crawler");
				headOutput.println("");
				headOutput.flush();
				System.out.println("Checking crawlability of: " + url);
				// get input
				BufferedReader headInput = new BufferedReader(new InputStreamReader(headS.getInputStream()));
				String line = "";
				String contentType = "";
				while (!(line = headInput.readLine()).equalsIgnoreCase("")) {
					if (line.contains("Content-Type:")) {
						contentType = line;
						if (!checkContentTypeHttp(line))
							return;
					} else if (line.contains("Content-Length:")) {
						if (!checkSizeHttp(line))
							return;
					} else if (line.contains("Last-Modified:")) {
						if (!checkModifiedHttp(url, line))
							return;
					}
				}
				// closing the head socket
				headOutput.close();
				headInput.close();
				headS.close();
				// opening up a socket for a GET request
				Socket getS = new Socket(currentURL.getHostName(), currentURL.getPortNo());
				PrintWriter getOutput = new PrintWriter(getS.getOutputStream());
				// send another request
				getOutput.println("GET " + currentURL.getFilePath() + " HTTP/1.1");
				getOutput.println("Host: localhost");
				getOutput.println("User-agent: cis455crawler");
				getOutput.println("");
				getOutput.flush();
				// Setting up JTidy
				Tidy domParse = new Tidy();
				domParse.setForceOutput(true);
				domParse.setShowErrors(0);
				domParse.setQuiet(true);
				boolean isHTML = !contentType.contains("xml");
				// HTML Parsing
				Document docTemp = null;
				// XML parsing
				DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
				DocumentBuilder dom = null;
				// parsing HTML
				System.out.println("Downloading: " + url);
				if (isHTML) {
					docTemp = domParse.parseDOM(new BufferedReader(
							new InputStreamReader(getS.getInputStream())), null);
					// searching document
					searchForUrls(docTemp);
					// adding to store
					System.out.println("Finished processing: " + url);
					System.out.println("");
					dbInstance.putDocument(url, docTemp);
					dbInstance.updateTime(url, currentDate.getTime());
				// parsing XML
				} else {
					try {
						dom = builder.newDocumentBuilder();
						docTemp = dom.parse(getS.getInputStream());
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}

					// EVENTUALLY -> check xpaths & add to channel

					// adding to store
					System.out.println("Finished processing: " + url);
					System.out.println("");
					dbInstance.putDocument(url, docTemp);
					dbInstance.updateTime(url, currentDate.getTime());
				}
				getS.close();
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
		String contentType = res.split(";")[0];
		return (contentType.endsWith("text/html") || contentType.endsWith("text/xml") ||
				contentType.endsWith("application/xml") || contentType.endsWith("+xml"));
	}
	
	/**
	 * Returns whether or not the content length of a url's HEAD response is ok to examine
	 * @param res - HEAD response to check
	 * @return true if content length is short enough, false if not
	 */
	private static boolean checkSizeHttp(String res) {
		return maxSize >= Integer.parseInt(res.split(" ")[1]);
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
		headConnect.setRequestProperty("User-agent:", "cis455crawler");
		int contentLength = headConnect.getContentLength();
		long lastModified = headConnect.getLastModified();
		String contentType = headConnect.getContentType();
		// disconnect after getting relevant info
		headConnect.disconnect();
		System.out.println("Checking crawlability of: " + url);
		// if content type is appropriate, it's small enough, and has been modified since last crawled
		if (checkContentTypeHttps(contentType) && 
				checkSizeHttps(contentLength) && checkModifiedHttps(url, lastModified)) {
			// open up a new connection
			HttpsURLConnection getConnect = (HttpsURLConnection) new URL(url).openConnection();
			getConnect.setRequestMethod("GET");
			getConnect.setRequestProperty("User-agent:", "cis455crawler");
			// Setting up JTidy
			Tidy domParse = new Tidy();
			domParse.setForceOutput(true);
			domParse.setShowErrors(0);
			domParse.setQuiet(true);
			boolean isHTML = true;
			// Checking if html or xml
			if (getConnect.getContentType() != null) {
				isHTML = !getConnect.getContentType().endsWith("xml");
			}
			// HTML Parsing
			Document docTemp = null;
			// XML parsing
			DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
			DocumentBuilder	dom = null;
			// parsing HTML
			System.out.println("Downloading: " + url);
			if (isHTML) {
				try {
					docTemp = domParse.parseDOM(getConnect.getInputStream(), null);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// searching document
				searchForUrls(docTemp);
				// adding to store
				System.out.println("Finished processing: " + url);
				System.out.println("");
				dbInstance.putDocument(url, docTemp);
				dbInstance.updateTime(url, currentDate.getTime());
			// parsing XML
			} else {
				try {
					dom = builder.newDocumentBuilder();
					docTemp = dom.parse(getConnect.getInputStream());
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
				
				// EVENTUALLY -> check xpaths & add to channel
				
				// adding to store
				System.out.println("Finished processing: " + url);
				System.out.println("");
				dbInstance.putDocument(url, docTemp);
				dbInstance.updateTime(url, currentDate.getTime());
			}
		// if content isn't crawlable
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
		if (res != null) {
			if (res.contains("text/xml")) {
				return true;
			} else if (res.contains("text/html")) {
				return true;
			} else {
				return res.contains("+xml");
			}
		} else {
			return true;
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
		long lastModified = dbInstance.getLastAccess(url);
		if (lastModified == 0) {
			return true;
		} else {
			return (lastModified < date);
		}
	}
	
	/**
	 * Start the recursive search for links
	 * @param d - document to search
	 */
	private static void searchForUrls(Document d) {
		Node root = d.getDocumentElement();
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			searchNode(children.item(i));
		}
	}
	
	/**
	 * Method to recursively search nodes in a DOM
	 * @param n - node to recursively search
	 */
	private static void searchNode(Node n) {
		if (n != null) {
			if (n.hasChildNodes()) {
				if (isLinkNode(n)) {
					addLink(n);
				}
				NodeList children = n.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					searchNode(children.item(i));
				}
			} else {
				if (isLinkNode(n)) {
					addLink(n);
				}
			}
		}
	}
	
	/**
	 * Checks if a given node is a link node
	 * @param n - node to examine
	 * @return true if it is a link node, false if not
	 */
	private static boolean isLinkNode(Node n) {
		return (equals(n.getLocalName(), "a"));
	}
	
	/**
	 * Method to add links within a node that is determined to contain a link
	 * @param n - node from which to add a link
	 */
	private static void addLink(Node n) {
		// get the linked url & the current host url
		String linkedUrl = n.getAttributes().getNamedItem("href").getNodeValue();
		String hostUrl = currentUrl;
		hostUrl = hostUrl.replaceFirst("s", "");
		if ((!hostUrl.endsWith("/") && !(hostUrl.endsWith(".html") || hostUrl.endsWith(".xml")) && !hostUrl.contains("?"))) {
			hostUrl += "/";
		}
		// url needs reformatting
		if (!(linkedUrl.startsWith("http://") || linkedUrl.startsWith("https://") || linkedUrl.startsWith("www."))) {
			URLInfo currentInfo = new URLInfo(hostUrl);
			// linked url is relative off of current host
			if (linkedUrl.startsWith("/")) {
				String newUrl = currentProtocol + currentInfo.getHostName() + linkedUrl;
				if (!previouslySearched.contains(newUrl) && !urlQueue.contains(newUrl))
					urlQueue.add(newUrl);
			// linked url is relative off of current filepath
			} else {
				String currentPath = currentInfo.getFilePath().substring(0, currentInfo.getFilePath().lastIndexOf("/") + 1);
				String newUrl = currentProtocol + currentInfo.getHostName() + currentPath + linkedUrl;
				if (!previouslySearched.contains(newUrl) && !urlQueue.contains(newUrl))
					urlQueue.add(newUrl);
			}
		// url does not need reformatting
		} else {
			urlQueue.add(linkedUrl);
		}
	}

	/**
	 * Prints message when crawling is finished
	 */
	private static void exit() {
		System.out.println("Done crawling");
	}
	
	private static boolean politeness(String url) throws UnknownHostException, IOException {
		// getting the robots.txt file
		String urlInfo = "";
		if (url.startsWith("https://"))
			urlInfo = url.replaceFirst("s", "");
		URLInfo currentInfo = new URLInfo(urlInfo);
		RobotsTxtInfo currentRobots = null;
		String newUrl = currentProtocol + currentInfo.getHostName() + "/robots.txt";
		// checking if not already contained
		if (!robotsMap.containsKey(newUrl)) {
			HttpsURLConnection getConnect = (HttpsURLConnection) new URL(newUrl).openConnection();
			getConnect.setRequestMethod("GET");
			String res = "";
			int line;
			InputStream getInput = getConnect.getInputStream();
			while ((line = getInput.read()) != -1 ) {
				res += (char) line;
			}
			// Parsing and storing robots.txt
			String[] agents = res.split("\n\n");
			currentRobots = new RobotsTxtInfo();
			for (String s : agents) {
				String agent = "";
				for (String l : s.split("\n")) {
	
					// Comment
					if (l.startsWith("#")) {
					// User-agent
					} else if (l.startsWith("User-agent:")) {
						agent = l.split(" ")[1];
						currentRobots.addUserAgent(agent);
					// Disallowed links
					} else if (l.startsWith("Disallow:")) {
						String disallowed = l.split(" ")[1];
						currentRobots.addDisallowedLink(agent, disallowed);
					// Crawl-delays
					} else if (l.startsWith("Crawl-delay:")) {
						int delay = Integer.parseInt(l.split(" ")[1]);
						currentRobots.addCrawlDelay(agent, delay);
					}
				}
			}
			robotsMap.put(newUrl, currentRobots);
		} else {
			currentRobots = robotsMap.get(newUrl);
		}
		// getting structures and values for either cis455crawler or *
		ArrayList<String> disallowedLinks = null;;
		int crawlDelay = 0;
		// if robots specifies cis455crawler rules
		if (currentRobots.containsUserAgent("cis455crawler")) {
			disallowedLinks = currentRobots.getDisallowedLinks("cis455crawler");
			crawlDelay = currentRobots.getCrawlDelay("cis455crawler");
			for (String s : disallowedLinks) {
				// if link is disallowed
				if (equals(currentProtocol + currentInfo.getHostName() + s, url)) {
					return false;
				}
			} 
			return true;
		// if robots specifies general rules
		} else if (currentRobots.containsUserAgent("*")) {
			disallowedLinks = currentRobots.getDisallowedLinks("*");
			crawlDelay = currentRobots.getCrawlDelay("*");
			for (String s : disallowedLinks) {
				// if link is disallowed
				if (equals(currentProtocol + currentInfo.getHostName() + s, url)) {
					return false;
				}
			} 
			return true;
		// assume politeness if no robots.txt
		} else {
			return true;
		}
	}

	/**
	 * Prints the command structure in case user enters commands wrong
	 */
	public static void usage() {
		System.out.println("Execution: java Crawler <url> <database-location> <max file size> [# of files to retrieve]");
		System.out.println("<url>                 the url to start the web crawl at");
		System.out.println("<database-location>   location of datastore for crawler data");
		System.out.println("<max file size>       maximum size of a file in MB to be scanned by the crawler");
		System.out.println("[# of files]          number of files to crawl before exiting the crawler");
	}
	
	/**
	 * Compares two strings char by char
	 * @param s1 - 1st string to compare
	 * @param s2 - 2nd string to compare
	 * @return true if equal, false if not
	 */
	private static boolean equals(String s1, String s2) {
		boolean ret = false;
		int i = 0;
		char[] s2Arr = s2.toCharArray();
		if (s2.length() == s1.length()) {
			for (char c : s1.toCharArray()) {
				if (c != s2Arr[i]) {
					return ret;
				} else {
					i++;
				}
			}
			if (i == s2Arr.length) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
