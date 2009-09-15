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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;


public class NoMainServletSipServlet extends SipServlet implements SipErrorListener, TimerListener {
	
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(NoMainServletSipServlet.class);
	
	/** Creates a new instance of SimpleProxyServlet */
	public NoMainServletSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the NoAppName sip servlet has been started");
		super.init(servletConfig);
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

	public void timeout(ServletTimer timer) {
		SipServletResponse sipServletResponse = (SipServletResponse)timer.getInfo();
		try {
			sipServletResponse.send();
		} catch (IOException e) {
			logger.error("Unexpected exception while sending the OK", e);
		}
	}

}