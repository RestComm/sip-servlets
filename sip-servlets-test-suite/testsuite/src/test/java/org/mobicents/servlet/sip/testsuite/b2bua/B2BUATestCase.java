package org.mobicents.servlet.sip.testsuite.b2bua;

import java.util.Hashtable;

import javax.sip.SipListener;
import javax.sip.SipProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.testsuite.ScenarioHarness;
import org.mobicents.servlet.sip.testsuite.simple.SimpleSipServletTest;

public class B2BUATestCase extends ScenarioHarness implements
		SipListener {
	
	private static Log logger = LogFactory.getLog(B2BUATestCase.class);

	
	protected Shootist shootist;

	
	protected Shootme shootme;


    
	

	// private Appender appender;

	public B2BUATestCase() {

		super("B2BUATest", true);

		try {
			providerTable = new Hashtable();

		} catch (Exception ex) {
			logger.error("unexpected exception", ex);
			fail("unexpected exception ");
		}
	}

	public void setUp() {

		try {
			super.setUp();
			this.shootist = new Shootist(5058, 5070, super.shootistProtocolObjects);
			SipProvider shootistProvider = shootist.createSipProvider();
			providerTable.put(shootistProvider, shootist);

			this.shootme = new Shootme(5059, shootmeProtocolObjects);
			SipProvider shootmeProvider = shootme.createProvider();
			providerTable.put(shootmeProvider, shootme);
			shootistProvider.addSipListener(this);
			shootmeProvider.addSipListener(this);

			

			shootistProtocolObjects.start();
			shootmeProtocolObjects.start();
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}
	
	public void doTest() {
		shootist.sendInvite();
	}

	
	public void tearDown() {
		try {
			Thread.sleep(4000);
			this.shootist.checkState();
			this.shootme.checkState();
			shootistProtocolObjects.destroy();
			shootmeProtocolObjects.destroy();
			Thread.sleep(2000);
			this.providerTable.clear();
			
			logger.info("Test completed");
		} catch (Exception ex) {
			logger.error("unexpected exception", ex);
			fail("unexpected exception ");
		}
	}


}
