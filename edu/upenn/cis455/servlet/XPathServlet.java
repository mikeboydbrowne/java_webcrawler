package edu.upenn.cis455.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		// Parse input values
		String htmlXml	= request.getParameter("html/xml");
		String xpath	= request.getParameter("xpath");
		
		System.out.println(htmlXml);
		System.out.println(xpath);
		
		// Check input values
		
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









