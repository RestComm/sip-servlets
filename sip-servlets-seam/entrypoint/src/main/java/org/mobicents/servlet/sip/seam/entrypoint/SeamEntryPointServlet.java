package org.mobicents.servlet.sip.seam.entrypoint;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;

import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.*;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Init;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpSession;

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
		setContext("businessProcessContext", new BusinessProcessContext() );
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
		ServletContext ctx = request.getSession().getServletContext();
		Init init = (Init) ctx.getAttribute( Seam.getComponentName(Init.class) );
		if ( init!=null && init.isDebug())
		{
			MockHttpSession session = new MockHttpSession(ctx);
			MockHttpServletRequest r = new MockHttpServletRequest(session);

			try {
				new Initialization(ctx).redeploy(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
