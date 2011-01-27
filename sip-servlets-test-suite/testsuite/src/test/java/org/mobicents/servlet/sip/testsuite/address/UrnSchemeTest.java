package org.mobicents.servlet.sip.testsuite.address;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

public class UrnSchemeTest extends junit.framework.TestCase {

	//Non regression test for issue 2289
	//https://code.google.com/p/mobicents/issues/detail?id=2289
	
	private SipFactoryImpl sipFactory;
	private javax.sip.address.AddressFactory jainAddressFactory;
	private String urn = "urn:service:sos";
	private String scheme = "urn";
	
	public void setUp() {		
		SipFactories.initialize("gov.nist", true);		
		sipFactory = new SipFactoryImpl(null);

		try {
			jainAddressFactory = SipFactory.getInstance().createAddressFactory();
		} catch (PeerUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void testJainAddress() throws Exception {
		javax.sip.address.Address urnJainAddr = jainAddressFactory.createAddress(urn); 
		assertTrue(urn.equalsIgnoreCase(urnJainAddr.getURI().toString()));
		assertTrue(scheme.equals(urnJainAddr.getURI().getScheme()));
	}
	
	public void testMobicentsAddress() throws Exception {
		javax.servlet.sip.Address urnMobicentsAddr = sipFactory.createAddress(urn);
		assertTrue(urn.equals(urnMobicentsAddr.getURI().toString()));
		assertTrue(scheme.equals(urnMobicentsAddr.getURI().getScheme()));
	}

}
