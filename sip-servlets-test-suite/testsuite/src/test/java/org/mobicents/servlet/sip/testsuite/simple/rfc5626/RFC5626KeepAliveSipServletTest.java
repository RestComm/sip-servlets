/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.testsuite.simple.rfc5626;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipProvider;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Added for Issue 2254 http://code.google.com/p/mobicents/issues/detail?id=2254
 * Testing keepalive section 4.4.1
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class RFC5626KeepAliveSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(RFC5626KeepAliveSipServletTest.class);

	private static final String TRANSPORT = "tcp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 30000;
//	private static final int TIMEOUT = 100000000;
	
	SipEmbedded tomcatShootist;
	
	TestSipListener receiver;
	SipProvider receiverProvider = null;
	ProtocolObjects receiverProtocolObjects;

	private Connector shootistConnector;
	
//	TestSipListener registerReciever;	
//	ProtocolObjects registerRecieverProtocolObjects;	
	
	public RFC5626KeepAliveSipServletTest(String name) {
		super(name);
		listeningPointTransport = ListeningPoint.TCP;
	}

	@Override
	public void deployApplication() {
		
	}
		
	
	public SipStandardContext deployShootme(Map<String, String> params) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp");
		context.setName("shootme-context");
		context.setPath("shootme-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setConcurrencyControlMode(ConcurrencyControlMode.SipApplicationSession);
		context.setManager(new SipStandardManager());
		for (Entry<String, String> param : params.entrySet()) {
			ApplicationParameter applicationParameter = new ApplicationParameter();
			applicationParameter.setName(param.getKey());
			applicationParameter.setValue(param.getValue());
			context.addApplicationParameter(applicationParameter);
		}
		assertTrue(tomcat.deployContext(context));
		return context;
	}

	
	public SipStandardContext deployShootist(Map<String, String> params) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("shootist-context");
		context.setPath("shootist-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setConcurrencyControlMode(ConcurrencyControlMode.SipApplicationSession);
		context.setManager(new SipStandardManager());
		for (Entry<String, String> param : params.entrySet()) {
			ApplicationParameter applicationParameter = new ApplicationParameter();
			applicationParameter.setName(param.getKey());
			applicationParameter.setValue(param.getValue());
			context.addApplicationParameter(applicationParameter);
		}
		assertTrue(tomcatShootist.deployContext(context));
		return context;
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		tomcatShootist = new SipEmbedded("tomcatShootist", serviceFullClassName);
		tomcatShootist.setLoggingFilePath(				
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + tomcatShootist.getLoggingFilePath());
		String darConfigurationFile = getDarConfigurationFile();
		tomcatShootist.setDarConfigurationFilePath(darConfigurationFile);
		tomcatShootist.initTomcat(darConfigurationFile, getSipStackProperties("mss-Shootist"));
		tomcatShootist.startTomcat();
		shootistConnector = tomcatShootist.addSipConnector("tomcatShootist", System.getProperty("org.mobicents.testsuite.testhostaddr"), 5090, listeningPointTransport);
		
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, "4", "true");
		receiver = new TestSipListener(5081, 5070, receiverProtocolObjects, true);
		receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();						
	}
	
	public void testNoTimeout() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		Map<String, String> params = new HashMap<String, String>();		
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "100");
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(0, receiver.getAllMessagesContent().size());
	}
	
	public void testShootmeTimeout() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		Map<String, String> params = new HashMap<String, String>();		
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "1");
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue("shootme onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootme onKeepAliveTimeout"));
		assertEquals(1, receiver.getAllMessagesContent().size());
	}
	
	public void testShootistTimeout() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		Map<String, String> params = new HashMap<String, String>();		
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "100");
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT/2);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue("shootist onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootist onKeepAliveTimeout"));
		assertEquals(1, receiver.getAllMessagesContent().size());
	}
	
	// making sure that we don't receive one timeout per Connection Based Transport Connector 
	public void testShootistTimeoutMultipleListeningPoints() throws Exception {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		tomcat.addSipConnector(serverName, "" + System.getProperty("org.mobicents.testsuite.testhostaddr"), 5070, ListeningPoint.TLS);
		tomcatShootist.addSipConnector(serverName, "" + System.getProperty("org.mobicents.testsuite.testhostaddr"), 5090, ListeningPoint.TLS);
		Map<String, String> params = new HashMap<String, String>();		
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "100");
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT/2);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue("shootist onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootist onKeepAliveTimeout"));
		assertEquals(1, receiver.getAllMessagesContent().size());
	}
	
	public void testShootistModifyKeepAliveTimeout() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		Map<String, String> params = new HashMap<String, String>();		
		params.put("changeKeepAliveTimeout", "1000");
		params.put("timeout", "" + TIMEOUT);
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "100");
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		assertEquals(0, receiver.getAllMessagesContent().size());		
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue("shootme onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootme onKeepAliveTimeout"));
		assertTrue("shootist onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootme onKeepAliveTimeout"));
		assertTrue(receiver.getAllMessagesContent().size()>0);
	}
	
	public void testShootistCloseReliableChannel() throws Exception {
		((SIPTransactionStack)tomcat.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		((SIPTransactionStack)tomcatShootist.getSipService().getSipStack()).setReliableConnectionKeepAliveTimeout(2200);
		
		Map<String, String> params = new HashMap<String, String>();				
		deployShootme(params);
		params = new HashMap<String, String>();
		params.put("host",  "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + sipConnector.getPort()+";transport=tcp;");
		params.put("noBye", "true");
		params.put("testKeepAlive", "100");
		if(!((SipStackImpl)tomcatShootist.getSipService().getSipStack()).getMessageProcessorFactory().getClass().getName().equals(NioMessageProcessorFactory.class.getName())) {			
			params.put("closeReliableChannel", "true");
			params.put("timeout", "" + TIMEOUT);
		} else {
			logger.debug("Nio setup we will kill the connection through removal of connector to avoid the retry mechanism false positivie");
		}
		deployShootist(params);
		Thread.sleep(TIMEOUT);
		if(((SipStackImpl)tomcatShootist.getSipService().getSipStack()).getMessageProcessorFactory().getClass().getName().equals(NioMessageProcessorFactory.class.getName())) {
			logger.debug("killing the connection through removal of connector to avoid the retry mechanism false positivie");
			tomcatShootist.removeConnector(shootistConnector);
		}
		assertEquals(0, receiver.getAllMessagesContent().size());		
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue("shootme onKeepAliveTimeout", receiver.getAllMessagesContent().contains("shootme onKeepAliveTimeout"));
		assertEquals(1, receiver.getAllMessagesContent().size());
	}
	
	@Override
	protected Properties getSipStackProperties() {
		return getSipStackProperties(getName());
	}
	
	protected Properties getSipStackProperties(String name) {
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
		"true");
		sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");
		sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP, 
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-debug.txt");
		sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-messages.xml");
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + name);
		sipStackProperties.setProperty(SipStandardService.AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");		
		sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "1");
		sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
		sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");		
		return sipStackProperties;
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();	
//		registerRecieverProtocolObjects.destroy();
		tomcatShootist.stopTomcat();
		logger.info("Test completed");
		super.tearDown();
	}


}
