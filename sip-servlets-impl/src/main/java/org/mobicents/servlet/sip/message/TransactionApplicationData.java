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
package org.mobicents.servlet.sip.message;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.sip.Address;
import javax.sip.Transaction;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;

/**
 * A container for holding branch specific data.
 * 
 *@author mranga
 */
public class TransactionApplicationData implements Serializable {		

	private static final long serialVersionUID = 9170581635026591070L;
	private static final Logger logger = Logger.getLogger(TransactionApplicationData.class);
	private transient ProxyBranchImpl proxyBranch;	
	private SipServletMessageImpl sipServletMessage;
//	private SipSessionKey sipSessionKey;
	private transient Set<SipServletResponseImpl> sipServletResponses;
	private transient Transaction transaction;
	private transient String initialRemoteHostAddress;
	private transient int initialRemotePort;
	private transient String initialRemoteTransport;
	private transient Address initialPoppedRoute;
	private transient AtomicInteger rseqNumber;
	// to be made non transient if we support tx failover at some point
	// or handle it conditionally through an Externalizable interface
	private transient String appNotDeployed = null;
	private transient boolean noAppReturned = false;
	private transient String modifier = null;	
	private transient boolean canceled = false;
	
	public TransactionApplicationData(SipServletMessageImpl sipServletMessage ) {		
		this.sipServletMessage = sipServletMessage;
		sipServletResponses = null;		
	}
	
	public void setProxyBranch(ProxyBranchImpl proxyBranch) {
		this.proxyBranch = proxyBranch;
	}
	
	/**
	 * @return the proxyBranch
	 */
	public ProxyBranchImpl getProxyBranch() {
		return proxyBranch;
	}
	
	public SipServletMessageImpl getSipServletMessage() {
		return this.sipServletMessage;
	}
	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}
	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	/**
	 * used to get access from the B2BUA to pending messages on the transaction
	 */
	public void addSipServletResponse(SipServletResponseImpl sipServletResponse) {
		if(sipServletResponses == null) {
			sipServletResponses = new CopyOnWriteArraySet<SipServletResponseImpl>();
		}
		sipServletResponses.add(sipServletResponse);
	}
	
	public Set<SipServletResponseImpl> getSipServletResponses() {
		return sipServletResponses;
	}
	/**
	 * @param initialRemoteHostAddress the initialRemoteHostAddress to set
	 */
	public void setInitialRemoteHostAddress(String initialRemoteHostAddress) {
		this.initialRemoteHostAddress = initialRemoteHostAddress;
	}
	/**
	 * @return the initialRemoteHostAddress
	 */
	public String getInitialRemoteHostAddress() {
		return initialRemoteHostAddress;
	}
	/**
	 * @param initialRemotePort the initialRemotePort to set
	 */
	public void setInitialRemotePort(int initialRemotePort) {
		this.initialRemotePort = initialRemotePort;
	}
	/**
	 * @return the initialRemotePort
	 */
	public int getInitialRemotePort() {
		return initialRemotePort;
	}
	/**
	 * @param initialRemoteTransport the initialRemoteTransport to set
	 */
	public void setInitialRemoteTransport(String initialRemoteTransport) {
		this.initialRemoteTransport = initialRemoteTransport;
	}
	/**
	 * @return the initialRemoteTransport
	 */
	public String getInitialRemoteTransport() {
		return initialRemoteTransport;
	}
	public Address getInitialPoppedRoute() {
		return initialPoppedRoute;
	}
	/**
	 * @param initialPoppedRoute the initialPoppedRoute to set
	 */
	public void setInitialPoppedRoute(Address initialPoppedRoute) {
		this.initialPoppedRoute = initialPoppedRoute;
	}
	
	/**
	 * @return the rseqNumber
	 */
	public AtomicInteger getRseqNumber() {
		if(rseqNumber == null) {
			rseqNumber = new AtomicInteger(1);
		}
		return rseqNumber;
	}
	/**
	 * @param rseqNumber the rseqNumber to set
	 */
	public void setRseqNumber(AtomicInteger rseqNumber) {
		this.rseqNumber = rseqNumber;
	}

	/**
	 * @param appNotDeployed the appNotDeployed to set
	 */
	public void setAppNotDeployed(String appNotDeployed) {
		this.appNotDeployed = appNotDeployed;
	}

	/**
	 * @return the appNotDeployed
	 */
	public String getAppNotDeployed() {
		return appNotDeployed;
	}

	/**
	 * @param noAppReturned the noAppReturned to set
	 */
	public void setNoAppReturned(boolean noAppReturned) {
		this.noAppReturned = noAppReturned;
	}

	/**
	 * @return the noAppReturned
	 */
	public boolean isNoAppReturned() {
		return noAppReturned;
	}

	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * @return the modifier
	 */
	public String getModifier() {
		return modifier;
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public void cleanUp() {
		if(logger.isDebugEnabled()) {
			logger.debug("cleaning up the application data");
		}
		initialPoppedRoute = null;
		proxyBranch = null;
		// cannot nullify because of noAckReceived needs it and TCK SipApplicationSessionListenerTest
//		if(cleanUpSipServletMessage && sipServletMessage != null) {
//			sipServletMessage.cleanUp();
//			if(sipServletMessage instanceof SipServletRequestImpl) {
//				((SipServletRequestImpl)sipServletMessage).cleanUpLastResponses();
//			}
//			sipSessionKey = sipServletMessage.getSipSessionKey();
//			sipServletMessage = null;
//		}
		if(sipServletResponses != null) {
			sipServletResponses.clear();
			sipServletResponses = null;
		}
		transaction = null;
		rseqNumber = null;
	}

//	/**
//	 * @return the sipSessionKey
//	 */
//	public SipSessionKey getSipSessionKey() {
//		if(sipServletMessage != null) {
//			return sipServletMessage.getSipSessionKey();
//		}
//		return sipSessionKey;
//	}	
}
