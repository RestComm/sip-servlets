package org.mobicents.servlet.sip.testsuite.b2bua;

import java.util.HashMap;

import javax.sip.SipListener;
import javax.sip.SipProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.testsuite.ScenarioHarness;

public class B2BUATestCase extends ScenarioHarness implements
		SipListener {
	
	private static Log logger = LogFactory.getLog(B2BUATestCase.class);

	protected Shootist shootist;
	
	protected Shootme shootme;

	public B2BUATestCase() {
		super("B2BUATest", true);
		providerTable = new HashMap<SipProvider, SipListener>();
	}

	public void setUp() throws Exception {
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
	}
	
	public void doTest() {
		shootist.sendInvite();
	}
	
	public void tearDown() throws InterruptedException {
		Thread.sleep(4000);
		this.shootist.checkState();
		this.shootme.checkState();
		shootistProtocolObjects.destroy();
		shootmeProtocolObjects.destroy();
		Thread.sleep(2000);
		this.providerTable.clear();
		
		logger.info("Test completed");
	}


}
