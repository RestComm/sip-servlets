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

package org.mobicents.servlet.sip.message;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.Transaction;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipSessionAsynchronousWork;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletMessage;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.security.MobicentsSipSessionSecurity;
import org.mobicents.servlet.sip.core.security.SipPrincipal;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionEventType;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * The purpose of this class is to be a facade to the real sip session as well as a 
 * serializable session class that can be put as a session attribute in other sessions or even its own session. 
 * Basically instead of replicating the whole attribute map, we will replicate the id, then on the remote side we will
 * read the ID and look it up in the remote session manager.
 * 
 * @author vralev
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsSipSessionFacade implements MobicentsSipSession, Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MobicentsSipSessionFacade.class);
	private transient MobicentsSipSession sipSession;
	private MobicentsSipSessionKey sipSessionKey = null;
	private MobicentsSipApplicationSessionKey sipAppSessionKey = null;
	
//	public MobicentsSipSessionFacade() { }
	
	public MobicentsSipSessionFacade(MobicentsSipSession sipSession) {
		this.sipSession = sipSession;
		this.sipSessionKey = sipSession.getKey();
		this.sipAppSessionKey = sipSession.getSipApplicationSession().getKey();
	}

	public SipServletRequest createRequest(String arg0) {
		return getSipSession().createRequest(arg0);
	}

	public SipApplicationSession getApplicationSession() {
		return getSipSession().getApplicationSession();
	}

	public Object getAttribute(String arg0) {
		return getSipSession().getAttribute(arg0);
	}

	public Enumeration<String> getAttributeNames() {
		return getSipSession().getAttributeNames();
	}

	public String getCallId() {
		return getSipSession().getCallId();
	}

	public long getCreationTime() {
		return getSipSession().getCreationTime();
	}

	public String getId() {
		return getSipSession().getId();
	}

	public boolean getInvalidateWhenReady() {
		return getSipSession().getInvalidateWhenReady();
	}

	public long getLastAccessedTime() {
		return getSipSession().getLastAccessedTime();
	}

	public Address getLocalParty() {
		return getSipSession().getLocalParty();
	}

	public SipApplicationRoutingRegion getRegion() {
		return getSipSession().getRegion();
	}

	public Address getRemoteParty() {
		return getSipSession().getRemoteParty();
	}

	public ServletContext getServletContext() {
		return getSipSession().getServletContext();
	}

	public State getState() {
		return getSipSession().getState();
	}

	public URI getSubscriberURI() {
		return getSipSession().getSubscriberURI();
	}

	public void invalidate() {
		getSipSession().invalidate();
	}

	public boolean isReadyToInvalidate() {
		return getSipSession().isReadyToInvalidate();
	}

	public boolean isValid() {
		return getSipSession().isValid();
	}
	
	public boolean isValidInternal() {
		return getSipSession().isValidInternal();
	}

	public void removeAttribute(String arg0) {
		getSipSession().removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		getSipSession().setAttribute(arg0, arg1);
	}

	public void setHandler(String arg0) throws ServletException {
		getSipSession().setHandler(arg0);
	}

	public void setInvalidateWhenReady(boolean arg0) {
		getSipSession().setInvalidateWhenReady(arg0);
	}

	public void setOutboundInterface(InetAddress arg0) {
		getSipSession().setOutboundInterface(arg0);
	}

	public void setOutboundInterface(InetSocketAddress arg0) {
		getSipSession().setOutboundInterface(arg0);
	}
	
	public void setOutboundInterface(SipURI arg0) {
		getSipSession().setOutboundInterface(arg0);
	}

//	public void readExternal(ObjectInput in) throws IOException,
//			ClassNotFoundException {		
//		sipSessionKey = (SipSessionKey) in.readObject();		
//		sipAppSessionKey = (SipApplicationSessionKey) in.readObject();
//		if(logger.isDebugEnabled()) {
//			logger.debug("sip app session key=" + sipAppSessionKey);
//			logger.debug("sip session key=" + sipSessionKey);
//		}				
//	}
//
//	public void writeExternal(ObjectOutput out) throws IOException {		
//		out.writeObject(sipSessionKey);
//		out.writeObject(sipAppSessionKey);
//	}

	public void access() {
		getSipSession().access();
	}

	public void addDerivedSipSessions(MobicentsSipSession derivedSession) {
		getSipSession().addDerivedSipSessions(derivedSession);
	}

	public void addOngoingTransaction(Transaction transaction) {
		getSipSession().addOngoingTransaction(transaction);
	}

	public void addSubscription(MobicentsSipServletMessage sipServletMessage)
			throws SipException {
		getSipSession().addSubscription(sipServletMessage);
	}

	public MobicentsSipSession findDerivedSipSession(String toTag) {
		
		return findDerivedSipSession(toTag);
	}

	public MobicentsB2BUAHelper getB2buaHelper() {
		
		return getSipSession().getB2buaHelper();
	}

	public Iterator<MobicentsSipSession> getDerivedSipSessions() {
		
		return getSipSession().getDerivedSipSessions();
	}

	public String getHandler() {
		
		return getSipSession().getHandler();
	}

	public MobicentsSipSessionKey getKey() {
		
		return getSipSession().getKey();
	}

	public Set<Transaction> getOngoingTransactions() {
		
		return getSipSession().getOngoingTransactions();
	}

	public String getOutboundInterface() {
		
		return getSipSession().getOutboundInterface();
	}

	public MobicentsProxy getProxy() {
		
		return getSipSession().getProxy();
	}

	public SipApplicationRoutingRegion getRegionInternal() {
		
		return getSipSession().getRegionInternal();
	}

	public Dialog getSessionCreatingDialog() {
		
		return getSipSession().getSessionCreatingDialog();
	}

	public MobicentsSipServletMessage getSessionCreatingTransactionRequest() {
		
		return getSipSession().getSessionCreatingTransactionRequest();
	}

	public MobicentsSipApplicationSession getSipApplicationSession() {
		
		return getSipSession().getSipApplicationSession();
	}

	public Map<String, Object> getSipSessionAttributeMap() {
		
		return getSipSession().getSipSessionAttributeMap();
	}

	public String getSipSubscriberURI() {
		
		return getSipSession().getSipSubscriberURI();
	}

	public Serializable getStateInfo() {
		
		return getSipSession().getStateInfo();
	}

	public SipPrincipal getUserPrincipal() {
		
		return getSipSession().getUserPrincipal();
	}

	public void onReadyToInvalidate() {
		getSipSession().onReadyToInvalidate();
	}

	public void onTerminatedState() {
		getSipSession().onTerminatedState();
	}

	public MobicentsSipSession removeDerivedSipSession(String toTag) {
		
		return getSipSession().removeDerivedSipSession(toTag);
	}

	public void removeOngoingTransaction(Transaction transaction) {
		
		getSipSession().removeOngoingTransaction(transaction);
	}

	public void removeSubscription(MobicentsSipServletMessage sipServletMessage) {
		getSipSession().removeSubscription(sipServletMessage);
	}

	public void setB2buaHelper(MobicentsB2BUAHelper helperImpl) {
		getSipSession().setB2buaHelper(helperImpl);
	}

	public void setLocalParty(Address addressImpl) {
		getSipSession().setLocalParty(addressImpl);
	}

	public void setParentSession(MobicentsSipSession mobicentsSipSession) {
		getSipSession().setParentSession(mobicentsSipSession);
	}
	
	public MobicentsSipSession getParentSession() {
		return getSipSession().getParentSession();
	}

	public void setProxy(MobicentsProxy proxy) {
		getSipSession().setProxy(proxy);
	}

	public void setRemoteParty(Address addressImpl) {
		getSipSession().setRemoteParty(addressImpl);
	}

	public void setRoutingRegion(SipApplicationRoutingRegion routingRegion) {
		getSipSession().setRoutingRegion(routingRegion);
	}

	public void setSessionCreatingDialog(Dialog dialog) {
		getSipSession().setSessionCreatingDialog(dialog);
	}

	public void setSessionCreatingTransactionRequest(MobicentsSipServletMessage message) {
		getSipSession().setSessionCreatingTransactionRequest(message);
	}

	public void setSipSessionAttributeMap(
			Map<String, Object> sipSessionAttributeMap) {
		getSipSession().setSipSessionAttributeMap(sipSessionAttributeMap);
	}

	public void setSipSubscriberURI(String subscriberURI) {
		getSipSession().setSipSubscriberURI(subscriberURI);
	}

	public void setState(State state) {
		getSipSession().setState(state);
	}

	public void setStateInfo(Serializable stateInfo) {
		getSipSession().setStateInfo(stateInfo);
	}

	public void setUserPrincipal(SipPrincipal principal) {
		getSipSession().setUserPrincipal(principal);
	}

	public void updateStateOnResponse(
			MobicentsSipServletResponse sipServletResponseImpl, boolean receive) {
		getSipSession().updateStateOnResponse(sipServletResponseImpl, receive);
	}

	public void updateStateOnSubsequentRequest(
			MobicentsSipServletRequest sipServletRequestImpl, boolean receive) {
		getSipSession().updateStateOnSubsequentRequest(sipServletRequestImpl, receive);
	}

	public MobicentsSipSession getFacade() {
		return getSipSession().getFacade();
	}
	
	@Override
	public boolean equals(Object obj) {
		return getSipSession().equals(obj);
	}

	@Override
	public int hashCode() {
		return getSipSession().hashCode();
	}

	@Override
	public String toString() {
		return sipSessionKey.toString();
	}

	public SipApplicationRouterInfo getNextSipApplicationRouterInfo() {
		return getSipSession().getNextSipApplicationRouterInfo();
	}

	public void setNextSipApplicationRouterInfo(
			SipApplicationRouterInfo routerInfo) {
		getSipSession().setNextSipApplicationRouterInfo(routerInfo);
	}

	public void setAckReceived(long cSeq, boolean ackReceived) {
		getSipSession().setAckReceived(cSeq, ackReceived);
	}	

	public void setCseq(long cseq) {
		getSipSession().setCseq(cseq);
	}

	public long getCseq() {
		return getSipSession().getCseq();
	}
	
	public boolean validateCSeq(MobicentsSipServletRequest request) {
		return getSipSession().validateCSeq(request);
	}
	
	/**
	 * @return the sipSession
	 */
	private MobicentsSipSession getSipSession() {
		// lazy loading the session, useful for HA
		if(sipSession == null){
			if(logger.isDebugEnabled()) {
				logger.debug("Trying to load the session from the deserialized session facade with the key " + sipSessionKey);
			}
			SipContext sipContext = StaticServiceHolder.sipStandardService
				.getSipApplicationDispatcher().findSipApplication(sipAppSessionKey.getApplicationName());
	
			MobicentsSipApplicationSession sipApplicationSession = sipContext.getSipManager().getSipApplicationSession(sipAppSessionKey, false);
			sipSession = sipContext.getSipManager().getSipSession(sipSessionKey, false, null, sipApplicationSession);
			if(sipSession == null)
				throw new NullPointerException(
					"We just tried to pull a SipSession from the distributed cache and it's null, key="
						+ sipSessionKey);
		}
		return sipSession;
	}	
	
	public MobicentsSipSession getMobicentsSipSession() {
		return sipSession;
	}

	public String getTransport() {
		return sipSession.getTransport();
	}

	public void setTransport(String transport) {
		sipSession.setTransport(transport);
	}

	public void scheduleAsynchronousWork(SipSessionAsynchronousWork work) {
		sipSession.scheduleAsynchronousWork(work);
	}

	public int getRequestsPending() {
		return sipSession.getRequestsPending();
	}

	public void setRequestsPending(int requests) {
		sipSession.setRequestsPending(requests);
	}

	public void notifySipSessionListeners(SipSessionEventType creation) {
		sipSession.notifySipSessionListeners(creation);		
	}

	public void setCopyRecordRouteHeadersOnSubsequentResponses(
			boolean copyRecordRouteHeadersOnSubsequentResponses) {
		sipSession.setCopyRecordRouteHeadersOnSubsequentResponses(copyRecordRouteHeadersOnSubsequentResponses);
	}

	public boolean getCopyRecordRouteHeadersOnSubsequentResponses() {
		return sipSession.getCopyRecordRouteHeadersOnSubsequentResponses();
	}

	public MobicentsSipSessionSecurity getSipSessionSecurity() {
		return sipSession.getSipSessionSecurity();
	}

	public void setSipSessionSecurity(MobicentsSipSessionSecurity sipSessionSecurity) {
		sipSession.setSipSessionSecurity(sipSessionSecurity);
	}
	
	public void acquire() {
		sipSession.acquire();
	}

	public void release() {
		sipSession.release();
	}
	
	public void setFlow(final javax.sip.address.SipURI flow) {
		this.sipSession.setFlow(flow);
	}

	public javax.sip.address.SipURI getFlow() {
		return this.sipSession.getFlow();
	}

	protected boolean orphan = false;

	public boolean isOrphan() {
		return sipSession.isOrphan();
	}

	public void setOrphan(boolean orphan) {
		sipSession.setOrphan(orphan);
	}
}
