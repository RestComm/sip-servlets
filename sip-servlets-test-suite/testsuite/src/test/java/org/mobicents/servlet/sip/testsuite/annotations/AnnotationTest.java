package org.mobicents.servlet.sip.testsuite.annotations;

import junit.framework.Assert;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TooManyListenersException;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.message.Response;

import org.apache.catalina.realm.MemoryRealm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;

public class AnnotationTest extends SipServletTestCase implements SipListener {

	private static Log logger = LogFactory.getLog(AnnotationTest.class);

	protected Hashtable providerTable = new Hashtable();

	private SipStack sipStackTracker;

	//private SipPhone sipPhoneTracker;
	
	private Tracker tracker;

	public AnnotationTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		//init();
		tracker = new Tracker();
		super.setUp();
	}

	public void testSecurity() {
		tracker.init();
		try {
			for(int q=0; q<10; q++)
			{
				if(tracker.receivedInvite) break;
				Thread.sleep(1111);
			}
			if(!tracker.receivedInvite) 
				fail("Didnt receive notification that the scenario is complete.");
		} catch (InterruptedException e) {
			fail("Test failed: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome
				+ "/sip-servlets-test-suite/applications/annotated-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		tomcat.deployContext(context);
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/annotations/annotated-servlet-dar.properties";
	}

	public void init() {

	}

	private SipListener getSipListener(EventObject sipEvent) {
		SipProvider source = (SipProvider) sipEvent.getSource();
		SipListener listener = (SipListener) providerTable.get(source);
		if (listener == null)
			throw new RuntimeException("Unexpected null listener");
		return listener;
	}

	public void processRequest(RequestEvent requestEvent) {
		getSipListener(requestEvent).processRequest(requestEvent);

	}

	public void processResponse(ResponseEvent responseEvent) {
		getSipListener(responseEvent).processResponse(responseEvent);

	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
		getSipListener(timeoutEvent).processTimeout(timeoutEvent);
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		fail("unexpected exception");

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		getSipListener(transactionTerminatedEvent)
				.processTransactionTerminated(transactionTerminatedEvent);

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		getSipListener(dialogTerminatedEvent).processDialogTerminated(
				dialogTerminatedEvent);

	}
}
