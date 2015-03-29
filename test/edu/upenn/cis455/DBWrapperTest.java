package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import edu.upenn.cis455.storage.DBWrapper;

public class DBWrapperTest {
	
	// Universal wrapper instance
	DBWrapper dbInstance = new DBWrapper(".");
	
	@Test
	public void testPut() {
		assertEquals(dbInstance.putUser("mikeboydbrowne", "1234"), 1);
	}

}
