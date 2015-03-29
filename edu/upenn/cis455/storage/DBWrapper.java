package edu.upenn.cis455.storage;

import org.w3c.dom.Document;

public class DBWrapper {
	
	private DBEnvironment env;
	private DataIndexer di;
	
	public DBWrapper(String path) {
		env = new DBEnvironment(path);
		di = new DataIndexer(env.getCrawlerStore());
	}
	
	public int putUser(String userName, String password) {
		UserEntity newUser = new UserEntity();
		newUser.setUserName(userName);
		newUser.setPassword(password);
		di.userData.put(newUser);
		return 1;
	}
	
	public int putDocument(String url, Document d) {
		CrawlerEntity newData = new CrawlerEntity();
		newData.setURL(url);
		newData.updateContent(d);
		di.crawlerData.put(newData);
		return 1;
	}
	
	public UserEntity getUser(String userName) {
		return di.userData.get(userName);
	}
	
	public CrawlerEntity getData(String url) {
		return di.crawlerData.get(url);
	}

}
