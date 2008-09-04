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

import javax.servlet.sip.B2buaHelper;
import javax.sip.Transaction;

import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * A container for holding branch specific data.
 * 
 *@author mranga
 */
public class TransactionApplicationData implements Serializable {
	private transient ProxyImpl proxy;
	private transient ProxyBranchImpl proxyBranch;	
	private transient SipServletMessageImpl sipServletMessage;
	private transient Set<SipServletResponseImpl> sipServletResponses;
	private transient Transaction transaction;
	private transient B2buaHelper b2buaHelper;
	private transient String initialRemoteHostAddress;
	private transient int initialRemotePort;
	private transient String initialRemoteTransport;
	
	public TransactionApplicationData(SipServletMessageImpl sipServletMessage ) {		
		this.sipServletMessage = sipServletMessage;
		sipServletResponses = new CopyOnWriteArraySet<SipServletResponseImpl>();
	}
	/**
	 * set proxy
	 */
	public void setProxy(ProxyImpl proxy) {
		this.proxy = proxy;
	}
	
	public void setProxyBranch(ProxyBranchImpl proxyBranch) {
		this.proxyBranch = proxyBranch;
	}
	
	/**
	 * @return the proxy
	 */
	public ProxyImpl getProxy() {
		return proxy;
	}
	
	/**
	 * @param b2buaHelperImpl the b2buaHelperImpl to set
	 */
	public void setB2buaHelper(B2buaHelper b2buaHelper) {
		this.b2buaHelper = b2buaHelper;
	}
	/**
	 * @return the b2buaHelperImpl
	 */
	public B2buaHelper getB2buaHelper() {
		return b2buaHelper;
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
	

}
