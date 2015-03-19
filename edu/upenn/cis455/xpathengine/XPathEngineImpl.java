package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;

public class XPathEngineImpl implements XPathEngine {
	
	String[] 	xpaths;
	boolean[] 	evaluatedPaths 	= null;
	boolean		pathsEvaled 	= false;
	boolean		xpathsSet		= false;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] s) {
		// Initializing XPaths array
		xpaths = new String[s.length];
		
		// Stripping whitespace from XPaths
		for (int i = 0; i < s.length; i++) {
			xpaths[i] = stripWhitespace(s[i]);
		}
		
		// XPaths have been set
		xpathsSet 	= true;
	}

	public boolean isValid(int i) {
		// Check to see if within bounds 
		if (!xpathsSet || i < 0 || i >= xpaths.length) {
			return false;
		
		// Traversing the DOM looking for 
		} else {
			return traverseDOM(xpaths[i]);
		}
	}

	public boolean[] evaluate(Document d) {
		
		// Checking all XPath matches
		boolean[] retArr = new boolean[xpaths.length];
		for (int i = 0; i < xpaths.length; i++) {
			retArr[i] = traverseDOM(xpaths[i]);
		}
		
		// Updating class variables
		pathsEvaled 	= true;
		evaluatedPaths 	= retArr;
		
		// Returning boolean array
		return retArr;
	}
	
	// Traverses the DOM given an XPATH and returns true/false depending on validity
	private boolean traverseDOM(String path) {
		return false;
	}
	
	// Checks to see if test value is one of those in limited grammar
//	private boolean isValidTest(String s) {
//		
//	}
	
	// Strips whitespace from input string unless between quotation marks
	private String stripWhitespace(String s) {
		
		// Non-null check
		if (s != null) {
			String ret = "";
			boolean betweenQuotes = false;
			
			// Adding all relevant characters
			for (char c : s.toCharArray()) {
				if (!betweenQuotes) {
					if (c == ' ' || c == '\"') {
						if (c == '\"') {
							ret += c;
						}
					} else {
						ret += c;
					}
				} else {
					ret += c;
				}
			}
			return ret;
		} else {
			return "";
		}
	}
        
}
