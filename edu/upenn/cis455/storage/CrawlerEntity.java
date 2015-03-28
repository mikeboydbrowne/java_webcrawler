package edu.upenn.cis455.storage;

import org.w3c.dom.Document;

import com.sleepycat.persist.model.PrimaryKey;

public class CrawlerEntity {
	
	private Document content;
	private long lastAccessed;
	
	@PrimaryKey
	private String url;
	
	
	public CrawlerEntity(String url, Document d) {
		this.url = url;
		this.content = d;
	}
	
	/**
	 * Updates the last accessed to the given long
	 * @param long
	 */
	public void updateTime(long time) {
		this.lastAccessed = time;
	}
	
	/**
	 * Updates the content associated with the url
	 * @param Document
	 */
	public void updateContent(Document d) {
		this.content = d;
	}
	
	/**
	 * Returns the time this url was last accessed
	 * @return long
	 */
	public long getLastAccessed() {
		return lastAccessed;
	}
	
	/**
	 * Return the content at the url address
	 * @return Document
	 */
	public Document getContent() {
		return content;
	}

}
