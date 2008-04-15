package org.mobicents.servlet.sip.core.session;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.util.HashMap;
import java.util.Map;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;


public class SessionManager {
	private Map<String, SipApplicationSessionImpl> appSessions = new HashMap<String, SipApplicationSessionImpl>();
	
	private static transient Log logger = LogFactory
		.getLog(SessionManager.class);
	
	/**
	 * Tries to extract the session from the application data in the dialog or
	 * the transaction. If no app data is assigned a new session is created and
	 * is associated with the dialog or the transaction (depending on the request
	 * type).
	 * 
	 * @param requestEvent a JAIN SIP request event
	 * @return
	 */
	public SipSessionImpl getRequestSession(RequestEvent requestEvent)
	{
		SIPTransaction transaction = (SIPTransaction) requestEvent.getServerTransaction();
		SipProviderImpl sp = (SipProviderImpl)requestEvent.getSource();
		
		SipSessionImpl session = null;
		
		// Try to associate with a session instance from dialog
		Dialog dialog = requestEvent.getDialog();

		if(dialog != null)
		{
			Object appData = dialog.getApplicationData();
			session = (SipSessionImpl)appData;
			if(session != null) logger.info("Session found in dialog.");
		}
		
		// Create the transaction manually because getServerTransaction() may return null
		if(transaction == null)
		{
			try
			{
				transaction = (SIPTransaction)sp.getNewServerTransaction(requestEvent.getRequest());
				session = (SipSessionImpl) transaction.getApplicationData();
				if(session != null) logger.info("Session found in transaction.");
			}
			catch(Exception ex)
			{
				logger.info(ex.getMessage());
				throw new RuntimeException("Error creating new transaction!", ex);
			}
		}
		
		if(session != null)
		{
			logger.info("Dialog session associated!");
		}
		else // If it's not in the dialog, we MUST create a new session
		{
			try
			{
				if(isDialogCreatingRequest(requestEvent))
				{
					// Create a null dialog to associate app data directly.
					dialog = sp.getNewDialog(transaction);
					session = new SipSessionImpl(dialog, transaction, null);
					dialog.setApplicationData(session);
					logger.info("New SipSession created (dialog creating request)");
				}
				else
				{
					// Use the transaction
					session = new SipSessionImpl(null, transaction, null);
					transaction.setApplicationData(session);
					logger.info("New SipSession created (non-dialog creating request)");
				}
			}
			catch(Exception ex)
			{
				logger.info(ex.getMessage());
				throw new RuntimeException("Error creating new session!", ex);
			}
		}
		return session;
	}
	
	public void addApplicationSession(String name, SipApplicationSessionImpl appSession)
	{
		if(appSessions.get(name) != null)
			throw new IllegalArgumentException("Application session already exists for " + name);
		appSessions.put(name, appSession);
	}
	
	public SipApplicationSessionImpl getApplicationSession(String appName)
	{
		return appSessions.get(appName);
	}
	
	public static boolean isDialogCreatingRequest(RequestEvent requestEvent)
	{
		return (requestEvent.getRequest().getMethod().equals(Request.INVITE) ||
				requestEvent.getRequest().getMethod().equals(Request.SUBSCRIBE));
	}
}
