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

import javax.annotation.Resource;
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
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
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
public class ReferSipServlet extends SipServlet implements SipServletListener, TimerListener, SipSessionListener {
	private static final long serialVersionUID = 1L;
	private static final String REFER_SESSION = "referSession";
	private static transient Logger logger = Logger.getLogger(ReferSipServlet.class);
	private final static String TRYING_CONTENT = "SIP/2.0 100 Trying";
	private final static String OK_CONTENT = "SIP/2.0 200 OK";
	private final static String SUBSEQUENT_CONTENT = "SIP/2.0 100 Subsequent";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String SIP_SESSION_READY_TO_BE_INVALIDATED = "sipSessionReadyToBeInvalidated";
	
	@Resource
	SipFactory sipFactory;
	
	/** Creates a new instance of ReferSipServlet */
	public ReferSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the refer sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("from : " + request.getFrom());
		logger.info("Got request: "
				+ request.getMethod());
	
		//If we receive an INVITE, we cancel the timer to avoid acting as UAC and sending REFER
		ServletTimer servletTimer = (ServletTimer)getServletContext().getAttribute("servletTimer");
		servletTimer.cancel();
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		request.getSession().setAttribute("inviteReceived", "true");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doRefer(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got REFER: "
				+ request);
		//If we receive a Refer, we cancel the timer to avoid acting as UAC and sending REFER
		ServletTimer servletTimer = (ServletTimer)getServletContext().getAttribute("servletTimer");
		servletTimer.cancel();
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_ACCEPTED);
		sipServletResponse.send();		
		// send notify		
		SipServletRequest notifyRequest = request.getSession().createRequest("NOTIFY");
		notifyRequest.addHeader("Subscription-State", "active;expires=3600");
		notifyRequest.addHeader("Event", "refer");
		if(request.isInitial() || request.getSession().getAttribute("inviteReceived") != null) {
			notifyRequest.setContentLength(TRYING_CONTENT.length());
			notifyRequest.setContent(TRYING_CONTENT, "message/sipfrag;version=2.0");
		} else {
			notifyRequest.setContentLength(TRYING_CONTENT.length());
			notifyRequest.setContent(SUBSEQUENT_CONTENT, "message/sipfrag;version=2.0");	
		}
		notifyRequest.send();

		if(request.isInitial() || request.getSession().getAttribute("inviteReceived") != null) {
			request.getSession().removeAttribute("inviteReceived");
			SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			sipApplicationSession.setAttribute(REFER_SESSION, request.getSession());
			String inviteUri = request.getHeader("Refer-To");
			SipURI fromURI = sipFactory.createSipURI("receiver", "example.com");			
			URI toURI = sipFactory.createURI(inviteUri.substring(1,inviteUri.length()-1));
			SipURI requestURI = sipFactory.createSipURI(((SipURI)toURI).getUser(), "127.0.0.1:5090");
			SipServletRequest inviteRequest = sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, toURI);
			inviteRequest.setRequestURI(requestURI);
			inviteRequest.setHeader("Referred-By", request.getFrom().getURI().toString());
			try {
				inviteRequest.send();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if("INVITE".equals(resp.getMethod())) {
			resp.createAck().send();
			SipSession referSession = (SipSession) resp.getApplicationSession().getAttribute(REFER_SESSION);
			if(referSession != null) {
				SipServletRequest notifyRequest = referSession.createRequest("NOTIFY");
				notifyRequest.addHeader("Subscription-State", "terminated;reason=noresource");
				notifyRequest.addHeader("Event", "refer");
				notifyRequest.setContentLength(OK_CONTENT.length());
				notifyRequest.setContent(OK_CONTENT, "message/sipfrag;version=2.0");
				notifyRequest.send();
			}
		}
	}
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(SipServletResponse.SC_OK).send();
	}
	
	@Override
	protected void doNotify(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(SipServletResponse.SC_OK).send();
	
		if(req.getRawContent() != null) {
			String content = new String(req.getRawContent());
			if("SIP/2.0 200 OK".equals(content) && req.getHeader("Out-Of-Dialog") == null) {
				SipServletRequest sipServletRequest = req.getSession().createRequest("REFER");
				SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
				SipURI requestURI = sipFactory.createSipURI("sender", "127.0.0.1:5080");
				sipServletRequest.setRequestURI(requestURI);
				sipServletRequest.addHeader("Refer-To", "sip:refer-to@nist.gov");
				sipServletRequest.send();
			} else if ("SIP/2.0 100 Subsequent".equals(content)) {
				SipServletRequest messageRequest = req.getSession().createRequest("MESSAGE");
				SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
				SipURI requestURI = sipFactory.createSipURI("sender", "127.0.0.1:5080");
				messageRequest.setRequestURI(requestURI);
				messageRequest.setContentLength(req.getContentLength());
				messageRequest.setContent("SIP/2.0 100 Subsequent", "text/plain;charset=UTF-8");
				messageRequest.send();
			}
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
		SipURI fromURI = sipFactory.createSipURI("receiver", "nist.gov");			
		SipURI toURI = sipFactory.createSipURI("sender", "nist.gov");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(timer.getApplicationSession(), "REFER", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("sender", "127.0.0.1:5080");
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.addHeader("Refer-To", "sip:refer-to@nist.gov");		
		try {
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void sessionCreated(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		logger.info("sip session expired " +  se.getSession());
		
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipFactory.createApplicationSession(), 
					"MESSAGE", 
					se.getSession().getLocalParty(), 
					se.getSession().getRemoteParty());
			SipURI sipUri=sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(SIP_SESSION_READY_TO_BE_INVALIDATED.length());
			sipServletRequest.setContent(SIP_SESSION_READY_TO_BE_INVALIDATED, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
}