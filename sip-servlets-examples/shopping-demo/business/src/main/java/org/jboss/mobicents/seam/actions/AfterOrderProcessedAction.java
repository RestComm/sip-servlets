package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;

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

/**
 * Used for setting date and time for delivery
 * 
 * @author amit.bhayani
 * 
 */

@Stateless
@Name("afterOrderProcessed")
public class AfterOrderProcessedAction implements AfterOrderProcessed, Serializable {
	@Logger private Log log;
	
	@In
	String customerfullname;

	@In
	String cutomerphone;

	@In
	BigDecimal amount;

	@In
	Long orderId;
	
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	@Resource(mappedName="java:/sip/shopping-demo/TimerService") TimerService timerService;

	public void fireOrderProcessedEvent() {
		
		log.info("SipFactory " + sipFactory);
		log.info("timerService " + timerService);
		
		log.info("*************** Fire ORDER_PROCESSED  ***************************");
		log.info("First Name = " + customerfullname);
		log.info("Phone = " + cutomerphone);
		log.info("orderId = " + orderId);		
        
        SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		sipApplicationSession.setAttribute("customerName", customerfullname);
		sipApplicationSession.setAttribute("customerPhone", cutomerphone);
		sipApplicationSession.setAttribute("amountOrder", amount);
		sipApplicationSession.setAttribute("orderId", orderId);									
		sipApplicationSession.setAttribute("deliveryDate", true);
		sipApplicationSession.setAttribute("sipFactory", sipFactory);
		ServletTimer servletTimer = timerService.createTimer(
				sipApplicationSession, 
				Integer.parseInt((String)Contexts.getApplicationContext().get("order.approval.waitingtime")), 
				false, 
				null);
		Contexts.getApplicationContext().set("deliveryDateTimer" + orderId, servletTimer);
	}

}
