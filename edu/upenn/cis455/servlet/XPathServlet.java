package edu.upenn.cis455.servlet;

import java.io.*;
import java.net.*;

import javax.servlet.http.*;
import javax.swing.text.Document;

import org.jsoup.Jsoup;

import edu.upenn.cis455.xpathengine.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		// Parse input values
		String 		htmlXml	= request.getParameter("html/xml");
		String 		xpath	= request.getParameter("xpath");
		String[] 	xpaths 	= xpath.split(";");
		
		// Get document
		Document doc = null;
		try {
			doc = (Document) Jsoup.connect("http://en.wikipedia.org/").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set up XPATH Engine
		XPathEngine engine = XPathEngineFactory.getXPathEngine();
		engine.setXPaths(xpaths);
		
		try {
			doc = (Document) Jsoup.connect("http://en.wikipedia.org/").get();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		engine.setXPaths();		// Set XPATH Values
		
		// Eventually sent out display results
		System.out.println(response);

		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		
		// Displaying the interface
		File 			index = new File("workspace/HW2/WebContent/WEB-INF/index.html");
		FileInputStream fileInput = new FileInputStream(index);
		
		int content;
		try {
			while ((content = fileInput.read()) != -1) {
				response.getOutputStream().write(content);
			}
			fileInput.close();		// Closing the file stream
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}









