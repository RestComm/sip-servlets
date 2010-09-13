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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
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
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ProxyBranchListener;
import org.mobicents.javax.servlet.sip.ProxyExt;
import org.mobicents.javax.servlet.sip.ResponseType;

public class ProxySipServlet extends SipServlet implements SipErrorListener, ProxyBranchListener, SipSessionListener, SipApplicationSessionListener {
	private static final String ERROR = "ERROR";
	private static final String SIP_APPLICATION_SESSION_TIMEOUT = "sipApplicationSessionTimeout";
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ProxySipServlet.class);
	String host = "127.0.0.1";
	private static String USE_HOSTNAME = "useHostName";
	private static String CHECK_URI = "check_uri";
	private static String CHECK_READY_TO_INVALIDATE = "check_rti";
	private static String NON_RECORD_ROUTING = "nonRecordRouting";
	private static String RECORD_ROUTING = "recordRouting";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the proxy sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.getMethod());
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
			
		if(USE_HOSTNAME.equals(fromURI.getUser())) {		
			host = "localhost";
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
			ArrayList<URI> uris = new ArrayList<URI>();
			uris.add(uri1);
			if(!fromURI.getUser().contains("unique-location")) {
				uris.add(uri2);
			}
			Proxy proxy = request.getProxy();
			List<SipURI> outboundInterfaces = (List<SipURI>)getServletContext().getAttribute(OUTBOUND_INTERFACES);

			if(outboundInterfaces == null) throw new NullPointerException("Outbound interfaces should not be null");

			SipURI obi = null;

			for(SipURI uri:outboundInterfaces) {
				logger.info("Outbound interface : " + uri);
				if(uri.toString().indexOf("127.0.0.1")>0 && uri.getTransportParam().equalsIgnoreCase(transport)) {
					// pick the lo interface, since its universal on all machines
					proxy.setOutboundInterface(new InetSocketAddress(InetAddress.getByName(uri.getHost()),uri.getPort()));
					obi = uri;
					break;
				}
			}

			if(obi == null) throw new NullPointerException("No loopback interface found.");

			boolean recordRoute = true;
			if(fromURI.getUser().contains(NON_RECORD_ROUTING)) {		
				recordRoute = false;
				logger.info("not record routing");
			}
			//proxy.setOutboundInterface((SipURI)sipFactory.createAddress("sip:proxy@127.0.0.1:5070").getURI());
			proxy.setRecordRoute(recordRoute);
			proxy.setSupervised(true);
			if(recordRoute) {
				proxy.getRecordRouteURI().setParameter("testparamname", "TESTVALUE");
			}		
			proxy.setParallel(true);
			if(CHECK_URI.equals(fromURI.getUser())) {
				Address routeAddress = sipFactory.createAddress("sip:127.0.0.1:5057");
				request.pushRoute(routeAddress);
				Address ra = request.getAddressHeader("Route");
				logger.info("doInvite: ra = " + ra);
				URI uri = ra.getURI(); // this causes NPE
				logger.info("doInvite: uri = " + uri);
				proxy.setParallel(false);
			}
			proxy.setProxyTimeout(4);
			proxy.proxyTo(uris);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request:\n" + request);
		SipServletResponse sipServletResponse = request.createResponse(200);
		
		// If the branchResponse callback was called we are good otherwise fail by
		// not delivering OK to BYE
		String doBranchRespValue = (String) request.getApplicationSession().getAttribute("branchResponseReceived");
		if("true".equals(doBranchRespValue))
			sipServletResponse.send();
		
		SipURI fromURI = ((SipURI)request.getFrom().getURI());
		logger.info("invalidate when ready "
				+ request.getApplicationSession().getInvalidateWhenReady());
		if(fromURI.getUser().equals(CHECK_READY_TO_INVALIDATE)) {
			request.getApplicationSession().setExpires(1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("Got response: " + response);
		logger.info("Sip Session is :" + response.getSession(false));
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
		logger.info("doBranchResponse callback was called.");		
		resp.getApplicationSession().setAttribute("branchResponseReceived", "true");
		super.doBranchResponse(resp);
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
		if(proxyBranch.getRequest() != null && proxyBranch.getRequest().getFrom().getURI().toString().contains("ResponseTimeout")) {
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
			SipURI sipUri = sipFactory.createSipURI("receiver", "127.0.0.1:"+ port);
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
}