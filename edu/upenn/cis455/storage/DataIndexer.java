package edu.upenn.cis455.storage;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class DataIndexer {
	
	PrimaryIndex<String,CrawlerEntity> crawlerData;
	PrimaryIndex<String,UserEntity> userData;
	PrimaryIndex<String,ChannelEntity> channelData;
	
	public DataIndexer(EntityStore store) {
		// Getting the primary index for data from crawler
		crawlerData = store.getPrimaryIndex(String.class, CrawlerEntity.class);
		
		// Getting the primary index for data from users
		userData = store.getPrimaryIndex(String.class, UserEntity.class);
		
		// Getting the primary index for data from channels
		channelData = store.getPrimaryIndex(String.class, ChannelEntity.class);
	}

}
