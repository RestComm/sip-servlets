/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.model.Customer;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

@Stateful
@Name("help")
public class HelpAction implements Help, Serializable {
	@Logger private Log log;

	@In(value = "currentUser", required = false)
	Customer customer;

	String phoneNumber;

	//jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void call() {
		log.info("the phone number to call is " + phoneNumber);
		String customerPhone = "sip:" + phoneNumber + "@" + (String)Contexts.getApplicationContext().get("caller.domain");;
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			
//			String callerAddress = (String)Contexts.getApplicationContext().get("caller.sip");
//			String callerDomain = (String)Contexts.getApplicationContext().get("caller.domain");
//			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
//			Address fromAddress = sipFactory.createAddress(fromURI);
			
			String toAddr = (String)Contexts.getApplicationContext().get("call.center.contact");
			Address toAddress = sipFactory.createAddress(toAddr);
			Address fromAddress = sipFactory.createAddress(customerPhone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(toAddr);
			sipServletRequest.setRequestURI(requestURI);
			
        	sipServletRequest.getSession().setAttribute("SecondPartyAddress", fromAddress);
        	sipServletRequest.getSession().setAttribute("HelpCall", Boolean.TRUE);
        	sipServletRequest.send();
        	
		} catch (UnsupportedOperationException uoe) {
			log.error("An unexpected exception occurred while trying to create the request for checkout confirmation", uoe);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while trying to create the request for checkout confirmation", e);
		}			
	}
	
	@Remove
	public void destroy() {
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
