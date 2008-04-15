package org.mobicents.servlet.sip.message;

import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.Transaction;

import org.mobicents.servlet.sip.core.session.SipSessionImpl;
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
	private SipSessionImpl sipSession;

	
	public TransactionApplicationData(SipSessionImpl sipSession ) {
		this.sipSession = (SipSessionImpl) sipSession;
		
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
	
	public void setSipSession(SipSessionImpl sipSession) {
		this.sipSession = sipSession;
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
	
	/**
	 * @return the sipSession
	 */
	public SipSessionImpl getSipSession() {
		return sipSession;
	}

}
