package edu.upenn.cis455.storage;

import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UserEntity {
	
	private String password;
	private Set<String> XPaths;
	private Set<String> channels;
	
	@PrimaryKey
	private String userName;
	
	
	public UserEntity(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	/**
	 * Sets the user's password
	 * @param String password
	 */
	public void setPassword(String password) {
		this.password = password;
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
}
