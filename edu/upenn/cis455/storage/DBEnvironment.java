package edu.upenn.cis455.storage;

import java.io.File;
import java.io.IOException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBEnvironment {
	
	// store for data from webapp/crawler
	private static String crawlerDirectory = null;
	private static Environment crawlerEnv;
	private static EntityStore crawlerStore;
	
	public DBEnvironment(String path) {
		setup(path);
	}
	
	/**
	 * Initializes the BerkeleyDB instances for the crawlerDB and userDB
	 */
	public void setup(String path) {
		File data = new File(path);
		System.out.println(data.getAbsolutePath());
		if (!data.isFile()) {
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			// initializing the environment's config
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);	   // supports creation if doesn't already exist
			envConfig.setTransactional(true);  // supports transactions
			
			// initializing the store's config
			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);
			
			// creating DB and store instances
			crawlerEnv = new Environment(data, envConfig);
			crawlerStore = new EntityStore(crawlerEnv, "CrawlerStore", storeConfig);

		} catch (DatabaseException dbe) {
			System.out.println(dbe.toString());
			System.out.println(dbe.getStackTrace());
			System.exit(-1);
		}
	}
	
	/**
	 * Returns a handle to the crawlerStore
	 * @return EntityStore
	 */
	public EntityStore getCrawlerStore() {
		return crawlerStore;
	}
	
	/**
	 * Returns a handle to the crawlerEnv
	 * @return Environment
	 */
	public Environment getCrawlerEnv() {
		return crawlerEnv;
	}
	
	/**
	 * Shuts down the BerkeleyDB instances for crawlers and users
	 */
	public void shutdownDBs() {
		// shutting down crawlerStore
		if (crawlerStore != null) {
			try {
				crawlerStore.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing store: " + dbe.toString());
				System.exit(-1);
			}
		}

		// shutting down crawlerEnv
		if (crawlerEnv != null) {
			try {
				// Finally, close environment.
				crawlerEnv.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing crawlerEnv: " + dbe.toString());
				System.exit(-1);
			}
		}
	}
}
