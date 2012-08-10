/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.Transaction;

import org.mobicents.javax.servlet.sip.SipSessionExt;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletMessage;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.security.MobicentsSipSessionSecurity;
import org.mobicents.servlet.sip.core.security.SipPrincipal;

/**
 * Extension to the SipSession interface from JSR 289
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface MobicentsSipSession extends SipSession, SipSessionExt {

	/**
	 * get the internal Mobicents Sip Session Key
	 * @return the internal Mobicents Sip Session Key
	 */
	MobicentsSipSessionKey getKey();

	/**
	 * Add the derived sip session it will be identified by the To tag from its key 
	 * @param derivedSession the derived session to add  
	 */
	void addDerivedSipSessions(MobicentsSipSession derivedSession);
	/**
	 * Removes the derived sip session identified by the To tag in parameter
	 * @param toTag the to Tag identifying the sip session to remove
	 * @return the removed derived sip session
	 */
	public MobicentsSipSession removeDerivedSipSession(String toTag);	
	/**
	 * Find the derived sip session identified by its to tag
	 * @param toTag the to Tag identifying the sip session to remove
	 * @return the derived sip session identified by its to tag or null if none has been found
	 */
	public MobicentsSipSession findDerivedSipSession(String toTag);

	
	MobicentsSipApplicationSession getSipApplicationSession();

	String getHandler();

	Dialog getSessionCreatingDialog();
	void setSessionCreatingDialog(Dialog dialog);
	
	MobicentsSipServletMessage getSessionCreatingTransactionRequest();
	void setSessionCreatingTransactionRequest(MobicentsSipServletMessage message);

	Set<Transaction> getOngoingTransactions();
	void removeOngoingTransaction(Transaction transaction);
	void addOngoingTransaction(Transaction transaction);

	Serializable getStateInfo();
	void setStateInfo(Serializable stateInfo);
		
	SipPrincipal getUserPrincipal();
	void setUserPrincipal(SipPrincipal principal);
	
	void setRoutingRegion(SipApplicationRoutingRegion routingRegion);

	/**
	 * Retieves the proxy of the sip session if any
	 * @return
	 */
	MobicentsProxy getProxy();
	void setProxy(MobicentsProxy proxy);
	
	public void setB2buaHelper(MobicentsB2BUAHelper helperImpl);
	public MobicentsB2BUAHelper getB2buaHelper();	

	void access();

	void updateStateOnResponse(MobicentsSipServletResponse sipServletResponseImpl,
			boolean receive);

	void updateStateOnSubsequentRequest(
			MobicentsSipServletRequest sipServletRequestImpl, boolean receive);

	void onTerminatedState();

	void onReadyToInvalidate();
	
	String getOutboundInterface();

	Iterator<MobicentsSipSession> getDerivedSipSessions();

	void setState(State state);	

	void setSipSubscriberURI(String subscriberURI);

	String getSipSubscriberURI();
	
	MobicentsSipSession getParentSession();
	void setParentSession(MobicentsSipSession mobicentsSipSession);

	Map<String, Object> getSipSessionAttributeMap();	

	void setSipSessionAttributeMap(Map<String, Object> sipSessionAttributeMap);

	void setLocalParty(Address addressImpl);

	void setRemoteParty(Address addressImpl);

	SipApplicationRoutingRegion getRegionInternal();
	
	void acquire();
	void release();
	
	//RFC 3265
	void addSubscription(MobicentsSipServletMessage sipServletMessage) throws SipException;
	void removeSubscription(MobicentsSipServletMessage sipServletMessage);
	
	MobicentsSipSession getFacade();

	void setNextSipApplicationRouterInfo(SipApplicationRouterInfo routerInfo);
	SipApplicationRouterInfo getNextSipApplicationRouterInfo();	

	public boolean isValidInternal();
 
	public long getCseq();
	public void setCseq(long cseq);
	boolean validateCSeq(MobicentsSipServletRequest sipServletRequestImpl);
	
	String getTransport();
	void setTransport(String transport);
	
	int getRequestsPending();
	void setRequestsPending(int requests);

	void setAckReceived(long cSeq, boolean ackReceived);

	void notifySipSessionListeners(SipSessionEventType creation);
	
	void setSipSessionSecurity(MobicentsSipSessionSecurity sipSessionSecurity);
	MobicentsSipSessionSecurity getSipSessionSecurity();
	
	/**
	 * Associate a particular flow (see RFC5626) with this
	 * session.
	 * 
	 * @param flow
	 */
	public void setFlow(final javax.sip.address.SipURI flow);

	public javax.sip.address.SipURI getFlow();

	public void setOrphan(boolean orphan);

	public boolean isOrphan();
}
