package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase {
	public static Test suite() {
		try {
			Class[] testClasses = {
				Class.forName("XPathhEngineImplTest"),
				Class.forName("")
			};

			return new TestSuite(testClasses);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
