package edu.upenn.cis455.storage;

public class DBWrapper {
	
	private DBEnvironment env;
	private DataIndexer di;
	
	public DBWrapper(String path) {
		env = new DBEnvironment(path);
		di = new DataIndexer(env.getCrawlerStore());
	}
	
	// Get user & user data + manipulate it
	
	// Get crawler data + manipulate it

}
