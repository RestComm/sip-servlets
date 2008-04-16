/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
