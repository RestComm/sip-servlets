package org.mobicents.servlet.sip.testsuite.address;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipURI;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
 */
public class SipURITest extends junit.framework.TestCase {

	static String[][] equal = {
			// should be escaped {"sip:%61lice@atlanta.com;transport=TCP", "sip:alice@AtlanTa.CoM;Transport=tcp"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;newparam=5"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;lr"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com;newparam=5"},
			{"sip:alice@atlanta.com?subject=project%20x&priority=urgent", "sip:alice@atlanta.com?priority=urgent&subject=project%20x"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;security=on"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com"}
	};
	
	static String[][] different = {
			{"sip:alice@atlanta.com", "sip:ALICE@atlanta.com"},
			{"sip:bob@biloxi.com", "sip:bob@biloxi.com:5060"},
			// should be different {"sip:bob@biloxi.com", "sip:bob@biloxi.com;transport=tcp"},
			{"sip:carol@chicago.com;newparam=6", "sip:carol@chicago.com;newparam=5"},
			//should be different {"sip:carol@chicago.com", "sip:carol@chicago.com?Subject=next%20meeting"},
			{"sip:carol@chicago.com?Subject=next%20meeting", "sip:carol@chicago.com?Subject=another%20meeting"},
			{"sip:carol@chicago.com;security=off", "sip:carol@chicago.com;security=on"}
	};
	
	private SipFactory sipFactory;
	
	public void setUp() {
		SipFactories.initialize("gov.nist");
		sipFactory = new SipFactoryImpl(null);
	}
	
	private SipURI sipUri(String uri) throws Exception {
		return (SipURI) sipFactory.createURI(uri);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			SipURI uri1 = sipUri(equal[i][0]);
			SipURI uri2 = sipUri(equal[i][1]);
			assertTrue(uri1.equals(uri2));
			assertTrue(uri2.equals(uri1));
		}
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			SipURI uri1 = sipUri(different[i][0]);
			SipURI uri2 = sipUri(different[i][1]);
			assertFalse(uri1.equals(uri2));
			assertFalse(uri2.equals(uri1));
		}
	}
}