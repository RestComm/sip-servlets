package org.mobicents.servlet.sip.testsuite.address;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
 */
public class SipURITest extends junit.framework.TestCase {

	static String[][] equal = {
			{"sip:%61lice@atlanta.com;transport=TCP", "sip:alice@AtlanTa.CoM;Transport=tcp"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;newparam=5"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;lr"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com;newparam=5"},
			{"sip:alice@atlanta.com?subject=project%20x&priority=urgent", "sip:alice@atlanta.com?priority=urgent&subject=project%20x"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;security=on"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com"},
			{"sip:[0:0:0:0:0:0:0:1%1]:5070;transport=udp", "sip:[0:0:0:0:0:0:0:1%1]:5070;transport=udp"}
	};
	
	static String[][] different = {
			{"sip:alice@atlanta.com", "sip:ALICE@atlanta.com"},
			{"sip:bob@biloxi.com", "sip:bob@biloxi.com:5060"},
			{"sip:bob@biloxi.com", "sip:bob@biloxi.com;transport=tcp"},
			{"sip:carol@chicago.com;newparam=6", "sip:carol@chicago.com;newparam=5"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com?Subject=next%20meeting"},
			{"sip:carol@chicago.com?Subject=next%20meeting", "sip:carol@chicago.com?Subject=another%20meeting"},
			{"sip:carol@chicago.com;security=off", "sip:carol@chicago.com;security=on"}
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {		
		SipFactories.initialize("gov.nist", true);		
		sipFactory = new SipFactoryImpl(null);
	}
	
	private SipURI sipUri(String uri) throws Exception {
		return (SipURI) sipFactory.createURI(uri);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			SipURI uri1 = sipUri(equal[i][0]);
			SipURI uri2 = sipUri(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			SipURI uri1 = sipUri(different[i][0]);
			SipURI uri2 = sipUri(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
	
	public void testEscaping() throws Exception {
		SipURI uri1 = sipUri("sip:%61lice@atlanta.com;transport=TCP");
		assertTrue(uri1.getUser() + " is different as alice" , uri1.getUser().equals("alice"));
		assertTrue(uri1.toString() + " is different as sip:alice@atlanta.com;transport=TCP" , uri1.toString().equals("sip:alice@atlanta.com;transport=TCP"));		
		uri1 = sipUri("sip:alice@example.com;transport=tcp?Subject=SIP%20Servlets");
		assertTrue(uri1.getHeader("Subject") + " is different as SIP Servlets" , uri1.getHeader("Subject").equals("SIP Servlets"));		
		assertTrue(uri1.toString() + " is different as sip:alice@example.com;transport=tcp?Subject=SIP Servlets" , uri1.toString().equals("sip:alice@example.com;transport=tcp?Subject=SIP Servlets"));
//		uri1 = sipUri("sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg%3bencode%3d314M-25/625-50");
//		SipURI uri2 = sipUri("sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg;encode=314M-25/625-50");		
//		assertTrue(uri1.getParameter("content-type") + " is different as video/mpeg;encode=314M-25/625-50" , uri1.getParameter("content-type").equals("video/mpeg;encode=314M-25/625-50"));
//		assertTrue(uri1.toString() + " is different as sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg%3bencode%3d314M-25/625-50" , uri1.equals(uri2));
	}	
	
	public void testNullUser() throws Exception {
		SipURI uri1 = sipUri("sip:atlanta.com;transport=TCP?Subject=SIP%20Servlets");
		assertNotNull(uri1);
		assertNull(uri1.getUser());
		assertNotNull(uri1.getHost());
		assertEquals("TCP", uri1.getTransportParam());
		assertTrue(uri1.getHeader("Subject") + " is different as SIP Servlets" , uri1.getHeader("Subject").equals("SIP Servlets"));
	}
	
	public void testParams() throws Exception {
		URI uri = sipFactory.createURI("sip:127.0.0.1:5080");
		uri.setParameter("Key", "val");
		String s = uri.toString();
		assertEquals("sip:127.0.0.1:5080;Key=val", s);
	}
}