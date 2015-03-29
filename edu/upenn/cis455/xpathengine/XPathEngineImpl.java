package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathEngineImpl implements XPathEngine {
	String[] xpaths;
	boolean[] evaluatedPaths = null;
	Document DOM = null;
	Node root = null;
	boolean rootSet = false;
	boolean domSet = false;
	boolean pathsEvaled = false;
	boolean xpathsSet = false;

	public XPathEngineImpl() {
	}

	public void setXPaths(String[] s) {
		// Initializing XPaths array
		xpaths = new String[s.length];
		// Stripping whitespace from XPaths
		for (int i = 0; i < s.length; i++) {
			xpaths[i] = stripWhitespace(s[i]);
		}
		// XPaths have been set
		xpathsSet = true;
	}

	public boolean isValid(int i) {
		// Check to see if within bounds
		if (!xpathsSet || i < 0 || i >= xpaths.length || !domSet) {
			return false;
			// Traversing the DOM looking for
		} else {
			return traverseDOM(xpaths[i], DOM);
		}
	}

	public boolean[] evaluate(Document d) {
		// Setting DOM
		DOM = d;
		domSet = true;
		if (d.getParentNode() == null && !rootSet) {
			rootSet = true;
			root = d.getDocumentElement();
		}
		// Checking all XPath matches
		boolean[] retArr = new boolean[xpaths.length];
		for (int i = 0; i < xpaths.length; i++) {
			retArr[i] = traverseDOM(xpaths[i], root);
		}
		// Updating class variables
		pathsEvaled = true;
		evaluatedPaths = retArr;
		// Returning boolean array
		return retArr;
	}

	// Traverses the DOM given an XPATH and returns true/false depending on
	// validity
	private boolean traverseDOM(String path, Node n) {
		// Splitting the path
		String step = path.split("/")[1];
		String pathLeft = path.substring(step.length() + 1);
		// Examine node and it's siblings
		Node testNode = null;
		NodeList children = n.getChildNodes();
		// Checking if step is a test step or not
		if (isTest(step)) {
			System.out.println("This is a test");
			// Step is a well formatted test
			if (isValidTest(step)) {
				System.out.println("This is a VALID Test");
				if (runTest(step, n)) {
					if (pathLeft.length() == 0) {
						return true;
					} else {
						for (int i = 0; i < children.getLength(); i++) {
							if (traverseDOM(pathLeft, testNode)) {
								return true;
							}
						}
					}
				} else {
					return false;
				}
				// Step is a poorly formatted test
			} else {
				System.out.println("This is an INVALID Test");
				return false;
			}
			// Step is not a test
		} else {
			// Iterating through each of the node's children
			for (int i = 0; i < children.getLength(); i++) {
				testNode = children.item(i);
				if (pathLeft.length() == 0) {
					if (equals(n.getLocalName(), step)) {
						return true;
					} else {
						return false;
					}
				} else {
					if (equals(n.getLocalName(), step)) {
						if (traverseDOM(pathLeft, testNode)) {
							return true;
						}
					}
				}
			}
		}
		// If node & siblings don't match, false
		return false;
	}

	public boolean isTest(String s) {
		return s.contains("[");
	}

	public boolean isValidTest(String s) {
		boolean isValid = true;
		if (s.contains("[") && s.contains("]")) {
			if (s.startsWith("[text()=\"") && s.endsWith("\"]")) {
				String testBody = s.substring(9);
				testBody = testBody.substring(0, testBody.length() - 2);
				isValid = checkTestBody(testBody);
			} else if (s.startsWith("[contains(text(),\"")
					&& s.endsWith("\")]")) {
				String testBody = s.substring(18);
				testBody = testBody.substring(0, testBody.length() - 3);
				isValid = checkTestBody(testBody);
			} else if (s.startsWith("[@attname=\"") && s.endsWith("\"]")) {
				String testBody = s.substring(12);
				testBody = testBody.substring(0, testBody.length() - 2);
				isValid = checkTestBody(testBody);
			} else {
				isValid = false;
			}
		} else {
			isValid = false;
		}
		return isValid;
	}

	public boolean checkTestBody(String s) {
		boolean isValid = true;
		// making sure quotations are parsed out
		// Non-null check
		if (s != null) {
			boolean escaped = false;
			// Adding all relevant characters
			for (char c : s.toCharArray()) {
				if (c == '"' || c == '\\') {
					if (c == '\\') {
						if (!escaped) {
							escaped = !escaped;
						}
					} else if (c == '"') {
						if (!escaped) {
							isValid = false;
						}
					}
				}
			}
		}
		return isValid;
	}

	public boolean runTest(String s, Node n) {
		if (s.startsWith("[contains(text(),\"")) {
			String testBody = s.substring(18);
			testBody = testBody.substring(0, testBody.length() - 3);
			String nodeData = n.getNodeValue();
			return nodeData.contains(testBody);
		} else if (s.startsWith("[text()=\"")) {
			String testBody = s.substring(9);
			testBody = testBody.substring(0, testBody.length() - 2);
			String nodeData = n.getNodeValue();
			return equals(nodeData, testBody);
		} else if (s.startsWith("[@attname=\"")) {
			String testBody = s.substring(11);
			testBody = testBody.substring(0, testBody.length() - 2);
			NamedNodeMap attrs = n.getParentNode().getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				if (equals(attrs.item(i).getLocalName(), testBody)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	// Strips whitespace from input string unless between quotation marks
	public String stripWhitespace(String s) {
		// Non-null check
		if (s != null) {
			String ret = "";
			boolean betweenQuotes = false;
			boolean escaped = false;
			// Adding all relevant characters
			for (char c : s.toCharArray()) {
				if (!betweenQuotes) {
					if (c == ' ' || c == '"' || c == '\\' || c == '[') {
						if (c == '"') {
							if (!escaped) {
								betweenQuotes = !betweenQuotes;
							}
							ret += c;
						} else if (c == '\\') {
							escaped = true;
							ret += c;
						} else if (c == ' ' && betweenQuotes) {
							ret += c;
						} else if (c == '[' && !betweenQuotes) {
							if (escaped) {
								ret += '/';
								ret += c;
							} else {
								ret += c;
							}
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

	private boolean equals(String s1, String s2) {
		boolean ret = false;
		int i = 0;
		char[] s2Arr = s2.toCharArray();
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
	}
}