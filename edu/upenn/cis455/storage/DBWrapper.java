package edu.upenn.cis455.storage;

import java.util.Set;

import org.w3c.dom.Document;

public class DBWrapper {
	
	private DBEnvironment env;
	private DataIndexer di;
	
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
		if(url != null && d != null && containsDocument(url)) {
			// create the CrawlerEntity
			CrawlerEntity newData = new CrawlerEntity();
			newData.setURL(url);
			newData.updateContent(d);

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
