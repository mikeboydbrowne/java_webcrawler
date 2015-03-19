package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;

public class XPathEngineImpl implements XPathEngine {
	
	String[] 	xpaths 			= null;
	boolean[] 	evaluatedPaths 	= null;
	boolean		pathsEvaled 	= false;
	boolean		xpathsSet		= false;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] s) {
		xpaths 		= s;
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
	
	private boolean traverseDOM(String path) {
		return false;
	}
        
}
