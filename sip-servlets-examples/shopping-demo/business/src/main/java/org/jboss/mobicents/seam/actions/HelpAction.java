/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
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

	
	public void call() {
		log.info("the phone number to call is " + phoneNumber);
		if(phoneNumber != null) {
			fireEvent(phoneNumber);
		}
	}

	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void fireEvent(String customerPhone) {		
			
		customerPhone = "sip:" + customerPhone + "@" + (String)Contexts.getApplicationContext().get("caller.domain");;
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();			
			String callerAddress = (String)Contexts.getApplicationContext().get("caller.sip");
			String callerDomain = (String)Contexts.getApplicationContext().get("caller.domain");
			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
			Address fromAddress = sipFactory.createAddress(fromURI);
			Address toAddress = sipFactory.createAddress(customerPhone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			// getting the contact address for the registered customer sip address
			String userContact= ((Map<String, String>)Contexts.getApplicationContext().get("registeredUsersMap")).get(customerPhone);
			if(userContact != null && userContact.length() > 0) {
				// for customers using the registrar
				URI requestURI = sipFactory.createURI(userContact);
				sipServletRequest.setRequestURI(requestURI);
			} else {
				// for customers not using the registrar and registered directly their contact location
				URI requestURI = sipFactory.createURI(customerPhone);
				sipServletRequest.setRequestURI(requestURI);
			}
			
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
