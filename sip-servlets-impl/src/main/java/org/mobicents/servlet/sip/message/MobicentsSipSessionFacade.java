package org.mobicents.servlet.sip.message;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

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
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.startup.SipContext;
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
public class MobicentsSipSessionFacade implements MobicentsSipSession, Externalizable {

	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(MobicentsSipSessionFacade.class);
	private MobicentsSipSession sipSession;
	
	public MobicentsSipSessionFacade() { }
	
	public MobicentsSipSessionFacade(MobicentsSipSession sipSession) {
		this.sipSession = sipSession;
	}

	public SipServletRequest createRequest(String arg0) {
		return sipSession.createRequest(arg0);
	}

	public SipApplicationSession getApplicationSession() {
		return sipSession.getApplicationSession();
	}

	public Object getAttribute(String arg0) {
		return sipSession.getAttribute(arg0);
	}

	public Enumeration<String> getAttributeNames() {
		return sipSession.getAttributeNames();
	}

	public String getCallId() {
		return sipSession.getCallId();
	}

	public long getCreationTime() {
		return sipSession.getCreationTime();
	}

	public String getId() {
		return sipSession.getId();
	}

	public boolean getInvalidateWhenReady() {
		return sipSession.getInvalidateWhenReady();
	}

	public long getLastAccessedTime() {
		return sipSession.getLastAccessedTime();
	}

	public Address getLocalParty() {
		return sipSession.getLocalParty();
	}

	public SipApplicationRoutingRegion getRegion() {
		return sipSession.getRegion();
	}

	public Address getRemoteParty() {
		return sipSession.getRemoteParty();
	}

	public ServletContext getServletContext() {
		return sipSession.getServletContext();
	}

	public State getState() {
		return sipSession.getState();
	}

	public URI getSubscriberURI() {
		return sipSession.getSubscriberURI();
	}

	public void invalidate() {
		sipSession.invalidate();
	}

	public boolean isReadyToInvalidate() {
		return sipSession.isReadyToInvalidate();
	}

	public boolean isValid() {
		return sipSession.isValid();
	}

	public void removeAttribute(String arg0) {
		sipSession.removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		sipSession.setAttribute(arg0, arg1);
	}

	public void setHandler(String arg0) throws ServletException {
		sipSession.setHandler(arg0);
	}

	public void setInvalidateWhenReady(boolean arg0) {
		sipSession.setInvalidateWhenReady(arg0);
	}

	public void setOutboundInterface(InetAddress arg0) {
		sipSession.setOutboundInterface(arg0);
	}

	public void setOutboundInterface(InetSocketAddress arg0) {
		sipSession.setOutboundInterface(arg0);
	}

	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException {
		String sipSessionId = arg0.readUTF();
		String sipAppSessionId = arg0.readUTF();
		String sipAppName = arg0.readUTF();
		SipContext sipContext = StaticServiceHolder.sipStandardService
			.getSipApplicationDispatcher().findSipApplication(sipAppName);
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipSessionKey(sipSessionId);
		} catch (ParseException e) {
			logger.error("Couldn't parse the following sip session key " + sipSessionId, e);
			throw new RuntimeException(e);
		}
		SipApplicationSessionKey sipAppKey = null;
		try {
			sipAppKey = SessionManagerUtil.parseSipApplicationSessionKey(sipAppSessionId);
		} catch (ParseException e) {
			logger.error("Couldn't parse the following sip application session key " + sipAppSessionId, e);
			throw new RuntimeException(e);
		}
		MobicentsSipApplicationSession sipApplicationSession = ((SipManager)sipContext.getManager()).getSipApplicationSession(sipAppKey, false);
		this.sipSession = ((SipManager)sipContext.getManager()).getSipSession(key, false, null, sipApplicationSession);
		if(this.sipSession == null)
			throw new NullPointerException(
					"We just tried to pull a SipSession from the distributed cache and it's null, key="
					+ key);
		
	}

	public void writeExternal(ObjectOutput arg0) throws IOException {
		MobicentsSipSession sipSessionImpl = (MobicentsSipSession) this.sipSession;
		MobicentsSipApplicationSession sipAppSession = (MobicentsSipApplicationSession) 
			sipSessionImpl.getSipApplicationSession();
		arg0.writeUTF(sipSessionImpl.getId());
		arg0.writeUTF(sipAppSession.getId());
		arg0.writeUTF(sipAppSession.getApplicationName());
	}

	public void access() {
		sipSession.access();
	}

	public void addDerivedSipSessions(MobicentsSipSession derivedSession) {
		sipSession.addDerivedSipSessions(derivedSession);
	}

	public void addOngoingTransaction(Transaction transaction) {
		sipSession.addOngoingTransaction(transaction);
	}

	public void addSubscription(SipServletMessageImpl sipServletMessage)
			throws SipException {
		sipSession.addSubscription(sipServletMessage);
	}

	public MobicentsSipSession findDerivedSipSession(String toTag) {
		
		return findDerivedSipSession(toTag);
	}

	public B2buaHelperImpl getB2buaHelper() {
		
		return sipSession.getB2buaHelper();
	}

	public Iterator<MobicentsSipSession> getDerivedSipSessions() {
		
		return sipSession.getDerivedSipSessions();
	}

	public String getHandler() {
		
		return sipSession.getHandler();
	}

	public SipSessionKey getKey() {
		
		return sipSession.getKey();
	}

	public Set<Transaction> getOngoingTransactions() {
		
		return sipSession.getOngoingTransactions();
	}

	public SipURI getOutboundInterface() {
		
		return sipSession.getOutboundInterface();
	}

	public ProxyImpl getProxy() {
		
		return sipSession.getProxy();
	}

	public SipApplicationRoutingRegion getRegionInternal() {
		
		return sipSession.getRegionInternal();
	}

	public Semaphore getSemaphore() {
		
		return sipSession.getSemaphore();
	}

	public Dialog getSessionCreatingDialog() {
		
		return sipSession.getSessionCreatingDialog();
	}

	public Transaction getSessionCreatingTransaction() {
		
		return sipSession.getSessionCreatingTransaction();
	}

	public MobicentsSipApplicationSession getSipApplicationSession() {
		
		return sipSession.getSipApplicationSession();
	}

	public Map<String, Object> getSipSessionAttributeMap() {
		
		return sipSession.getSipSessionAttributeMap();
	}

	public URI getSipSubscriberURI() {
		
		return sipSession.getSipSubscriberURI();
	}

	public Serializable getStateInfo() {
		
		return sipSession.getStateInfo();
	}

	public Principal getUserPrincipal() {
		
		return sipSession.getUserPrincipal();
	}

	public void onReadyToInvalidate() {
		sipSession.onReadyToInvalidate();
	}

	public void onTerminatedState() {
		sipSession.onTerminatedState();
	}

	public MobicentsSipSession removeDerivedSipSession(String toTag) {
		
		return sipSession.removeDerivedSipSession(toTag);
	}

	public void removeOngoingTransaction(Transaction transaction) {
		
		sipSession.removeOngoingTransaction(transaction);
	}

	public void removeSubscription(SipServletMessageImpl sipServletMessage) {
		sipSession.removeSubscription(sipServletMessage);
	}

	public void setB2buaHelper(B2buaHelperImpl helperImpl) {
		sipSession.setB2buaHelper(helperImpl);
	}

	public void setLocalParty(Address addressImpl) {
		sipSession.setLocalParty(addressImpl);
	}

	public void setParentSession(MobicentsSipSession mobicentsSipSession) {
		sipSession.setParentSession(mobicentsSipSession);
	}

	public void setProxy(ProxyImpl proxy) {
		sipSession.setProxy(proxy);
	}

	public void setRemoteParty(Address addressImpl) {
		sipSession.setRemoteParty(addressImpl);
	}

	public void setRoutingRegion(SipApplicationRoutingRegion routingRegion) {
		sipSession.setRoutingRegion(routingRegion);
	}

	public void setSessionCreatingDialog(Dialog dialog) {
		sipSession.setSessionCreatingDialog(dialog);
	}

	public void setSessionCreatingTransaction(Transaction transaction) {
		sipSession.setSessionCreatingTransaction(transaction);
	}

	public void setSipSessionAttributeMap(
			Map<String, Object> sipSessionAttributeMap) {
		sipSession.setSipSessionAttributeMap(sipSessionAttributeMap);
	}

	public void setSipSubscriberURI(URI subscriberURI) {
		sipSession.setSipSubscriberURI(subscriberURI);
	}

	public void setState(State state) {
		sipSession.setState(state);
	}

	public void setStateInfo(Serializable stateInfo) {
		sipSession.setStateInfo(stateInfo);
	}

	public void setUserPrincipal(Principal principal) {
		sipSession.setUserPrincipal(principal);
	}

	public void updateStateOnResponse(
			SipServletResponseImpl sipServletResponseImpl, boolean receive) {
		sipSession.updateStateOnResponse(sipServletResponseImpl, receive);
	}

	public void updateStateOnSubsequentRequest(
			SipServletRequestImpl sipServletRequestImpl, boolean receive) {
		sipSession.updateStateOnSubsequentRequest(sipServletRequestImpl, receive);
	}

	public MobicentsSipSessionFacade getSession() {
		return sipSession.getSession();
	}
	
	@Override
	public boolean equals(Object obj) {
		return sipSession.equals(obj);
	}

	@Override
	public int hashCode() {
		return sipSession.hashCode();
	}

	@Override
	public String toString() {
		return sipSession.toString();
	}

	public SipApplicationRouterInfo getNextSipApplicationRouterInfo() {
		return sipSession.getNextSipApplicationRouterInfo();
	}

	public void setNextSipApplicationRouterInfo(
			SipApplicationRouterInfo routerInfo) {
		sipSession.setNextSipApplicationRouterInfo(routerInfo);
	}

	public void setAckReceived(boolean ackReceived) {
	}

	public boolean isAckReceived() {
		return false;
	}

	public void setCseq(long cseq) {
	}

	public long getCseq() {
		return 0;
	}
}
