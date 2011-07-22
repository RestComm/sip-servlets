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

package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;


public class ServletMappingSipServlet extends SipServlet implements SipServletListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ServletMappingSipServlet.class);
	
	@Resource
    private SipFactory factory;
	
	/** Creates a new instance of ServletMappingSipServlet */
	public ServletMappingSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the servlet mamping sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		sendMessage("inviteReceived");
		
		logger.info("Got request: "
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request: " + request);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}

	public void servletInitialized(SipServletContextEvent ce) {
		try {
			if(ce.getSipServlet().equals(this)) {
	            sendMessage("servletInitialized");
			}
        } catch (Exception e) {
            logger.error("unexpected exception while trying to send the invite out", e);
        }
	}

	/**
	 * @throws ServletParseException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void sendMessage(String body) throws ServletParseException,
			UnsupportedEncodingException, IOException {
		SipApplicationSession appSession = factory.createApplicationSession();
		SipServletRequest request =
		    factory.createRequest(appSession, "MESSAGE",
		                          "sip:from@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:to@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
		request.setContentLength(2);
		request.setContent(body, "text/plain;charset=UTF-8");
		request.send();
	}	

	

}