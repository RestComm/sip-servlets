package org.mobicents.servlet.sip.message;

import javax.sip.Transaction;

import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * A container for holding branch specific data.
 * 
 *@author mranga
 */
public class TransactionApplicationData {
	private ProxyImpl proxy;
	private ProxyBranchImpl proxyBranch;	
	private SipServletMessageImpl sipServletMessage;
	private Transaction transaction;
	private SipServletRequestImpl originalProxyRequest;
	
	public TransactionApplicationData(SipServletMessageImpl sipServletMessage ) {		
		this.sipServletMessage = sipServletMessage;		
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

}
