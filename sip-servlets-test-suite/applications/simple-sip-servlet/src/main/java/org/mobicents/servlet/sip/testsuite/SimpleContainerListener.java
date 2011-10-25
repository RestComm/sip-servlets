/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.servlet.sip.testsuite;

import javax.annotation.Resource;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.CongestionControlEvent;
import org.mobicents.javax.servlet.ContainerListener;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
@SipListener
public class SimpleContainerListener implements ContainerListener {
	private static Logger logger = Logger
			.getLogger(SimpleContainerListener.class);

	@Resource SipFactory sipFactory;
	
	public void onCongestionControlStarted(CongestionControlEvent event) {
		SimpleSipServlet.sendMessage(sipFactory.createApplicationSession(), sipFactory, "congestionControlStarted", null);
	}

	public void onCongestionControlStopped(CongestionControlEvent event) {
		SimpleSipServlet.sendMessage(sipFactory.createApplicationSession(), sipFactory, "congestionControlStopped", null);
	}

	public SipServletResponse onRequestThrottled(SipServletRequest request, CongestionControlEvent event) {
		SipServletResponse sipServletResponse = request.createResponse(503, "");
		sipServletResponse.addHeader("Reason", event.getReason().toString());
		sipServletResponse.addHeader("ReasonMessage", event.getMessage());
		return sipServletResponse;
	}

	
}
