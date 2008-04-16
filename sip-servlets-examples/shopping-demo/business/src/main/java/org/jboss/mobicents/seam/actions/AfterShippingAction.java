package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.mobicents.seam.util.TTSUtils;
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
			String adminAddress = (String)Contexts.getApplicationContext().get("admin.sip");
			Address fromAddress = sipFactory.createAddress(adminAddress);
			Address toAddress = sipFactory.createAddress(cutomerphone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(cutomerphone);
			sipServletRequest.setRequestURI(requestURI);
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
			
			TTSUtils.buildAudio(stringBuffer.toString(), "shipping.wav");
			Thread.sleep(300);
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer();
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection("media/trunk/IVR/1");
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
			connection.addConnectionListener(listener);
			connection.modify("$", null);
			sipApplicationSession.setAttribute("customerName", customerfullname);
			sipApplicationSession.setAttribute("customerPhone", cutomerphone);
			sipApplicationSession.setAttribute("amountOrder", amount);
			sipApplicationSession.setAttribute("orderId", orderId);
			sipApplicationSession.setAttribute("connection", connection);
			sipApplicationSession.setAttribute("shipping", true);
		} catch (UnsupportedOperationException uoe) {
			log.error("An unexpected exception occurred while trying to create the request for shipping call", uoe);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while trying to create the request for shipping call", e);
		}
	}
}
