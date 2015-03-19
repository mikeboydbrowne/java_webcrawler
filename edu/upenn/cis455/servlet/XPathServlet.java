package edu.upenn.cis455.servlet;

import java.io.*;
import java.net.*;

import javax.servlet.http.*;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.xpathengine.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		// Parse inputs
		String 		htmlXml		= standardizeReq(request.getParameter("html/xml"));
		String 		xpath		= request.getParameter("xpath");
		String[] 	xpaths 		= xpath.split(";");
		boolean		isHTML 		= true;
		
		// Checking if html or xml
		isHTML = !htmlXml.endsWith(".xml");
		
		// Setting up JTidy
		Tidy		domParse	= new Tidy();
		domParse.setForceOutput(true);
		domParse.setShowErrors(0);
		domParse.setQuiet(true);
		Document doc = null;
		
		// Parsing if HTML
		if (isHTML) {
			try {
				doc = domParse.parseDOM(new URL(htmlXml).openStream(), System.out);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		// Parsing if XML
		} else {
			
		}
		
		// Set up XPATH Engine
		XPathEngine engine = XPathEngineFactory.getXPathEngine();
		engine.setXPaths(xpaths);
		engine.evaluate(doc);
		
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
	
	private String standardizeReq(String req) {
		if (req.startsWith("http://")) {
			return req;
		} else if (req.startsWith("www.")){
			return "http://" + req;
		} else if (req.startsWith("http://www.")) {
			return req;
		} else {
			return "http://www." + req;
		}
	}
}









