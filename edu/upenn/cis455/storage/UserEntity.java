package edu.upenn.cis455.storage;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UserEntity {
	
	private String password;
	private Set<String> XPaths = new HashSet<String>();
	private Set<String> channels = new HashSet<String>();
	private String currentSession = null;
	
	@PrimaryKey
	private String userName;
	
	
//	public UserEntity(String userName, String password) {
//		this.userName = userName;
//		this.password = password;
//	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Sets the user's password
	 * @param String password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setSession(String sessionID) {
		this.currentSession = sessionID;
	}
	
	public void addChannel(String channel) {
		this.channels.add(channel);
	}
	
	/**
	 * Returns the user's username
	 * @return String
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Returns the user's passowrd
	 * @return String
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns the user's set of requested XPaths
	 * @return Set<String> 
	 */
	public Set<String> getXPaths() {
		return XPaths;
	}
	
	/**
	 * Returns the user's set of crawler channels 
	 * @return Set<String>
	 */
	public Set<String> getChannels() {
		return channels;
	}
	
	public String getSession() {
		return currentSession;
	}
}
