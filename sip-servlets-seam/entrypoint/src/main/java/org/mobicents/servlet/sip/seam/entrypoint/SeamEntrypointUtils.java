package org.mobicents.servlet.sip.seam.entrypoint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Id;

public class SeamEntrypointUtils {
	private static LogProvider log = Logging.getLogProvider(SeamEntrypointUtils.class);
	
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
	
	public static void destroyContext(Context context) {
		try {
			Method destroyMethod = Contexts.class.getDeclaredMethod("destroy", Context.class);
			destroyMethod.setAccessible(true);
			destroyMethod.invoke(null, context);
			destroyMethod.setAccessible(false);
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
		setContext("conversationContext", new BasicContext(ScopeType.CONVERSATION));
		setContext("businessProcessContext", new BusinessProcessContext() );
		simulateConversationStart(message.getSession());
	}
	
	public static void beginEvent(SipSession sipSession) {

        setContext("applicationContext", new ApplicationContext( Lifecycle.getApplication() ));
		setContext("eventContext", new BasicContext(ScopeType.EVENT ));
		setContext("sessionContext", new SessionContext(new SipSeamSessionMap(sipSession)));
		setContext("conversationContext", new BasicContext(ScopeType.CONVERSATION));
		setContext("businessProcessContext", new BusinessProcessContext() );
		simulateConversationStart(sipSession);
	}
	
	private static void simulateConversationStart(SipSession session) {
		if(!session.isValid()) return;

		String cid = Id.nextId();
		ArrayList<String> stack = new ArrayList<String>();
		stack.add(cid);
		Manager.instance().setCurrentConversationId(cid);
		Manager.instance().setCurrentConversationIdStack(stack);
		ConversationEntries.instance().createConversationEntry(cid, stack);
		cid = Conversation.instance().getId();

		ConversationEntries entries = ConversationEntries.instance();
		ConversationEntry ce = entries.getConversationEntry(cid);
		Manager.instance().setCurrentConversationIdStack(ce.getConversationIdStack());
		Manager.instance().setCurrentConversationId(ce.getId());
	}
	
	public static void endEvent() 
	{
		Lifecycle.endRequest();
	}
}
