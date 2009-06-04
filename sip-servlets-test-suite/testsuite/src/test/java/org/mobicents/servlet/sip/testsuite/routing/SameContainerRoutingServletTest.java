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
package org.mobicents.servlet.sip.testsuite.routing;

import java.util.ArrayList;
import java.util.List;

import javax.sip.SipProvider;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test starts 1 sip servlets container 
 * on port 5070 that has 2 services Shootme and Shootist.
 * 
 * Then the shootist sends an INVITE that goes to Shootme.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SameContainerRoutingServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SameContainerRoutingServletTest.class);
	
	private static final int TIMEOUT = 20000;
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;		
	
	public SameContainerRoutingServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/notifier-servlet/src/main/sipapp",
				"notifier-context", 
				"notifier-service"));		
	}
	
	public void deployShootistApplication(List<ApplicationParameter> applicationParameterList) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/subscriber-servlet/src/main/sipapp");
		context.setName("subscriber-context");
		context.setPath("subscriber-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		for (ApplicationParameter applicationParameter : applicationParameterList) {
			context.addApplicationParameter(applicationParameter);	
		}
		assertTrue(tomcat.deployContext(context));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/routing/samecontainer-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, "127.0.0.1:5070");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();		
	}
	
	public void testSameContainerRouting() throws Exception {
		List<ApplicationParameter> appParamList = new ArrayList<ApplicationParameter>();
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("requestURI");
		applicationParameter.setValue("127.0.0.1:5070");
		appParamList.add(applicationParameter);
		deployShootistApplication(appParamList);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getAllMessagesContent().size() > 0);
		boolean dialogCompletedReceived =false;
		for (String messageContent : sender.getAllMessagesContent()) {
			if(messageContent.equalsIgnoreCase("dialogCompleted")) {
				dialogCompletedReceived = true;
			}
		}
		assertTrue(dialogCompletedReceived);
	}
	
	public void testSameContainerRoutingNo200ToNotify() throws Exception {
		List<ApplicationParameter> appParamList = new ArrayList<ApplicationParameter>();
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("requestURI");
		applicationParameter.setValue("127.0.0.1:5070");
		ApplicationParameter applicationParameter2 = new ApplicationParameter();
		applicationParameter2.setName("no200OKToNotify");
		applicationParameter2.setValue("true");
		appParamList.add(applicationParameter);
		appParamList.add(applicationParameter2);
		deployShootistApplication(appParamList);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getAllMessagesContent().size() > 0);
		boolean dialogCompletedReceived =false;
		for (String messageContent : sender.getAllMessagesContent()) {
			if(messageContent.equalsIgnoreCase("dialogCompleted")) {
				dialogCompletedReceived = true;
			}
		}
		assertTrue(dialogCompletedReceived);
	}	

	@Override
	public void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		logger.info("Test completed");
		super.tearDown();
	}


}
