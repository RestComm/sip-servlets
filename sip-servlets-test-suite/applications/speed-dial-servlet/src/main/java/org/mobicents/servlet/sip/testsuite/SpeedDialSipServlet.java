/*
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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class SpeedDialSipServlet extends SipServlet implements SipErrorListener,
		Servlet {
	
	private static transient Logger logger = Logger.getLogger(SpeedDialSipServlet.class);
	private static final String REMOTE_TRANSPORT = "udp";
	private static final int REMOTE_PORT = 5080;
	private static final String REMOTE_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String INITIAL_REMOTE_TRANSPORT = "udp";
	private static final int INITIAL_REMOTE_PORT = 5090;
	private static final String INITIAL_REMOTE_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String LOCAL_TRANSPORT = "udp";
	private static final int LOCAL_PORT = 5070;
	private static final String LOCAL_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String TEST_USER_REMOTE = "remote";
	
	Map<String, String> dialNumberToSipUriMapping = null;

	/** Creates a new instance of SpeedDialSipServlet */
	public SpeedDialSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the speed dial sip servlet has been started");
		super.init(servletConfig);
		dialNumberToSipUriMapping = new HashMap<String, String>();
		dialNumberToSipUriMapping.put("1", "sip:receiver@sip-servlets.com");
		dialNumberToSipUriMapping.put("2", "sip:mranga@sip-servlets.com");
		dialNumberToSipUriMapping.put("3", "sip:vlad@sip-servlets.com");
		dialNumberToSipUriMapping.put("4", "sip:bartek@sip-servlets.com");
		dialNumberToSipUriMapping.put("5", "sip:jeand@sip-servlets.com");
		dialNumberToSipUriMapping.put("6", "sip:receiver-failover@sip-servlets.com");
		dialNumberToSipUriMapping.put("b2bua", "sip:fromProxy@sip-servlets.com");
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());
		logger.info(request.getRequestURI().toString());
		
		if(((SipURI)request.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			if(request.getRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && request.getRemotePort() == REMOTE_PORT && request.getTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + request.getRemoteAddr());
				logger.error("remote port " + request.getRemotePort());
				logger.error("remote transport " + request.getTransport());
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect remote information");
				sipServletResponse.send();
				return;
			}
			if(request.getInitialRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && request.getInitialRemotePort() == REMOTE_PORT && request.getInitialTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + request.getInitialRemoteAddr());
				logger.error("Initial remote port " + request.getInitialRemotePort());
				logger.error("Initial remote transport " + request.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(request.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getLocalPort() == LOCAL_PORT && request.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + request.getLocalAddr());
				logger.error("local port " + request.getLocalPort());
				logger.error("local transport " + request.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
		
		String dialNumber = ((SipURI)request.getRequestURI()).getUser();
		String mappedUri = dialNumberToSipUriMapping.get(dialNumber);	
		if(mappedUri != null) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);			
			Proxy proxy = request.getProxy();
			proxy.setProxyTimeout(120);
			proxy.setRecordRoute(true);
			proxy.setParallel(false);
			proxy.setSupervised(true);
			logger.info("proxying to " + mappedUri);
			proxy.proxyTo(sipFactory.createURI(mappedUri));				
		} else {
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE, "No mapping for " + dialNumber);
			sipServletResponse.send();			
		}		
	}
	
	protected void doSuccessResponse(SipServletResponse resp)
		throws ServletException, IOException {
		logger.info("Got response " + resp);
		if(((SipURI)resp.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			if(resp.getRemoteAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getRemotePort() == LOCAL_PORT && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + resp.getRemoteAddr());
				logger.error("remote port " + resp.getRemotePort());
				logger.error("remote transport " + resp.getTransport());
				throw new IllegalArgumentException("remote information is incorrect");
			}
			if(resp.getInitialRemoteAddr().equals(INITIAL_REMOTE_LOCALHOST_ADDR) && resp.getInitialRemotePort() == INITIAL_REMOTE_PORT && resp.getInitialTransport().equalsIgnoreCase(INITIAL_REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + resp.getInitialRemoteAddr());
				logger.error("Initial remote port " + resp.getInitialRemotePort());
				logger.error("Initial remote transport " + resp.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(resp.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getLocalPort() == LOCAL_PORT && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + resp.getLocalAddr());
				logger.error("local port " + resp.getLocalPort());
				logger.error("local transport " + resp.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
	}

	// SipErrorListener methods
	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

}
