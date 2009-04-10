package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;

@Name("orderApprovalAction")
@Stateless
public class OrderApprovalAction implements OrderApproval, Serializable {
	@Logger private Log log;
	
	@In
	String customerfullname;

	@In
	String cutomerphone;

	@In
	BigDecimal amount;

	@In
	Long orderId;
	
	//jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;

	
	public void fireOrderApprovedEvent() {

		log.info("*************** Fire ORDER_APPROVED  ***************************");
		log.info("First Name = " + customerfullname);
		log.info("Phone = " + cutomerphone);
		log.info("orderId = " + orderId);
				
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			String callerAddress = (String)Contexts.getApplicationContext().get("caller.sip");
			String callerDomain = (String)Contexts.getApplicationContext().get("caller.domain");
			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
			Address fromAddress = sipFactory.createAddress(fromURI);
			Address toAddress = sipFactory.createAddress(cutomerphone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			// getting the contact address for the registered customer sip address
			String userContact= ((Map<String, String>)Contexts.getApplicationContext().get("registeredUsersMap")).get(cutomerphone);
			if(userContact != null && userContact.length() > 0) {
				// for customers using the registrar
				URI requestURI = sipFactory.createURI(userContact);
				sipServletRequest.setRequestURI(requestURI);
			} else {
				// for customers not using the registrar and registered directly their contact location
				URI requestURI = sipFactory.createURI(cutomerphone);
				sipServletRequest.setRequestURI(requestURI);
			}
			
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(MediaConnectionListener.PR_JNDI_NAME);
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
			connection.addConnectionListener(listener);
			connection.modify("$", null);
			sipServletRequest.getSession().setAttribute("customerName", customerfullname);
			sipServletRequest.getSession().setAttribute("customerPhone", cutomerphone);
			sipServletRequest.getSession().setAttribute("amountOrder", amount);
			sipServletRequest.getSession().setAttribute("orderId", orderId);
			sipServletRequest.getSession().setAttribute("connection", connection);			
			sipServletRequest.getSession().setAttribute("deliveryDate", true);
			sipServletRequest.getSession().setAttribute("caller", (String)Contexts.getApplicationContext().get("caller.sip"));
			sipServletRequest.getSession().setAttribute("callerDomain", (String)Contexts.getApplicationContext().get("caller.domain"));
			sipServletRequest.getSession().setAttribute("callerPassword", (String)Contexts.getApplicationContext().get("caller.password"));
		} catch (UnsupportedOperationException uoe) {
			log.error("An unexpected exception occurred while trying to create the request for delivery date", uoe);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while trying to create the request for delivery date", e);
		}		
	}

	public void fireOrderRejectedEvent() {
		log.info("*************** Fire ORDER_REJECTED  ***************************");
		log.info("First Name = " + customerfullname);
		log.info("Phone = " + cutomerphone);
		log.info("orderId = " + orderId);

		
	}	

}
