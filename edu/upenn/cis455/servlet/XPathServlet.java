package edu.upenn.cis455.servlet;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.*;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserEntity;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	DBWrapper dbInstance = new DBWrapper("/home/cis455/workspace/HW2/data/");
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String reqPath = request.getPathInfo();
		System.out.println(reqPath);
		// 
		if (reqPath.equalsIgnoreCase("/")) {
			redirectRoot(response);
		// validating a user
		} else if (reqPath.equalsIgnoreCase("/login")) {
			String userName = request.getParameter("username");
			String password = request.getParameter("pword");
			
			// redirect to user and give it session if validated
			if (dbInstance.passwordAuth(userName, password)) {
				dbInstance.setCurrentSession(userName);
				String currentSession = dbInstance.getCurrentSession(userName);
				Cookie sessionID = new Cookie("sessionID", currentSession);
				response.addCookie(sessionID);
				redirectUser(response);
			
			// redirect to root if not validated
			} else {
				redirectRoot(response);
			}

		// creating a new user
		} else if (reqPath.equalsIgnoreCase("/createAccount")) {
			String userName = request.getParameter("username");
			String password = request.getParameter("pword");
			String passwordConf = request.getParameter("pwordConf");
			
			// add a new user, if conf + password match
			if (equals(password, passwordConf) && !dbInstance.containsUser(userName)) {
				dbInstance.putUser(userName, password);
				dbInstance.setCurrentSession(userName);
				String currentSession = dbInstance.getCurrentSession(userName);
				Cookie sessionID = new Cookie("sessionID", currentSession);
				response.addCookie(sessionID);
				
				// redirect to user page
				redirectUser(response);
			
			// redirect if conf + password don't match
			} else {
				redirectRoot(response);
			}
		
		// user adds channel
		} else if (reqPath.equalsIgnoreCase("/addChannel")) {
			// checking if session id
			Cookie[] cookies = request.getCookies();
			String sessionId = "";
			for (Cookie c : cookies) {
				if (c.getName().equalsIgnoreCase("sessionID"))
					sessionId = c.getValue();
			}
			// if none or non-valid session id
			if (!dbInstance.isValidSession(sessionId)) {
				
			} else {
				// getting data
				String name = request.getParameter("name");
				String xml = request.getParameter("xpaths");
				String url = request.getParameter("url");
				
				Map<String,String> sessionsUsers = dbInstance.getSessionsUsers();
				
				// adding channel to DB
				dbInstance.addChannel(name, xml, url);
				dbInstance.addChannel(sessionsUsers.get(sessionId), name);
				
				// refreshing user page
				redirectUser(response);
			}

		// directing to root if post to logout
		} else if (reqPath.equalsIgnoreCase("/logout")) {
			redirectRoot(response);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		String reqPath = request.getPathInfo();
		System.out.println(reqPath);
		
		// root
		if (reqPath.equalsIgnoreCase("/")) {
			// checking if session id
			Cookie[] cookies = request.getCookies();
			String sessionId = "";
			for (Cookie c : cookies) {
				if (c.getName().equalsIgnoreCase("sessionID"))
					sessionId = c.getValue();
			}
			
			// if none or non-valid session id
			if (!dbInstance.isValidSession(sessionId)) {
				// Displaying home interface
				addFile(response, "workspace/HW2/WebContent/WEB-INF/index.html");
			
			// if valid session id, redirect
			} else {
				redirectUser(response);
			}
		
		// createAccount
		} else if (reqPath.startsWith("/createAccount")) {
			// Displaying createAccount interface
			addFile(response, "workspace/HW2/WebContent/WEB-INF/createAccount.html");
			
		// channels
		} else if (reqPath.startsWith("/channels")) {
			
			// displaying first part of interface
			addFile(response, "workspace/HW2/WebContent/WEB-INF/channels1.html");

			// adding channel input
			HashMap<String,String> channels = dbInstance.getChannels();
			
			// no channels
			if (channels.isEmpty()) {
				try {
					response.getOutputStream().write("<div style=\"text-align:center;\">There appear to be no channels in the app</div>".getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			// filling in channels
			} else {
				int i = 1;
				for (String n : channels.keySet()) {
					String newChannel = "<div style=\"text-align: center\">"
											+ "<div><h4><strong>Channel #" + i +":</strong></h4></div>"
											+ "<div style=\"margin-top:-5px; margin-bottom:5px\">Name: " + n + "<br>XPaths: " + channels.get(n) + "</div>"
											+ "<div style=\"display:inline-block; margin-bottom: 10px\">"
												+ "<button class=\"btn btn-success\" style=\"margin-left: 20px;\">View</button>"
											+ "</div>"
										+ "</div>";
					try {
						response.getOutputStream().write(newChannel.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
					i++;
				}
			}
			
			// Displaying second part of interface
			addFile(response, "workspace/HW2/WebContent/WEB-INF/channels2.html");
		// /user
		} else if (reqPath.startsWith("/user")) {
			// checking if session id
			Cookie[] cookies = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				String sessionId = "";
				for (Cookie c : cookies) {
					if (c.getName().equalsIgnoreCase("sessionID"))
						sessionId = c.getValue();
				}
				System.out.println(sessionId);
				// output user page if session id is valid
				if (dbInstance.isValidSession(sessionId)) {
					Map<String,String> stringsUsers = dbInstance.getSessionsUsers();
					String user = stringsUsers.get(sessionId);
					System.out.println(user);
					UserEntity currentUser = dbInstance.getUser(user);
					// Displaying home interface
					addFile(response, "workspace/HW2/WebContent/WEB-INF/user1.html");
					addFile(response, "workspace/HW2/WebContent/WEB-INF/user2.html");
					
					// Adding channel input
					HashMap<String,String> channels = dbInstance.getChannels();
					
					// no channels
					if (channels.isEmpty()) {
						try {
							response.getOutputStream().write("<div style=\"text-align:center;\">There appear to be no channels in the app</div>".getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
					
					// channels
					} else {
						Set<String> userChannels = dbInstance.getChannels(user);
						int i = 1;
						for (String n : channels.keySet()) {
							String newChannel = "<div style=\"text-align: center\">"
													+ "<div><h4><strong>Channel #" + i +":</strong></h4></div>"
													+ "<div style=\"margin-top:-5px; margin-bottom:5px\">Name: " + n + "<br>XPaths: " + channels.get(n) + "</div>"
													+ "<div style=\"display:inline-block; margin-bottom: 10px\">";
							if (userChannels.contains(n)) {
														newChannel += "<button class=\"btn btn-danger\" style=\"margin-left: 20px;\">Delete</button>";
							}
							newChannel += "<button class=\"btn btn-success\" style=\"margin-left: 20px;\">View</button>"
										+ "</div>"
										+ "</div>";
							try {
								response.getOutputStream().write(newChannel.getBytes());
							} catch (IOException e) {
								e.printStackTrace();
							}
							i++;
						}
					}
					
					// Adding end of file
					addFile(response, "workspace/HW2/WebContent/WEB-INF/user3.html");
				// redirect to home if not
				} else {
					redirectRoot(response);
				}
			} else {
				redirectRoot(response);
			}
			
		} else if (reqPath.startsWith("/channels/view/")) {
			
		} else if (reqPath.startsWith("/channels/delete/")) {
			
		} else if (reqPath.startsWith("/login")) {
			redirectRoot(response);

		// redirect to root if don't recognize
		} else if (reqPath.equalsIgnoreCase("/logout")) {
			Cookie[] cookies = request.getCookies();
			Cookie sessionID = null;
			for (Cookie c : cookies) {
				if (c.getName().equalsIgnoreCase("sessionID"))
					sessionID = c;
			}
			sessionID.setValue(null);
			response.addCookie(sessionID);
			Object test = (String) request.getAttribute("sessionID");
			System.out.println(test);
			redirectRoot(response);
		
		// redirect to root if don't recognize
		} else {
			redirectRoot(response);
		}
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	private String standardizeReq(String req) {
		if (req.startsWith("http://")) {
			return req;
		} else if (req.startsWith("www.")) {
			return "http://" + req;
		} else if (req.startsWith("http://www.")) {
			return req;
		} else {
			return "http://www." + req;
		}
	}
	
	private void addFile(HttpServletResponse response, String filename) {
		// Displaying home interface
		File index = new File(filename);
		FileInputStream fileInput = null;
		try {
			fileInput = new FileInputStream(index);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int content;
		try {
			while ((content = fileInput.read()) != -1) {
				response.getOutputStream().write(content);
			}
			fileInput.close(); // Closing the file stream
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void redirectUser(HttpServletResponse response) {
		try {
			response.getOutputStream().write("<script>window.location.replace(\"/HW2/xpath/user\");</script>".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void redirectRoot(HttpServletResponse response) {
		try {
			response.getOutputStream().write("<script>window.location.replace(\"/HW2/xpath/\");</script>".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
