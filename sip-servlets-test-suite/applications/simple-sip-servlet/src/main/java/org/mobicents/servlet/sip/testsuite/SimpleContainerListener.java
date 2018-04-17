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
import javax.servlet.ServletContext;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.ContainerEvent;
import org.mobicents.javax.servlet.ContainerListener;
import org.mobicents.javax.servlet.RequestThrottledEvent;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
@SipListener
public class SimpleContainerListener implements ContainerListener {
	private static Logger logger = Logger
			.getLogger(SimpleContainerListener.class);

	@Resource SipFactory sipFactory;
	

	public void onRequestThrottled(RequestThrottledEvent event, ServletContext ctx) {
		SipServletResponse sipServletResponse = event.getRequest().createResponse(503, "");
		sipServletResponse.addHeader("Reason", event.getReason().toString());
		sipServletResponse.addHeader("ReasonMessage", event.getMessage());
                ctx.setAttribute("org.mobicents.servlet.sip.THROTTLED_RESPONSE", sipServletResponse);
	}


    public void sendEvent(ContainerEvent event, ServletContext ctx) {
        logger.info("Received event:" + event.getEventType().toString());
        SimpleSipServlet.sendMessage(sipFactory.createApplicationSession(), sipFactory, event.getEventType().toString(), null);
        switch (event.getEventType()){
            case CONGESTION_STARTED:
                break;
            case CONGESTION_STOPPED:
                break;
            case GRACEFUL_SHUTDOWN_STARTED:
                break;
            case GRACEFUL_SHUTDOWN_CHECK:
                ctx.setAttribute("PREVENT_GRACEFULL", true);
                break;
            case REQUEST_THROTTLED:
                onRequestThrottled((RequestThrottledEvent) event, ctx);
                break;
            default:
            
        }
    }

	
}
