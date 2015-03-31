package edu.upenn.cis455.storage;

import java.sql.Date;
//import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class ChannelEntity {
	
	HashMap<String, String> channelDocs = new HashMap<String, String>();
	HashMap<String, Date> docTimes = new HashMap<String, Date>();
//	SimpleDateFormat universalFormat = new SimpleDateFormat("YYYY-MM-DDThh:mm:ss");
	String channel;
	String xsl;
	
	@PrimaryKey
	String name;
	
//	@SecondaryKey(relate = Relationship.ONE_TO_MANY);
//	String name;
	
	public boolean addDocTime(String url, String d, long t) {
		if (d != null) {
			channelDocs.put(url, d);
			docTimes.put(url, new Date(t));
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setNameChannel(String s, String n, String url) {
		if (s != null) {
			channel = s;
			name = n;
			xsl = url;
			return true;
		} else {
			return false;
		}
	}
	
	public String getPath() {
		return channel;
	}
}
