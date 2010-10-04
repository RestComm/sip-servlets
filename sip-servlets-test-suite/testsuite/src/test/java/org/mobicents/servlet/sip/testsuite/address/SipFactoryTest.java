package org.mobicents.servlet.sip.testsuite.address;

import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * 
 */
public class SipFactoryTest extends junit.framework.TestCase {
	private SipFactoryImpl sipFactory;
	
	public void setUp() {
		SipFactories.initialize("gov.nist", true);
		sipFactory = new SipFactoryImpl(null);
	}
	
	/**
	 * Non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=1856
	 * Cannot create a parameterable header for Session-Expires
	 * 
	 * @throws ServletParseException
	 */
	public void testSessionExpires() throws ServletParseException {
		Parameterable p = sipFactory.createParameterable("3600");
		p.setParameter("refresher", "uac");
		System.out.println(p.toString());
	}
}