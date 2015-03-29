package edu.upenn.cis455.crawler.info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.w3c.dom.Document;

import edu.upenn.cis455.storage.DBWrapper;

public class Crawler {
	
	static String initialURL = "";
	static DBWrapper dbInstance = null;
	static String maxSize = "";
	static int numFiles;
	static Queue<String> urlQueue;
	static HashSet<String> previouslySearched = new HashSet<String>();
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length == 3 || args.length == 4) {
			initialURL = args[0];
			dbInstance = new DBWrapper(args[1]);
			maxSize = args[2];
			if (args.length == 4)
				numFiles = Integer.parseInt(args[3]);
			initializeCrawl();
		} else {
			usage();
		}
	}
	
	private static void initializeCrawl() throws UnknownHostException, IOException {
		// initialze the queue and add it's first link
		urlQueue = new LinkedList<String>();
		urlQueue.add(initialURL);
		
		// start crawling
		run();
	}
	
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
	
	private static void processUrl(String url) throws UnknownHostException, IOException {
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
				if (checkContentType(res)) {
					
					// opening up a socket for a GET request
					Socket getS = new Socket(currentURL.getHostName(), currentURL.getPortNo());
					PrintWriter getOutput = new PrintWriter(getS.getOutputStream());
					
					// send another request
					getOutput.println("GET " + currentURL.getFilePath() + " HTTP/1.1");
					getOutput.println("Host: localhost");
					getOutput.println("");
					getOutput.flush();
					
					BufferedReader getInput = new BufferedReader(new InputStreamReader(getS.getInputStream()));
					
					// reset res & line
					res = "";
					line = "";
					
					while ((line = getInput.readLine()) != null) {
						System.out.println(line);
						res += line + "\n";
						
						// check if the content-length is smaller than maximum
						if (line.contains("Content-Length:")) {
							if (!checkSize(line)) {
								return;
							}
						
						// check if it was modified since it's last crawling
						} else if (line.contains("Last-Modified:")) {
							if (!checkModified(line)) {
								return;
							}
						}
					}
					
					
				} else {
					return;
				}
					// Check size --> small enough
						// Check contains --> contained
							// Check if-modified since --> not modified
								// Modify lastAccessed of record
							// Download, update document and time, look for links + add to queue
						// Download, create new document and time record, look for links + add to queue
					// Don't do anything
					
			}
		}
		return;
	}
	
	private static boolean checkContentType(String res) {
		if (res.contains("Content-Type: text/xml")) {
			return true;
		} else if (res.contains("Content-Type: text/html")) {
			return true;
		} else if (res.contains("Content-Type: ")) {
			return res.contains("+xml");
		} else {
			return false;
		}
	}
	
	private static boolean checkSize(String line) {
		return true;
	}
	
	private static boolean checkModified(String line) {
		return false;
	}
	
	private static void processHttpsRequest(String url) {
		
	}
	
	private static void searchForUrls(Document d) {
		
	}
	
	private static void exit() {
		
	}
	
	
	public static void usage() {
		System.out.println("Execution: java Crawler <url> <database-location> <max file size> [# of files to retrieve]");
		System.out.println("<url>.................the url to start the web crawl at");
		System.out.println("<database-location>...location of datastore for crawler data");
		System.out.println("<max file size>.......maximum size of a file to be scanned by the crawler");
		System.out.println("[# of files]..........number of files to crawl before exiting the crawler");
	}

}
