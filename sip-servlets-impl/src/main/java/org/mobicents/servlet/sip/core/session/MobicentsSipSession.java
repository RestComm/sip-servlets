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
package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.Transaction;

import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.message.MobicentsSipSessionFacade;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface MobicentsSipSession extends SipSession {

	/**
	 * get the internal Mobicents Sip Session Key
	 * @return the internal Mobicents Sip Session Key
	 */
	SipSessionKey getKey();

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
	
	Transaction getSessionCreatingTransaction();

	void setSessionCreatingTransaction(Transaction transaction);

	Set<Transaction> getOngoingTransactions();
	
	void removeOngoingTransaction(Transaction transaction);
	
	void addOngoingTransaction(Transaction transaction);

	Serializable getStateInfo();

	void setStateInfo(Serializable stateInfo);
		
	Principal getUserPrincipal();

	void setUserPrincipal(Principal principal);
	
	void setRoutingRegion(SipApplicationRoutingRegion routingRegion);

	/**
	 * Retieves the proxy of the sip session if any
	 * @return
	 */
	ProxyImpl getProxy();

	/**
	 * Set the proxy of the sip session
	 * @param proxy
	 */
	void setProxy(ProxyImpl proxy);
	
	public void setB2buaHelper(B2buaHelperImpl helperImpl);
	
	public B2buaHelperImpl getB2buaHelper();	
	void access();

	void updateStateOnResponse(SipServletResponseImpl sipServletResponseImpl,
			boolean receive);

	void updateStateOnSubsequentRequest(
			SipServletRequestImpl sipServletRequestImpl, boolean receive);

	void onTerminatedState();

	void onReadyToInvalidate();
	
	SipURI getOutboundInterface();

	Iterator<MobicentsSipSession> getDerivedSipSessions();

	void setState(State state);	

	void setSipSubscriberURI(URI subscriberURI);

	URI getSipSubscriberURI();
	
	void setParentSession(MobicentsSipSession mobicentsSipSession);

	Map<String, Object> getSipSessionAttributeMap();	

	void setSipSessionAttributeMap(Map<String, Object> sipSessionAttributeMap);

	void setLocalParty(Address addressImpl);

	void setRemoteParty(Address addressImpl);

	SipApplicationRoutingRegion getRegionInternal();
	
	Semaphore getSemaphore();
	
	//RFC 3265
	void addSubscription(SipServletMessageImpl sipServletMessage) throws SipException;
	void removeSubscription(SipServletMessageImpl sipServletMessage);
	
	MobicentsSipSessionFacade getSession();

	void setNextSipApplicationRouterInfo(SipApplicationRouterInfo routerInfo);
	SipApplicationRouterInfo getNextSipApplicationRouterInfo();

	public boolean isAckReceived();

	public void setAckReceived(boolean ackReceived);

	public long getCseq();

	public void setCseq(long cseq);
}
