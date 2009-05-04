package org.mobicents.servlet.sip.testsuite.address;

import javax.servlet.sip.TelURL;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests for creating telUrl
 */
public class TelURLTest extends junit.framework.TestCase {

	static String[][] equal = {
			{"tel:+358-555-1234567;postd=pp22", "tel:+358-555-1234567;POSTD=PP22"},
			//they should be equivalent but jain sip performs a string comparison
//			{"tel:+358-555-1234567;postd=pp22;isub=1411", "tel:+358-555-1234567;isub=1411;postd=pp22"}
	};
	
	static String[][] different = {
			{"tel:+358-555-1234567;postd=pp23@foo.com;user=phone", "tel:+358-555-1234567;POSTD=PP22@foo.com;user=phone"}
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {
		SipFactories.initialize("gov.nist", true);
		sipFactory = new SipFactoryImpl(null);
	}
	
	private TelURL telUrl(String uri) throws Exception {
		return (TelURL) sipFactory.createURI(uri);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			TelURL uri1 = telUrl(equal[i][0]);
			TelURL uri2 = telUrl(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			TelURL uri1 = telUrl(different[i][0]);
			TelURL uri2 = telUrl(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
}