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

package org.jboss.mobicents.seam;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.slee.EventTypeID;
import javax.slee.connection.ExternalActivityHandle;
import javax.slee.connection.SleeConnection;
import javax.slee.connection.SleeConnectionFactory;

import org.jboss.seam.annotations.JndiName;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.log.Log;
import org.mobicents.slee.service.events.InteropCustomEvent;

@Local
@Stateful
@Name("CallManagerBean")
@JndiName("jslee-sips/#{ejbName}/local")
@Synchronized
public class CallManagerBean implements CallManager, Serializable {
	@Logger private Log log;	
	
	@Resource(mappedName="java:/MobicentsConnectionFactory")
	SleeConnectionFactory factory;
	
	SipServletResponse sipServletResponse;
	ExternalActivityHandle handle;
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.mobicents.seam.CallManager#initMediaConnection(javax.servlet.sip.SipServletRequest)
	 */
	public void initMediaConnection(SipServletRequest request) throws IOException {
		log.info("initiating media connection");
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		fireEvent(
				null, 
				(byte[])request.getContent(), 
				"org.mobicents.slee.service.interopdemo.INIT_MEDIA", 
				request.getSession().getAttribute("callManagerRef"));
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void fireEvent(String boothNumber, byte[] sdpContent, String eventType, Object callManagerRef) {
		log.info("firing event into slee with following sdp : "+ sdpContent);
		try {								
			SleeConnection conn1 = factory.getConnection();
			if(handle == null) {
				handle = conn1.createActivityHandle();
			}

			EventTypeID requestType = conn1.getEventTypeID(
					eventType,
					"org.mobicents", "1.0");
			InteropCustomEvent interopCustomEvent = new InteropCustomEvent(boothNumber, sdpContent, callManagerRef);

			conn1.fireEvent(interopCustomEvent, requestType, handle, null);
			conn1.close();

		} catch (Exception e) {
			log.error("unexpected exception while firing the event " + eventType + " into jslee", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.mobicents.seam.CallManager#mediaConnectionCreated(java.lang.String)
	 */
	public void mediaConnectionCreated(String sdpContent) throws IOException {
		sipServletResponse.setContentLength(sdpContent.length());
		sipServletResponse.setContent(sdpContent.getBytes(), "application/sdp");
		sipServletResponse.send();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.mobicents.seam.CallManager#playAnnouncement(javax.servlet.sip.SipServletMessage, java.lang.String)
	 */
	public void playAnnouncement(SipServletMessage message, String annoucementType) {
		fireEvent(
				(String) message.getSession().getAttribute("boothNumber"), 
				null, 
				"org.mobicents.slee.service.interopdemo." + annoucementType, 
				message.getSession().getAttribute("callManagerRef"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.mobicents.seam.CallManager#endCall(javax.servlet.sip.SipServletMessage, boolean)
	 */
	public void endCall(SipServletMessage message, boolean callerTerminating) throws IOException {
		if(callerTerminating) {
			fireEvent(
				(String) message.getSession().getAttribute("boothNumber"), 
				null, 
				"org.mobicents.slee.service.interopdemo.END_MEDIA", 
				message.getSession().getAttribute("callManagerRef"));
		} else {
			sipServletResponse.getSession().createRequest("BYE").send();
		}
	}
	
	@Remove
	public void destroy() {
	}
}
