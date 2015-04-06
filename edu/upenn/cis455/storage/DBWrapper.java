package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.tidy.*;

import java.security.SecureRandom;

public class DBWrapper {
	
	private DBEnvironment env;
	private DataIndexer di;
	private SecureRandom random = new SecureRandom();
	
	public DBWrapper(String path) {
		env = new DBEnvironment(path);
		di = new DataIndexer(env.getCrawlerStore());
	}
	
	/**
	 * Check if user is in the database
	 * @param userName - requested user's username
	 * @return true if contained, false if not
	 */
	public boolean containsUser(String userName) {
		if (userName != null) {
			return di.userData.contains(userName);
		} else {
			return false;
		}
	}
	
	/**
	 * Check if document at url is in database
	 * @param url - requested document's url
	 * @return true if contained, false if not
	 */
	public boolean containsDocument(String url) {
		if (url != null) {
			return di.crawlerData.contains(url);
		} else {
			return false;
		}
	}
	
	/**
	 * Put the user with username and password in the database
	 * @param userName - new user's username
	 * @param password - new user's password
	 * @return 1 if put was successful, 0 if record exists, -1 if error
	 */
	public int putUser(String userName, String password) {
		if (userName != null && password != null && !containsUser(userName)) {
			// create the UserEntity
			UserEntity newUser = new UserEntity();
			newUser.setUserName(userName);
			newUser.setPassword(password);
			
			// put the object in the database
			di.userData.put(newUser);
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Put the document at a given URL in the database
	 * @param url - requested URL
	 * @param d - document at the requested url
	 * @return 1 if put was successful, 0 if record exists, -1 if error
	 * 			
	 */
	public int putDocument(String url, Document d) {
		if(url != null && d != null && !containsDocument(url)) {
			// create the CrawlerEntity
			CrawlerEntity newData = new CrawlerEntity();
			newData.setURL(url);
			
			// transforming dom to string
			DOMSource domSource = new DOMSource(d);
			StringWriter writer = new StringWriter();
			StreamResult domStream = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = tf.newTransformer();
				transformer.transform(domSource, domStream);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			StringBuffer sb = writer.getBuffer(); 
			String stringifiedDom = sb.toString();
		    
		    // updating document
		    newData.updateContent(stringifiedDom);

			// put the object in the database
			di.crawlerData.put(newData);
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Get the UserEntity associated with the given username
	 * @param userName - userName to get
	 * @return the UserEntity object associated with the userName
	 */
	public UserEntity getUser(String userName) {
		if (userName != null && containsUser(userName)) {
		return di.userData.get(userName);
		} else {
			return null;
		}
	}
	
	/**
	 * Return the requested user's password
	 * @param userName - username whose password to get
	 * @return the user's password if user is contained, null if otherwise
	 */
	private String getPassword(String userName) {
		if (containsUser(userName)) {
			return di.userData.get(userName).getPassword();
		} else {
			return null;
		}
	}
	
	/**
	 * Return the XPaths requested by a particular user
	 * @param userName - username whose XPaths to get
	 * @return the user's requested XPaths if contained, null if otherwise
	 */
	public Set<String> getXPaths(String userName) {
		if (containsUser(userName)) {
			return di.userData.get(userName).getXPaths();
		} else {
			return null;
		}
	}
	
	public boolean addChannel(String userName, String channel) {
		if (containsUser(userName) && channel != null) {
			UserEntity updatedUser = di.userData.get(userName);
			di.userData.delete(userName);
			updatedUser.addChannel(channel);
			di.userData.put(updatedUser);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return the channels requested by a particular user
	 * @param userName - username whose channels to get
	 * @return the user's requested channels if contained, null if otherwise
	 */
	public Set<String> getChannels(String userName) {
		if (containsUser(userName)) {
			return di.userData.get(userName).getChannels();
		} else {
			return null;
		}
	}
	
	/**
	 * Return's the user's current sessionID
	 * @param userName - user whose sessionID to return
	 * @return
	 */
	public String getCurrentSession(String userName) {
		if (containsUser(userName)) {
			UserEntity data = di.userData.get(userName);
			return data.getSession();
		} else {
			return null;
		}
	}
	
	/**
	 * Set's a user's sessionID variable
	 * @param userName user whose session to set
	 */
	public void setCurrentSession(String userName) {
		if (containsUser(userName)) {
			String newID = new BigInteger(130, random).toString(32);
			UserEntity updatedUser = di.userData.get(userName);
			di.userData.delete(userName);
			updatedUser.setSession(newID);
			di.userData.put(updatedUser);
		}
	}
	
	/**
	 * Gets the set of currently used sessions
	 * @return Set<String> of session
	 */
	public Set<String> getSessions() {
		HashSet<String> sessions = new HashSet<String>();
		for (String u : di.userData.sortedMap().keySet()) {
			if (di.userData.get(u).getSession() != null) {
				sessions.add(di.userData.get(u).getSession());
			}
		}
		return sessions;
	}
	
	/**
	 * Checks validity of a session
	 * @param sessionID - session to check
	 * @return true/false depending on validity
	 */
	public boolean isValidSession(String sessionID) {
		return getSessions().contains(sessionID);
	}
	
	/**
	 * Gets a hashmap of sessionIDs => users
	 * @return
	 */
	public Map<String, String> getSessionsUsers() {
		Map<String, String> sessions = new HashMap<String,String>();
		for (String u : di.userData.sortedMap().keySet()) {
			if (di.userData.get(u).getSession() != null) {
				sessions.put(di.userData.get(u).getSession(), u);
			}
		}
		return sessions;
	}
	
	/**
	 * Checking if a user is logged in somewhere
	 * @param userName - user to check
	 * @return true/false depending on logged-in status
	 */
	public boolean isAuthed(String userName) {
		if (containsUser(userName)) {
			return di.userData.get(userName).getSession() != null;
		} else {
			return true;
		}
	}
	
	/**
	 * Checks the password of a given user
	 * @param userName - user whose password to check
	 * @param password - password of the user
	 * @return true if password is correct, false if not
	 */
	public boolean passwordAuth(String userName, String password) {
		if (containsUser(userName)) {
			return equals(password, getPassword(userName));
		} else {
			return false;
		}
	}
	
	/**
	 * Update a user's password
	 * @param userName - user whose password to check
	 * @param oldPass - old password of the user
	 * @param newPass - new password of the user
	 * @return true if passed, false if not
	 */
	public boolean passwordUpdate(String userName, String oldPass, String newPass) {
		if (containsUser(userName)) {
			if (passwordAuth(userName, oldPass)) {
				UserEntity updatedUser = getUser(userName);
				updatedUser.setPassword(newPass);
				di.userData.delete(userName);
				di.userData.put(updatedUser);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Deletes a user if the given password and username are correct
	 * @param userName - user to be deleted
	 * @param password - password of user to be deleted
	 * @return true if user deleted, false if not
	 */
	public boolean deleteUser(String userName, String password) {
		if (containsUser(userName) && passwordAuth(userName, password)) {
			di.userData.delete(userName);
			return !containsUser(userName);
		} else {
			return false;
		}
	}
	
	/**
	 * Get the CrawlerEntity associated with the given url
	 * @param url - url of document to get
	 * @return the CrawlerEntity object associated with the userName
	 */
	public CrawlerEntity getData(String url) {
		return di.crawlerData.get(url);
	}
	
	/**
	 * Get data associated with the URL in question
	 * @param url - url to get info from
	 * @return Document
	 */
	public Document getDocument(String url) {
		if (containsDocument(url)) {
			String stringifiedDom = di.crawlerData.get(url).getContent();
			InputStream domStream = new ByteArrayInputStream(stringifiedDom.getBytes(StandardCharsets.UTF_8));
			// Setting up JTidy
			Tidy domParse = new Tidy();
			domParse.setForceOutput(true);
			domParse.setShowErrors(0);
			domParse.setQuiet(true);
			return domParse.parseDOM(domStream, null);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the last time a document was accessed
	 * @param url - url to get long from
	 * @return the last accessed time, 0 if not included or access
	 */
	public long getLastAccess(String url) {
		if (containsDocument(url)) {
			return di.crawlerData.get(url).getLastAccessed();
		} else {
			return 0;
		}
	}
	
	/**
	 * Update the document associated with a given url
	 * @param url - url to update
	 * @param d - document to put in
	 * @return true if update was a success, false if not
	 */
	public boolean updateDocument(String url, Document d) {
		if (containsDocument(url)) {
			CrawlerEntity updatedData = di.crawlerData.get(url);
			
			// transforming dom to string
			DOMSource domSource = new DOMSource(d);
			StringWriter writer = new StringWriter();
			StreamResult domStream = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = tf.newTransformer();
				transformer.transform(domSource, domStream);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			StringBuffer sb = writer.getBuffer(); 
			String stringifiedDom = sb.toString(); 
					    
			// updating document
			updatedData.updateContent(stringifiedDom);
			
			// putting in store
			di.crawlerData.delete(url);
			di.crawlerData.put(updatedData);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Update the time of the crawler data
	 * @param url - url to be updated
	 * @param lastAccessed - time to put
	 * @return true if updated, false if not
	 */
	public boolean updateTime(String url, long lastAccessed) {
		if (containsDocument(url)) {
			CrawlerEntity updatedData = di.crawlerData.get(url);
			updatedData.updateTime(lastAccessed);
			di.crawlerData.delete(url);
			di.crawlerData.put(updatedData);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Update both the time and document associate with a url
	 * @param url - url to update
	 * @param lastAccessed - time to change
	 * @param d - document to change
	 * @return true if success, false if not
	 */
	public boolean updateTimeandDocument(String url, long lastAccessed, Document d) {
		if (containsDocument(url)) {
			CrawlerEntity updatedData = di.crawlerData.get(url);
			updatedData.updateTime(lastAccessed);
			
			// transforming dom to string
			DOMSource domSource = new DOMSource(d);
			StringWriter writer = new StringWriter();
			StreamResult domStream = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = tf.newTransformer();
				transformer.transform(domSource, domStream);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			StringBuffer sb = writer.getBuffer(); 
			String stringifiedDom = sb.toString();
								    
			// updating document
			updatedData.updateContent(stringifiedDom);
						
			// putting in store
			di.crawlerData.delete(url);
			di.crawlerData.put(updatedData);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Adds a channel with the given Xpaths
	 * @param s channel to start
	 * @return true if successful, false if not
	 */
	public boolean addChannel(String s, String n, String url) {
		if (s != null) {
			ChannelEntity newChannel = new ChannelEntity();
			newChannel.setNameChannel(s, n, url);
			di.channelData.put(newChannel);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Adds a document and its relevant information to the datastore
	 * @param n - name of channel
	 * @param url - url of document in question
	 * @param d - document to add
	 * @param t - time at which the document was added
	 * @return
	 */
	public boolean addDocumentTime(String n, String url, String d, long t) {
		if (n != null && d != null && di.channelData.contains(n)) {
			di.channelData.get(n).addDocTime(url, d, t);
			return true;
		} else {
			return false;
		}
	}
	
	public HashMap<String, String> getChannels() {
		HashMap<String,String> channels = new HashMap<String,String>();
		for (String n : di.channelData.sortedMap().keySet()) {
			channels.put(di.channelData.get(n).getPath(), n);
		}
		return channels;
	}
	
	
	/**
	 * Compares two strings character by character
	 * @param s1 - string1
	 * @param s2 - string2
	 * @return true if exactly equal, false if not
	 */
	private boolean equals(String s1, String s2) {
		boolean ret = false;
		int i = 0;
		char[]	s2Arr = s2.toCharArray();
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
