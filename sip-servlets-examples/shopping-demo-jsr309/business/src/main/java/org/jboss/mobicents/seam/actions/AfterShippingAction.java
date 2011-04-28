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
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.listeners.DTMFListener;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.mobicents.seam.util.MMSUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

/**
 * An example of a Seam component used to handle a jBPM transition event.
 * 
 * @author Amit Bhayani
 */
@Stateless
@Name("afterShipping")
public class AfterShippingAction implements AfterShipping, Serializable {
	@Logger private Log log;
	
	@In
	String customerfullname;

	@In
	String cutomerphone;

	@In
	BigDecimal amount;

	@In
	Long orderId;
	
	@In
	Order order;
	
	//jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;


	public void orderShipped() {
		log.info("*************** Fire ORDER_SHIPPED  ***************************");
		log.info("First Name = " + customerfullname);
		log.info("Phone = " + cutomerphone);
		log.info("orderId = " + orderId);
		log.info("order = " + order);
		
		Timestamp orderDate = order.getDeliveryDate();
		
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
			//TTS file creation		
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Welcome ");
			stringBuffer.append(customerfullname);
			stringBuffer.append(". This is a reminder call for your order number ");
			stringBuffer.append(orderId);
			stringBuffer.append(". The shipment will be at your doorstep on .");
			stringBuffer.append(orderDate.getDate());
			stringBuffer.append(" of ");

			String month = null;

			switch (orderDate.getMonth()) {
			case 0:
				month = "January";
				break;
			case 1:
				month = "February";
				break;
			case 2:
				month = "March";
				break;
			case 3:
				month = "April";
				break;
			case 4:
				month = "May";
				break;
			case 5:
				month = "June";
				break;
			case 6:
				month = "July";
				break;
			case 7:
				month = "August";
				break;
			case 8:
				month = "September";
				break;
			case 9:
				month = "October";
				break;
			case 10:
				month = "November";
				break;
			case 11:
				month = "December";
				break;
			default:
				break;
			}

			stringBuffer.append(month);
			stringBuffer.append(" ");
			stringBuffer.append(1900 + orderDate.getYear());
			stringBuffer.append(" at ");
			stringBuffer.append(orderDate.getHours());
			stringBuffer.append(" hour and ");
			stringBuffer.append(orderDate.getMinutes());
			stringBuffer.append(" minute. Thank you. Bye.");				
			
			sipServletRequest.getSession().setAttribute("speechUri",
					java.net.URI.create("data:" + URLEncoder.encode("ts("+ stringBuffer +")", "UTF-8")));
			Thread.sleep(300);
			//Media Server Control Creation
			MediaSession mediaSession = MMSUtil.getMsControl().createMediaSession();
			NetworkConnection conn = mediaSession
			.createNetworkConnection(NetworkConnection.BASIC);

			SdpPortManager sdpManag = conn.getSdpPortManager();

			sdpManag.generateSdpOffer();


			byte[] sdpOffer = null;
			int numTimes = 0;
			while(sdpOffer == null && numTimes<10) {
				sdpOffer = sdpManag.getMediaServerSessionDescription();
				Thread.sleep(500);
				numTimes++;
			}
			
			sipServletRequest.setContentLength(sdpOffer.length);
			sipServletRequest.setContent(sdpOffer, "application/sdp");	
			MediaGroup mg = mediaSession.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			sipServletRequest.getSession().setAttribute("mediaGroup", mg);
			sipServletRequest.getSession().setAttribute("mediaSession", mediaSession);
			sipServletRequest.getSession().setAttribute("customerName", customerfullname);
			sipServletRequest.getSession().setAttribute("customerPhone", cutomerphone);
			sipServletRequest.getSession().setAttribute("amountOrder", amount);
			sipServletRequest.getSession().setAttribute("orderId", orderId);
			sipServletRequest.getSession().setAttribute("connection", conn);
			sipServletRequest.getSession().setAttribute("shipping", true);
			sipServletRequest.send();
			mg.getSignalDetector().addListener(new DTMFListener(mg, sipServletRequest.getSession(), MMSUtil.audioFilePath));
			conn.join(Direction.DUPLEX, mg);
		} catch (UnsupportedOperationException uoe) {
			log.error("An unexpected exception occurred while trying to create the request for shipping call", uoe);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while trying to create the request for shipping call", e);
		}
	}
}
