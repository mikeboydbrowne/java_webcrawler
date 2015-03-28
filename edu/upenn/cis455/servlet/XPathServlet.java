package edu.upenn.cis455.servlet;

import java.io.*;
import java.net.*;

import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		// Parse inputs
		String htmlXml = standardizeReq(request.getParameter("html/xml"));
		String xpath = request.getParameter("xpath");
		String[] xpaths = xpath.split(";");
		boolean	isHTML = true;
		
		// Checking if html or xml
		isHTML = !htmlXml.endsWith(".xml");
		
		// Setting up JTidy
		Tidy domParse = new Tidy();
		domParse.setForceOutput(true);
		domParse.setShowErrors(0);
		domParse.setQuiet(true);
		
		// HTML Parsing
		Document docTemp = null;
		
		// XML Parsing
		DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
		DocumentBuilder	dom = null;
		
		// Parsing HTML
		if (isHTML) {
			try {
				docTemp = domParse.parseDOM(new URL(htmlXml).openStream(), null);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		// Parsing XML
		} else {
			try {
				dom = builder.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		// Set up XPATH Engine
		XPathEngine engine = XPathEngineFactory.getXPathEngine();
		engine.setXPaths(xpaths);
		
		// Evaluating the document
		boolean[] evaluatedPaths = engine.evaluate(docTemp);
		
		// Getting the print writer
		OutputStream responseOutput = null;
		String responseText = "";
		
		try {
			responseOutput = response.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		responseText = "<!DOCTYPE html>"
		+ "<html lang=\"en\">\n"
		+ "<head>\n"
		+ "<title>Webcrawler Interface</title>\n"
		+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
		+ "<!-- Latest compiled and minified CSS -->\n"
		+ "<link rel=\"stylesheet\"\n"
		+ "href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">\n"
		+ "<!-- Latest compiled and minified JavaScript -->\n"
		+ "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>\n"
		+ "</head>\n"
		+ "<body>\n"
		+ "<div class=\"container\">\n"
		+ "<div class=\"row\">\n"
		+ "<div class=\"col-md-4\"></div>\n"
		+ "<div class=\"col-md-4\">\n";
		
		responseText += "<h1 style=\"text-align: center; margin-top: 50px; margin-bottom; 30px\">Webcrawler Results</h1>";
		
		
		
		int i = 0;
		for (boolean b : evaluatedPaths) {
			responseText += "<h4>Xpath: " + xpaths[i] + " is:" + b + "</h4>\n<br>\n";
			i++;
		}
		
		responseText += "<div class=\"control-group\" style=\"text-align: center; margin-top:20px; \">"
						+ "<button class=\"btn btn-primary\">Back to Crawler Interface</button>"
						+ "</div>";
		
		responseText += "</div>\n"
		+ "<div class=\"col-md-4\"></div>\n"
		+ "</div>\n"
		+ "</div>\n"
		+ "</body>\n"
		+ "</html>";
		
		System.out.println(responseText);
		
		try {
			responseOutput.write(responseText.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		// Eventually sent out display results
		System.out.println(response);

		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		
		// Displaying the interface
		File index = new File("workspace/HW2/WebContent/WEB-INF/index.html");
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









