package org.mobicents.servlet.sip.seam.entrypoint;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;

import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.contexts.BasicContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.EventContext;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.SessionContext;
import org.jboss.seam.core.Events;

public class SeamEntryPointServlet extends javax.servlet.sip.SipServlet implements SipSessionListener{
	
	public static void setContext(String ctxName, Context newctx) {
		Field field = null;
		try {
			field = Contexts.class.getDeclaredField(ctxName);
	
			field.setAccessible(true);
			ThreadLocal<Context> ctx = (ThreadLocal<Context>) field.get(null);
			ctx.set(newctx);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void beginEvent(SipServletMessage message) {
		setContext("applicationContext", new ApplicationContext( Lifecycle.getApplication() ));
		setContext("eventContext", new BasicContext(ScopeType.EVENT ));
		setContext("sessionContext", new SessionContext( new SipSeamRequestSessionMap(message)));
		setContext("conversationContext", new BasicContext(ScopeType.CONVERSATION ));
		//setContext("businessProcessContext", new BusinessProcessContext() );
	}
	

	public static void endEvent(SipServletMessage message) 
	{
		Lifecycle.endRequest();
	}

	public void sessionCreated(SipSessionEvent arg0) {
		Lifecycle.beginSession(new SipSeamSessionMap(arg0.getSession()));
		System.out.println("SEAM SIP SESSION CREATED");
	}

	public void sessionDestroyed(SipSessionEvent arg0) {
		Lifecycle.endSession(new SipSeamSessionMap(arg0.getSession()));
		System.out.println("SEAM SIP SESSION DESTROYED");
	}

	public void sessionReadyToInvalidate(SipSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doRequest(SipServletRequest request) throws ServletException,
			IOException {
		beginEvent(request);
		Events.instance().raiseEvent(request.getMethod().toUpperCase(), request);
		endEvent(request);
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException {
		beginEvent(response);
		Events.instance().raiseEvent("RESPONSE", response);
		endEvent(response);
	}
}
