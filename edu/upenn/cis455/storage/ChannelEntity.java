package edu.upenn.cis455.storage;

import java.util.HashSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class ChannelEntity {
	
	HashSet<String> channelDocs;
	
	@PrimaryKey
	String channel;

}
