package org.mobicents.servlet.sip.seam.entrypoint;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;

import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.*;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.deployment.ComponentsXmlDeploymentHandler;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.util.Id;
import org.mobicents.servlet.sip.seam.entrypoint.media.MsProviderContainer;

public class SeamEntryPointServlet extends javax.servlet.sip.SipServlet implements SipSessionListener {
	public void sessionCreated(SipSessionEvent arg0) {
		arg0.getSession().setAttribute("msSession", MsProviderContainer.msProvider.createSession());
		arg0.getSession().setAttribute("sipSession", arg0.getSession());
		Lifecycle.beginSession(new SipSeamSessionMap(arg0.getSession()));
		System.out.println("SEAM SIP SESSION CREATED");
	}

	public void sessionDestroyed(SipSessionEvent arg0) {
		//cleanupConversation(arg0.getSession());
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
		SeamEntrypointUtils.beginEvent(request);
		Contexts.getApplicationContext().set("sipSession", request.getSession());
		Contexts.getApplicationContext().set("msSession", MsProviderContainer.msProvider.createSession());
		Events.instance().raiseEvent(request.getMethod().toUpperCase(), request);
		SeamEntrypointUtils.endEvent();
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException {
		SeamEntrypointUtils.beginEvent(response);
		Events.instance().raiseEvent("RESPONSE", response);
		SeamEntrypointUtils.endEvent();
	}
}
