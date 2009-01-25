package org.mobicents.servlet.sip.seam.entrypoint;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.contexts.BasicContext;
import org.jboss.seam.contexts.BusinessProcessContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.SessionContext;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.ConversationEntry;
import org.jboss.seam.core.Manager;
import org.jboss.seam.util.Id;

public class SeamEntrypointUtils {
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
		Context eventContext = new BasicContext(ScopeType.EVENT );
		eventContext.set("sipServletMessage", message);
        setContext("applicationContext", new ApplicationContext( Lifecycle.getApplication() ));
		setContext("eventContext", eventContext);
		setContext("sessionContext", new SessionContext( new SipSeamRequestSessionMap(message)));
		setContext("conversationContext", new SessionContext( new SipSeamRequestSessionMap(message)));
		setContext("businessProcessContext", new BusinessProcessContext() );
		simulateConversation(message.getSession());
	}
	
	public static void beginEvent(SipSession sipSession) {

        setContext("applicationContext", new ApplicationContext( Lifecycle.getApplication() ));
		setContext("eventContext", new BasicContext(ScopeType.EVENT ));
		setContext("sessionContext", new SessionContext(new SipSeamSessionMap(sipSession)));
		setContext("conversationContext", new SessionContext(new SipSeamSessionMap(sipSession)));
		setContext("businessProcessContext", new BusinessProcessContext() );
		simulateConversation(sipSession);
	}
	
	private static void simulateConversation(SipSession session) {
		if(!session.isValid()) return;
		String cid = (String)session.getAttribute("org.mobicents.servlet.sip.conversationId");
		if(cid == null) {
			cid = Id.nextId();
			ArrayList<String> stack = new ArrayList<String>();
			stack.add(cid);
			Manager.instance().setCurrentConversationId(cid);
			Manager.instance().setCurrentConversationIdStack(stack);
			ConversationEntries.instance().createConversationEntry(cid, stack);
			Manager.instance().setLongRunningConversation(true);
			cid = Conversation.instance().getId();
			session.setAttribute("org.mobicents.servlet.sip.conversationId", cid);
		}
		ConversationEntries entries = ConversationEntries.instance();
		ConversationEntry ce = entries.getConversationEntry(cid);
		Manager.instance().setCurrentConversationIdStack(ce.getConversationIdStack());
		Manager.instance().setCurrentConversationId(ce.getId());
	}
	
	private static void cleanupConversation(SipSession session) {
		String cid = (String)session.getAttribute("org.mobicents.servlet.sip.conversationId");
		ConversationEntries.instance().removeConversationEntry(cid);
	}
	

	public static void endEvent() 
	{
		Lifecycle.endRequest();
	}
}
