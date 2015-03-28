package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;;

public class XPathEngineImplTest {

	@Test
	public void testCheckTestBody_Simple() {
//		assertTrue(checkTestBody("hello how are you?"));
	}
	
	public void testCheckTestBody_Quotation() {
//		assertTrue(checkTestBody("hello\"how are you"));
	}
	
	public void testIsValidTest_Simple() {
//		assertFalse(isValidTest("asdfasdf"));	
	}
	
	public void testIsValidTest_TextComma() {
//		assertFalse(isValidTest("[text(),\""));
	}
	
	public void testIsValidTest_ContainsEquals() {
//		assertFalse(isValidTest("[contains(text()=\""));
	}
	

}
