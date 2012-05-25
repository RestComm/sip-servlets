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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ProxyBranchListener;
import org.mobicents.javax.servlet.sip.ProxyExt;
import org.mobicents.javax.servlet.sip.ResponseType;
import org.mobicents.javax.servlet.sip.SipFactoryExt;
import org.mobicents.javax.servlet.sip.SipServletRequestExt;
import org.mobicents.javax.servlet.sip.SipServletResponseExt;

public class ProxySipServlet extends SipServlet implements SipErrorListener, ProxyBranchListener, SipSessionListener, SipApplicationSessionListener, TimerListener {
	private static final String ERROR = "ERROR";
	private static final String SIP_APPLICATION_SESSION_TIMEOUT = "sipApplicationSessionTimeout";
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ProxySipServlet.class);
	String host = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	private static String USE_HOSTNAME = "useHostName";	
	private static String CHECK_URI = "check_uri";
	private static String TEST_2_TRYING = "test_2_trying";
	private static String CHECK_READY_TO_INVALIDATE = "check_rti";
	private static String NON_RECORD_ROUTING = "nonRecordRouting";
	private static String RECORD_ROUTING = "recordRouting";
	private static String TEST_CREATE_SUBSEQUENT_REQUEST = "test_create_subsequent_request";
	private static String TEST_TERMINATION = "test_termination";
	private static String REGISTER_OUTBOUND = "register-outbound";
	private static String INVITE_INBOUND = "invite-inbound";
	private static final String BRANCHES = "branches";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";

	@Resource TimerService timerService;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the proxy sip servlet has been started");
		super.init(servletConfig);
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		req.getProxy().setAddToPath(true);
		URI uri1 = sipFactory.createAddress("sip:receiver@" + host + ":5057").getURI();
		((ProxyExt)req.getProxy()).setSipOutboundSupport(true);
		req.getProxy().proxyTo(uri1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		SipApplicationSession sas = request.getApplicationSession();
		logger.info("Got request:\n" + request.getMethod());
		SipServletRequestExt req = (SipServletRequestExt)request;
		if(req.isOrphan() && !req.isInitial()) {
			req.getSession().setAttribute("h", "hhh");
			return;
		}
		if(request.getFrom().toString().contains("proxy-orphan")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			SipFactoryExt sipFactoryExt = (SipFactoryExt) sipFactory;
			request.getApplicationSession(false);
			sipFactoryExt.setRouteOrphanRequests(true);
			Object o = getServletContext().getAttribute(javax.servlet.sip.SipServlet. OUTBOUND_INTERFACES);
			request.getProxy().setRecordRoute(true);
			request.getProxy().proxyTo(sipFactory.createURI("sip:a@127.0.0.1:5090;transport=udp"));
			return;
		}
		if(request.getFrom().toString().contains("proxy-tcp")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Object o = getServletContext().getAttribute(javax.servlet.sip.SipServlet. OUTBOUND_INTERFACES);
			request.getProxy().setRecordRoute(true);
			request.getProxy().proxyTo(sipFactory.createURI("sip:a@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090;transport=tcp"));
			return;
		}
		
		if(request.getFrom().toString().contains("proxy-udp")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Object o = getServletContext().getAttribute(javax.servlet.sip.SipServlet. OUTBOUND_INTERFACES);
			request.getProxy().setRecordRoute(true);
			request.getProxy().proxyTo(sipFactory.createURI("sip:a@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090;transport=udp"));
			return;
		}
		
		if(request.getFrom().toString().contains("proxy-unspecified")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Object o = getServletContext().getAttribute(javax.servlet.sip.SipServlet. OUTBOUND_INTERFACES);
			request.getProxy().setRecordRoute(true);
			request.getProxy().proxyTo(sipFactory.createURI("sip:a@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"));
			return;
		}
				
		if(request.getFrom().toString().contains("proxy-tls")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Object o = getServletContext().getAttribute(javax.servlet.sip.SipServlet. OUTBOUND_INTERFACES);
			request.getProxy().setRecordRoute(true);
			request.getProxy().proxyTo(sipFactory.createURI("sips:a@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090;transport=tls"));
			return;
		}
		String error = (String) request.getApplicationSession().getAttribute(ERROR);
		if(error != null) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, error);
			sipServletResponse.send();
			return;
		}
		final String from = request.getFrom().getURI().toString();
		SipURI fromURI = ((SipURI)request.getFrom().getURI());		
		logger.info("invalidate when ready "
				+ request.getApplicationSession().getInvalidateWhenReady());
		if(fromURI.getUser().equals(CHECK_READY_TO_INVALIDATE)) {
			if(!request.getApplicationSession().getInvalidateWhenReady()) {
				SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
				sipServletResponse.send();
				return;
			}
		}
		Address contactAddress = request.getAddressHeader("Contact");
		int contactPort = ((SipURI)contactAddress.getURI()).getPort();
		request.getApplicationSession().setAttribute("contactPort", contactPort);
		request.getSession().setAttribute("contactPort", contactPort);
		if(fromURI.getUser().equals(SIP_APPLICATION_SESSION_TIMEOUT)) {			
			logger.info("testing session expiration, setting invalidateWhenReady to false");
			request.getApplicationSession().setAttribute(SIP_APPLICATION_SESSION_TIMEOUT, "true");			
		}
		if(!request.isInitial()){
			return;
		}
						
		//This is a proxying sample.
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			
		if(fromURI.getUser().contains(USE_HOSTNAME)) {		
			host = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
			logger.info("using Host Name for proxy test");
		}

		URI uri1 = sipFactory.createAddress("sip:receiver@" + host + ":5057").getURI();		
		URI uri2 = sipFactory.createAddress("sip:cutme@" + host + ":5056").getURI();
		URI uri3 = sipFactory.createAddress("sip:nonexist@" + host + ":5856").getURI();
		String via = request.getHeader("Via");
		String transport = "udp";
		if(via.contains("TCP") || via.contains("tcp")) {
			transport = "tcp";
			logger.info("setting transport param to " + transport);
			((SipURI)uri1).setTransportParam(transport);
			((SipURI)uri2).setTransportParam(transport);
			((SipURI)uri3).setTransportParam(transport);
			request.getApplicationSession().setAttribute("transport", transport);
			request.getSession().setAttribute("transport", transport);			
		} else {
			request.getApplicationSession().setAttribute("transport", transport);
			request.getSession().setAttribute("transport", transport);
		}
		
		if(from.contains("forward-sender-downstream-proxy")) {
			URI uri = sipFactory.createAddress("sip:receiver@" + host + ":5070").getURI();
			Proxy proxy = request.getProxy();
			proxy.setParallel(false);
			proxy.setRecordRoute(true);
			proxy.setProxyTimeout(5);
			logger.info("proxying to downstream proxy" + uri);
			proxy.proxyTo(uri);
			return;
		}
		
		if(from.contains("unique-location-urn")) {
			Proxy proxy = request.getProxy();
			proxy.setParallel(false);
			proxy.setRecordRoute(true);
			proxy.setProxyTimeout(5);
			logger.info("proxying to downstream proxy" + request.getRequestURI());
			if(from.contains("route")) {
				request.pushRoute((SipURI)uri1);
			}
			proxy.proxyTo(request.getRequestURI());
			return;
		}
		
		if(from.contains("sequential")) {
			Proxy proxy = request.getProxy();
			proxy.setParallel(false);
			proxy.setProxyTimeout(5);
			if(from.contains("1xxResponseTimeout")) {
				((ProxyExt)proxy).setProxy1xxTimeout(1);				
			}
			if(from.contains("finalResponseTimeout")) {				
				proxy.setProxyTimeout(2);				
			}
			proxy.setRecordRoute(true);
			ArrayList<URI> uris = new ArrayList<URI>();
			if(from.contains("sequential-reverse")) {
				uris.add(uri1);
				if(!from.contains("sequential-reverse-one")) {
					uris.add(uri2);
				}
			} else if(from.contains("sequential-three")) {
				uris.add(uri3);
				uris.add(uri2);
				uris.add(uri1);
			} else if(from.contains("sequential-cut")) {
				uris.add(uri2);
			} else if(from.contains("nonexist")) {
				uris.add(uri3);
				proxy.setProxyTimeout(40);
			} else {
				uris.add(uri2);
				uris.add(uri1);
			}

			proxy.proxyTo(uris);
		} else {
			Proxy proxy = request.getProxy();
			ArrayList<URI> uris = new ArrayList<URI>();
			uris.add(uri1);
			if(!fromURI.getUser().contains("unique-location") && !fromURI.getUser().contains("prack") && !fromURI.getUser().contains(REGISTER_OUTBOUND)) {
				uris.add(uri2);
			}
			if(fromURI.getUser().contains(REGISTER_OUTBOUND)) {
				((ProxyExt)proxy).setSipOutboundSupport(true);
			}
			if(fromURI.getUser().contains(INVITE_INBOUND)) {
				((ProxyExt)proxy).setSipOutboundSupport(true);
				uris.clear();
				SipURI sipURI = sipFactory.createSipURI("receiver", host );
				sipURI.setPort(5080);
				if(via.contains("TCP") || via.contains("tcp")) {					
					sipURI.setTransportParam("tcp");
					uris.add(sipURI);
				} else {
					uris.add(sipURI);
				}
			}			
			List<SipURI> outboundInterfaces = (List<SipURI>)getServletContext().getAttribute(OUTBOUND_INTERFACES);

			if(outboundInterfaces == null) throw new NullPointerException("Outbound interfaces should not be null");

			SipURI obi = null;

			for(SipURI uri:outboundInterfaces) {
				logger.info("Outbound interface : " + uri);
				if(uri.toString().indexOf("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "")>0 && uri.getTransportParam().equalsIgnoreCase(transport)) {
						
					// pick the lo interface, since its universal on all machines
					proxy.setOutboundInterface(new InetSocketAddress(InetAddress.getByName(uri.getHost()),uri.getPort()));
					obi = uri;
					break;
				}
				if(System.getProperty("org.mobicents.testsuite.testhostaddr").contains("::1")) {
					if(uri.toString().contains(":1")) {
						obi = uri;break;
					}
				}
			}

			if(obi == null) throw new NullPointerException("No loopback interface found.");

			boolean recordRoute = true;
			if(fromURI.getUser().contains(NON_RECORD_ROUTING)) {		
				recordRoute = false;
				logger.info("not record routing");
			}
			if(fromURI.getUser().contains(TEST_TERMINATION)) {		
				((ProxyExt)proxy).storeTerminationInformation(true);
				logger.info("testing termination");
			}
			//proxy.setOutboundInterface((SipURI)sipFactory.createAddress("sip:proxy@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070").getURI());
			proxy.setRecordRoute(recordRoute);
			proxy.setSupervised(true);
			if(recordRoute) {
				proxy.getRecordRouteURI().setParameter("testparamname", "TESTVALUE");
			}		
			proxy.setParallel(true);
			if(CHECK_URI.equals(fromURI.getUser())) {
				Address routeAddress = sipFactory.createAddress("sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057");
				request.pushRoute(routeAddress);
				Address ra = request.getAddressHeader("Route");
				logger.info("doInvite: ra = " + ra);
				URI uri = ra.getURI(); // this causes NPE
				logger.info("doInvite: uri = " + uri);
				proxy.setParallel(false);
			}
			if (from.contains("update")){
				proxy.setProxyTimeout(40);
			} else {
				proxy.setProxyTimeout(4);
			}
			if(TEST_2_TRYING.equals(fromURI.getUser())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(fromURI.getUser().contains(BRANCHES)) {		
				List<ProxyBranch> proxyBranches = proxy.createProxyBranches(uris);
				for(ProxyBranch proxyBranch : proxyBranches) {
					proxyBranch.getRequest().setContent("test", CONTENT_TYPE);
				}
				proxy.startProxy();
			} else {
				proxy.proxyTo(uris);
			}
		}
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		SipURI fromURI = ((SipURI)request.getFrom().getURI());
		String from = fromURI.toString();
		if(request.getFrom().toString().contains("proxy-orphan")) {
			request.getSession().invalidate();
			request.getApplicationSession().invalidate();
			return;
		}
		if(from.contains("unique-location-urn-route")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			URI uri1 = sipFactory.createAddress("sip:receiver@" + host + ":5057").getURI();
			request.pushRoute((SipURI)uri1);			
		}
		if(from.contains(TEST_TERMINATION)) {
			timerService.createTimer(request.getApplicationSession(), 10000, false, (Serializable) request);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(fail) {
			request.createResponse(500).send();
			throw new ServletException("Failed because of double response");
		}
		logger.info("Got BYE request:\n" + request);
		SipURI fromURI = ((SipURI)request.getFrom().getURI());
		String from = fromURI.toString();
		SipServletRequestExt sipServletRequestExt = (SipServletRequestExt) request;
		if(sipServletRequestExt.isOrphan()) return;
		if(from.contains("unique-location-urn-route")) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			URI uri1 = sipFactory.createAddress("sip:receiver@" + host + ":5057").getURI();
			request.pushRoute((SipURI)uri1);			
		}
		
		if(fromURI.toString().contains(TEST_CREATE_SUBSEQUENT_REQUEST)) {
			try {
				request.getSession().createRequest("BYE");
			} catch(IllegalStateException ise) {
				logger.error("Got expected IllegalStateException on trying to create a subsequent request on a proxy session", ise);
				throw ise;
			}
		}
		
		SipServletResponse sipServletResponse = request.createResponse(200);
		
		// If the branchResponse callback was called we are good otherwise fail by
		// not delivering OK to BYE
		String doBranchRespValue = (String) request.getApplicationSession().getAttribute("branchResponseReceived");
		if("true".equals(doBranchRespValue))
//			sipServletResponse.send();
		

		logger.info("invalidate when ready "
				+ request.getApplicationSession().getInvalidateWhenReady());
		if(fromURI.getUser().equals(CHECK_READY_TO_INVALIDATE)) {
			request.getApplicationSession().setExpires(1);
		}
	}
	
	long lastOKstamp=0;
	SipServletResponse oldResp;
	boolean fail;

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		
		logger.info("Got response: " + response);
		logger.info("Sip Session is :" + response.getSession(false));
		
		SipServletResponseExt sipServletResponseExt = (SipServletResponseExt) response;
		SipApplicationSession sas = response.getApplicationSession();
		SipServletRequest re = response.getRequest();
//		re.getCallId();
		if(sipServletResponseExt.isOrphan()) {
			sipServletResponseExt.getApplicationSession(false);
			return;
		}
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		SipFactoryExt ext = (SipFactoryExt)sipFactory;
		if(!ext.isRouteOrphanRequests()) { // not to break the orphan test
			long delta = Math.abs(System.currentTimeMillis() - lastOKstamp);			
			if(response.getStatus() == 200) {								
				if(delta < 20 & oldResp != null && response != null && oldResp.getCallId().equals(response.getCallId())) {					
					fail = true;
					throw new ServletException("Problem with double response delta=" + delta + "\n1:" + oldResp + "\n2:"
							+ response);
					// This means we receive two responses within 20ms which can't be a retransmission but an indication of a bug delivering responses twice http://code.google.com/p/mobicents/issues/detail?id=2821
				}
				lastOKstamp = System.currentTimeMillis();
				oldResp = response;
			}
		}

		
		if(!"PRACK".equals(response.getMethod()) && response.getProxy() != null && response.getProxy().getOriginalRequest() != null) {
			logger.info("Original Sip Session is :" + response.getProxy().getOriginalRequest().getSession(false));
		}
		if(response.getFrom().getURI().toString().contains("sequential-retransmission")) {
			if(response.getMethod().equals("INVITE")) {
				if(response.getStatus() == 200) {
					String lastOK = (String) response.getSession().getAttribute("lastOK");
					if(lastOK != null) {
						if(!response.toString().equals(lastOK)) {
							// We expect to see the retransmissions. Fail the whole test in an ugly way otherwise.
							System.out.print("ERROR ERROR ERROR : // We expect to see the retransmissions. Fail the whole test in an ugly way otherwise.\nERROR\ndsfdsf\n\n\n\n\n\n\nERROR'n");
							System.exit(0);
						}
					}
					response.getSession().setAttribute("lastOK", response.toString());
				}
			}
		}
		response.toString();
		super.doResponse(response);
	}
	
	@Override
	protected void doBranchResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("doBranchResponse callback was called " + resp);		
		resp.getApplicationSession().setAttribute("branchResponseReceived", "true");		
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
	
    @Override
    protected void doSubscribe(SipServletRequest req) throws ServletException, IOException 
    {
    	if(req.isInitial()) {
	        SipURI uri = (SipURI)req.getRequestURI().clone();
	        uri.setPort(5057);
	        req.pushRoute(uri);
	        Proxy proxy = req.getProxy(true);
	        final String from = req.getFrom().getURI().toString();
			if(from.contains(RECORD_ROUTING)) {
				proxy.setRecordRoute(true);
			}
	        proxy.proxyTo(req.getRequestURI());
    	}
    }

    @Override
    protected void doPublish(SipServletRequest req) throws ServletException, IOException 
    {
        SipURI uri = (SipURI)req.getRequestURI().clone();
        uri.setPort(5057);
        req.pushRoute(uri);
        req.getProxy(true).proxyTo(req.getRequestURI());
    }
	
    @Override
    protected void doCancel(SipServletRequest req) throws ServletException,
    		IOException {
    	logger.error("CANCEL seen at proxy " + req);
    }

	public void onProxyBranchResponseTimeout(ResponseType responseType,
			ProxyBranch proxyBranch) {
		logger.info("onProxyBranchResponseTimeout callback was called. responseType = " + responseType + " , branch = " + proxyBranch + ", request " + proxyBranch.getRequest() + ", response " + proxyBranch.getResponse());
		if(proxyBranch.getRequest() != null && (proxyBranch.getRequest().getFrom().getURI().toString().contains("ResponseTimeout") || proxyBranch.getRequest().getFrom().getURI().toString().contains("unique-location"))) {
			sendMessage(responseType.toString(), 5080, "udp");
		}
	}

	/**
	 * @param sipApplicationSession
	 * @param storedFactory
	 */
	private void sendMessage(String content, int port, String transport) {
		try {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipFactory.createApplicationSession(), 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri = sipFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":"+ port);
			sipUri.setTransportParam(transport);
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(content.length());
			sipServletRequest.setContent(content, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}

	public void sessionCreated(SipApplicationSessionEvent ev) {
		logger.info("sessionCreated " +  ev.getApplicationSession().getId());
		String expires = getServletContext().getInitParameter(SIP_APPLICATION_SESSION_TIMEOUT);
		if(expires != null) {
			logger.info("setting expires to " +  expires);
			long now = System.currentTimeMillis();
			ev.getApplicationSession().setExpires(Integer.valueOf(expires));
			long expirationTime = ev.getApplicationSession().getExpirationTime();
			logger.info("expirationTime " +  expirationTime);
			if(expirationTime < (now + (Integer.valueOf(expires) * 60 * 1000L))) {
				ev.getApplicationSession().setAttribute(ERROR, "sip App Sesion getExpirationTime() returns incorrect value");
			}
		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		logger.info("sessionDestroyed " +  ev.getApplicationSession().getId());
	}

	public void sessionExpired(SipApplicationSessionEvent ev) {
		logger.info("sessionExpired " +  ev.getApplicationSession().getId());
		if(ev.getApplicationSession().getAttribute(SIP_APPLICATION_SESSION_TIMEOUT) != null) {
			sendMessage("sessionExpired", (Integer) ev.getApplicationSession().getAttribute("contactPort"), (String) ev.getApplicationSession().getAttribute("transport"));
		}
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		logger.info("sessionReadyToInvalidate " +  ev.getApplicationSession().getId());
		if(ev.getApplicationSession().getAttribute("contactPort") != null) {
			sendMessage("sessionReadyToInvalidate", (Integer) ev.getApplicationSession().getAttribute("contactPort"), (String) ev.getApplicationSession().getAttribute("transport"));
			if(ev.getApplicationSession().getAttribute(SIP_APPLICATION_SESSION_TIMEOUT) != null) {
				ev.getApplicationSession().setInvalidateWhenReady(false);
			}
		}
	}

	public void sessionCreated(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		logger.info("sipSessionReadyToInvalidate " +  se.getSession().getId());
		if(se.getSession().getAttribute("contactPort") != null) {
			sendMessage("sipSessionReadyToInvalidate", (Integer) se.getSession().getAttribute("contactPort"), (String) se.getSession().getAttribute("transport"));
		}
	}
	
	@Override
	protected void doUpdate(SipServletRequest req) throws ServletException, IOException {
		logger.info("Got request:\n "+req);
	}

	//@Override
	public void timeout(ServletTimer timer) {
		SipServletRequest request = (SipServletRequest)timer.getInfo();
		try {
			((ProxyExt)request.getProxy()).terminateSession(request.getSession(), 500, "Testing termination", 500, "Testing Termination");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TooManyHopsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}