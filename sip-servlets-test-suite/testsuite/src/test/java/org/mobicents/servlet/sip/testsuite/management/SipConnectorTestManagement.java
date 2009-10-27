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
package org.mobicents.servlet.sip.testsuite.management;

import java.util.Iterator;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test is disabled because it needs to take JVM arguments and it doesn't work if they are set during the test :
 * -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
 * @author jean.deruelle@gmail.com
 *
 */
public class SipConnectorTestManagement extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SipConnectorTestManagement.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
	// the order is important here 
	private static final String[] CONNECTORS_TO_TEST = new String[]{
		"sipConnectorAdded",
		"sipConnectorRemoved",
	};	
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	
	
	public SipConnectorTestManagement(String name) {
		super(name);
		System.setProperty("com.sun.management.jmxremote", "");
		System.setProperty("com.sun.management.jmxremote.port", "8999");
		System.setProperty("com.sun.management.jmxremote.ssl", "false");
		System.setProperty("com.sun.management.jmxremote.authenticate", "false");
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();		
	}
	
	public void testShootme() throws Exception {
		logger.info("\nCreate an RMI connector client and " +
		 "connect it to the RMI connector server");
		StringBuffer urlString = new StringBuffer();
		urlString.append("service:jmx:rmi://localhost:");
		urlString.append(8999);
		urlString.append("/jndi/rmi://localhost:");
		urlString.append(8999);
		urlString.append("/jmxrmi");
           JMXServiceURL url = new JMXServiceURL(
        		   urlString.toString());
           JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

           
		ObjectName objectName = new ObjectName(serverName + ":type=Service,serviceName="+ serverName);
		// Get the Platform MBean Server
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        MBeanInfo mbeanInfo = mbsc.getMBeanInfo(objectName);
        MBeanOperationInfo[] operationInfos = mbeanInfo.getOperations();
        System.out.println("MBean Operations:");
        String[] operationNames = new String[operationInfos.length];
        for (int i = 0; i < operationInfos.length; i++) {
           System.out.println(i + ": " + operationInfos[i].getDescription() + " " +
        		   operationInfos[i].getSignature());
           operationNames[i] = operationInfos[i].getName();
        }
		
        SipConnector[] sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
//        Thread.sleep(TIMEOUT);
        assertEquals(1, sipConnectors.length);
        for (int i = 0; i < sipConnectors.length; i++) {
			logger.info(sipConnectors[i]);
		}
        
        SipConnector sipConnector = new SipConnector();
        sipConnector.setIpAddress("127.0.0.1");
        sipConnector.setPort(5071);
        sipConnector.setTransport(ListeningPoint.UDP);
        mbsc.invoke(objectName, "addSipConnector",new Object[] {sipConnector}, new String[]{SipConnector.class.getCanonicalName()});
        sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
        assertEquals(2, sipConnectors.length);
        
        mbsc.invoke(objectName, "removeSipConnector",new Object[] {"127.0.0.1", 5071, ListeningPoint.UDP}, new String[]{String.class.getCanonicalName(), int.class.getCanonicalName(), String.class.getCanonicalName()});
        sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
        assertEquals(1, sipConnectors.length);     
        
        Thread.sleep(TIMEOUT);
        
        Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(2, sender.getAllMessagesContent().size());
		for (int i = 0; i < CONNECTORS_TO_TEST.length; i++) {
			assertTrue(sender.getAllMessagesContent().contains(CONNECTORS_TO_TEST[i]));
		}	
	}
	
	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		logger.info("Test completed");
		super.tearDown();
	}


}
