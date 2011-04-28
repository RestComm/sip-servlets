/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
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
	//jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/TimerService") TimerService timerService;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	@Resource(mappedName="java:/sip/shopping-demo/TimerService") TimerService timerService;

	public void fireOrderProcessedEvent() {
		
		log.info("SipFactory " + sipFactory);
		log.info("timerService " + timerService);
		
		log.info("*************** Fire ORDER_PROCESSED  ***************************");
		log.info("First Name = " + customerfullname);
		log.info("Phone = " + cutomerphone);
		log.info("orderId = " + orderId);		
        
		Map<String, Object> attributes = new HashMap<String, Object>();
		
        SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		attributes.put("customerName", customerfullname);
		attributes.put("customerPhone", cutomerphone);
		String userContact= ((Map<String, String>)Contexts.getApplicationContext().get("registeredUsersMap")).get(cutomerphone);
		if(userContact != null && userContact.length() > 0) {
			// for customers using the registrar
			attributes.put("customerContact", userContact);
		} else {
			// for customers not using the registrar and registered directly their contact location
			attributes.put("customerContact", cutomerphone);			
		}
		attributes.put("amountOrder", amount);
		attributes.put("orderId", orderId);									
		attributes.put("deliveryDate", true);
		attributes.put("caller", (String)Contexts.getApplicationContext().get("caller.sip"));
		attributes.put("callerDomain", (String)Contexts.getApplicationContext().get("caller.domain"));
		attributes.put("callerPassword", (String)Contexts.getApplicationContext().get("caller.password"));
		attributes.put("adminAddress", (String)Contexts.getApplicationContext().get("admin.sip"));
		attributes.put("adminContactAddress", (String)Contexts.getApplicationContext().get("admin.sip.default.contact"));
		ServletTimer servletTimer = timerService.createTimer(
				sipApplicationSession, 
				Integer.parseInt((String)Contexts.getApplicationContext().get("order.approval.waitingtime")), 
				false, 
				(Serializable)attributes);
		Contexts.getApplicationContext().set("deliveryDateTimer" + orderId, servletTimer);
	}

}
