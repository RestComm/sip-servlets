package org.mobicents.servlet.sip.testsuite.address;

import javax.servlet.sip.Address;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
 */
public class AddressTest extends junit.framework.TestCase {

	static String[][] equal = {
			{"\"Alice\" <sip:%61lice@bea.com;transport=TCP;lr>;q=0.6;expires=3601", "\"Alice02\" <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
			{"<sip:%61lice@bea.com;transport=TCP;lr>;expires=3601;q=0.6", " <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
			{"<sip:%61lice@bea.com;transport=TCP;lr>;q=0.6", "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"}
	};
	
	static String[][] different = {
			{"<sip:%61lice@bea.com;transport=TCP;lr>;q=0.5", "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {
		SipFactories.initialize("gov.nist", true);
		sipFactory = new SipFactoryImpl(null);
	}
	
	private Address address(String address) throws Exception {
		return (Address) sipFactory.createAddress(address);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			Address uri1 = address(equal[i][0]);
			Address uri2 = address(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			Address uri1 = address(different[i][0]);
			Address uri2 = address(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
}