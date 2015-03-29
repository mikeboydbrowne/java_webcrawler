package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.upenn.cis455.storage.DBWrapper;

public class DBWrapperTest {
	
	// Universal wrapper instance
	DBWrapper dbInstance = new DBWrapper(".");
	
	@Test
	public void addUser() {
		assertEquals(dbInstance.putUser("mikeboydbrowne", "1234"), 0);
	}
	
	@Test
	public void testPersistance() {
		assertTrue(dbInstance.containsUser("mikeboydbrowne"));
	}
	
	@Test
	public void testPasswordAuth() {
		assertTrue(dbInstance.passwordAuth("mikeboydbrowne", "1234"));
	}
}
