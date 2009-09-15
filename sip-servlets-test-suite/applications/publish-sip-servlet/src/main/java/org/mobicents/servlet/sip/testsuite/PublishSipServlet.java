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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class PublishSipServlet extends SipServlet implements SipServletListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static final String SUBSCRIBER_SESSIONS = "subscriberSessions";
	private static final String PUBLISH_SIP_ETAGS = "publishSIP-ETag_s";
	private static transient Logger logger = Logger.getLogger(PublishSipServlet.class);
	
	
	/** Creates a new instance of PublishSipServlet */
	public PublishSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the publish sip servlet has been started");
		super.init(servletConfig);
		getServletContext().setAttribute(SUBSCRIBER_SESSIONS, new HashMap<URI, SipSession>());
		getServletContext().setAttribute(PUBLISH_SIP_ETAGS, new HashMap<URI, String>());
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if("PUBLISH".equals(resp.getMethod()) && !"Modify".equals(getServletContext().getAttribute("publishStateSent"))) {
			SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			SipURI fromURI = sipFactory.createSipURI("presentity", "example.com");			
			SipURI toURI = sipFactory.createSipURI("presentity", "example.com");
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "PUBLISH", fromURI, toURI);
			SipURI requestURI = sipFactory.createSipURI("presentity", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(requestURI);
			sipServletRequest.setExpires(3600);
			sipServletRequest.setHeader("Event", "presence");
			sipServletRequest.setHeader("SIP-If-Match", resp.getHeader("Sip-ETag"));
			
			try {
				if(getServletContext().getAttribute("publishStateSent") != null) {
					sipServletRequest.setContent("Modify", "application/pidf+xml");
					getServletContext().setAttribute("publishStateSent", "Modify");
				} else {
					getServletContext().setAttribute("publishStateSent", "Refresh"); 
				}
				sipServletRequest.send();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doSubscribe(SipServletRequest request) throws ServletException,
			IOException {
		//If we receive a SUBSCRIBE, we cancel the timer to avoid acting as UAC and sending PUBLISH
		ServletTimer servletTimer = (ServletTimer)getServletContext().getAttribute("servletTimer");
		servletTimer.cancel();
		
		logger.info("Got Subscribe: "
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.addHeader("Expires", request.getHeader("Expires"));
		sipServletResponse.addHeader("Event", request.getHeader("Event"));
		sipServletResponse.send();		
		Map<URI, SipSession> subscriberSessions = (Map<URI, SipSession>) 
			getServletContext().getAttribute(SUBSCRIBER_SESSIONS);
		subscriberSessions.put(request.getRequestURI(), request.getSession());
		
		// send notify		
		SipServletRequest notifyRequest = request.getSession().createRequest("NOTIFY");
		notifyRequest.addHeader("Subscription-State", "active");
		notifyRequest.addHeader("Event", "presence");
		notifyRequest.send();
	}

	@Override
	protected void doPublish(SipServletRequest request) throws ServletException,
			IOException {
		Map<URI, SipSession> subscriberSessions = (Map<URI, SipSession>) 
			getServletContext().getAttribute(SUBSCRIBER_SESSIONS);
		Map<URI, String> sipETags = (Map<URI, String>) 
			getServletContext().getAttribute(PUBLISH_SIP_ETAGS);
	
		SipSession subscriberSession = subscriberSessions.get(request.getRequestURI());
		boolean sipIfMatchFound = true;
		String sipIfMatch = request.getHeader("SIP-If-Match");
		if(sipIfMatch != null && !sipIfMatch.equalsIgnoreCase(sipETags.get(request.getRequestURI()))) {
			sipIfMatchFound = false;
		}
		if(subscriberSession != null && sipIfMatchFound) {						
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.addHeader("Expires", request.getHeader("Expires"));
			String sipETag = Integer.toString((int) (Math.random()*10000000));
			sipETags.put(request.getRequestURI(), sipETag);
			sipServletResponse.addHeader("SIP-ETag", sipETag);
			sipServletResponse.send();
			
			if(request.getRawContent() != null) {
				String content = new String(request.getRawContent());
				if(content.length() > 0) {
					if(logger.isDebugEnabled()) {
						logger.debug("Content : "+content);
					}
					SipServletRequest notifyRequest = subscriberSession.createRequest("NOTIFY");
					notifyRequest.addHeader("Subscription-State", "active");
					notifyRequest.addHeader("Event", "presence");
					notifyRequest.send();
				}
			}
		} else {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			sipServletResponse.send();	
		}
	}
	
	// Listener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		TimerService timerService = (TimerService)ce.getServletContext().getAttribute(TIMER_SERVICE);
		sipApplicationSession.setAttribute("sipFactory", sipFactory);
		ServletTimer servletTimer = timerService.createTimer(sipApplicationSession, 2000, false, null);
		ce.getServletContext().setAttribute("servletTimer", servletTimer);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer timer) {
		SipFactory sipFactory = (SipFactory) timer.getApplicationSession().getAttribute("sipFactory");		
		SipURI fromURI = sipFactory.createSipURI("presentity", "example.com");			
		SipURI toURI = sipFactory.createSipURI("presentity", "example.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(timer.getApplicationSession(), "PUBLISH", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("presentity", "127.0.0.1:5080");
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.setExpires(3600);
		sipServletRequest.setHeader("Event", "presence");
		
		try {
			sipServletRequest.setContent("Initial", "application/pidf+xml");
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}
	}
}