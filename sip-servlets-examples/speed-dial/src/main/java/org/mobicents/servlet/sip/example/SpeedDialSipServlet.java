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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * This example shows Speed Dial capabilities.
 * User can type a number on their sip phone and this will be translated to their favorite
 * hard coded address and then proxied to it.
 * @author Jean Deruelle
 *
 */
public class SpeedDialSipServlet extends SipServlet {

	private static transient final Logger logger = Logger.getLogger(SpeedDialSipServlet.class);
	Map<String, String> dialNumberToSipUriMapping = null;
	
	/** Creates a new instance of SpeedDialSipServlet */
	public SpeedDialSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the speed dial sip servlet has been started");
		super.init(servletConfig);
		dialNumberToSipUriMapping = new HashMap<String, String>();
		dialNumberToSipUriMapping.put("1", "sip:receiver@sip-servlets.com"); 
		dialNumberToSipUriMapping.put("2", "sip:mranga@sip-servlets.com"); // The Master Blaster
		dialNumberToSipUriMapping.put("3", "sip:vlad@sip-servlets.com");
		dialNumberToSipUriMapping.put("4", "sip:bartek@sip-servlets.com");
		dialNumberToSipUriMapping.put("5", "sip:jeand@sip-servlets.com");
		dialNumberToSipUriMapping.put("9", "sip:receiver@127.0.0.1:5090"); // special case to be able to run the app standalone
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());
		logger.info(request.getRequestURI().toString());
		
		String dialNumber = ((SipURI)request.getRequestURI()).getUser();
		String mappedUri = dialNumberToSipUriMapping.get(dialNumber);	
		if(mappedUri != null) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Proxy proxy = request.getProxy();
			proxy.setRecordRoute(false);
			proxy.setParallel(false);
			proxy.setSupervised(false);
			logger.info("proxying to " + mappedUri);
			proxy.proxyTo(sipFactory.createURI(mappedUri));				
		} else {
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE, "No mapping for " + dialNumber);
			sipServletResponse.send();			
		}		
	}
}
