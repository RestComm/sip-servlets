package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.TimerService;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

@Stateless
@Name("beforeOrderProcessed")
public class BeforeOrderProcessedAction implements BeforeOrderProcessed, Serializable {
	@Logger private Log log;
	
    @In String  customerfullname;
    @In String  cutomerphone;
    @In BigDecimal amount;
    @In Long orderId;	     
    
    //jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/TimerService") TimerService timerService;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	@Resource(mappedName="java:/sip/shopping-demo/TimerService") TimerService timerService;

    public void fireBeforeOrderProcessedEvent()
    {
        log.info( "***************Fire BEFORE_ORDER_PROCESSED . Custom event to call user to set date ***************************" );
        log.info( "Customer Name = " + customerfullname);	
        log.info( "Phone = " + cutomerphone);
        log.info( "orderId = " + orderId);
        log.info( "Amount = " + amount);
        
        SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		sipApplicationSession.setAttribute("customerName", customerfullname);
		sipApplicationSession.setAttribute("customerPhone", cutomerphone);
		// getting the contact address for the registered customer sip address
		String userContact= ((Map<String, String>)Contexts.getApplicationContext().get("registeredUsersMap")).get(cutomerphone);
		if(userContact != null && userContact.length() > 0) {
			// for customers using the registrar
			sipApplicationSession.setAttribute("customerContact", userContact);
		} else {
			// for customers not using the registrar and registered directly their contact location
			sipApplicationSession.setAttribute("customerContact", cutomerphone);			
		}
		sipApplicationSession.setAttribute("amountOrder", amount);
		sipApplicationSession.setAttribute("orderId", orderId);
		sipApplicationSession.setAttribute("adminApproval", true);
		sipApplicationSession.setAttribute("orderApproval", true);
		sipApplicationSession.setAttribute("sipFactory", sipFactory);		
		sipApplicationSession.setAttribute("caller", (String)Contexts.getApplicationContext().get("caller.sip"));
		sipApplicationSession.setAttribute("callerDomain", (String)Contexts.getApplicationContext().get("caller.domain"));
		sipApplicationSession.setAttribute("callerPassword", (String)Contexts.getApplicationContext().get("caller.password"));
		sipApplicationSession.setAttribute("adminAddress", (String)Contexts.getApplicationContext().get("admin.sip"));
		sipApplicationSession.setAttribute("adminContactAddress", (String)Contexts.getApplicationContext().get("admin.sip.default.contact"));
		ServletTimer servletTimer = timerService.createTimer(
				sipApplicationSession, 
				Integer.parseInt((String)Contexts.getApplicationContext().get("order.approval.waitingtime")), 
				false, 
				null);
		Contexts.getApplicationContext().set("adminTimer" + orderId, servletTimer);
    }     

}
