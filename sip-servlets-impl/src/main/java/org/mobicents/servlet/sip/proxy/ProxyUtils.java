package org.mobicents.servlet.sip.proxy;

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PathHeader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.sip.address.Address;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
/**
 * TODO: Use outbound interface from ProxyParams.outboundInterface when adding local
 * listening point addresses.
 *
 */
public class ProxyUtils {
	private static Log logger = LogFactory.getLog(ProxyUtils.class);
	private SipFactoryImpl sipFactoryImpl;
	private ProxyImpl proxy;
	
	public ProxyUtils(SipFactoryImpl sipFactoryImpl, ProxyImpl proxy)
	{
		this.sipFactoryImpl = sipFactoryImpl;
		this.proxy = proxy;
	}
	
	public Request createProxiedRequest(SipServletRequestImpl originalRequest, ProxyBranchImpl proxyBranch, ProxyParams params)
	{
		try {
			Request clonedRequest = (Request) originalRequest.getMessage().clone();

			// The target is null when proxying subsequent requests (the Route header is already there)
			if(params.destination != null)
			{				
				if(logger.isDebugEnabled()){
					logger.debug("request URI on the request to proxy : " + params.destination);
				}
				//this way everything is copied even the port but might not work for TelURI...
				clonedRequest.setRequestURI(((SipURIImpl)params.destination).getURI());
				
//				// Add route header
//				javax.sip.address.SipURI routeUri = SipFactories.addressFactory.createSipURI(
//						params.destination.getUser(), params.destination.getHost());
//				routeUri.setPort(params.destination.getPort());
//				routeUri.setLrParam();
//				javax.sip.address.Address address = SipFactories.addressFactory.createAddress(params.destination.getUser(),
//						routeUri);
//				RouteHeader rheader = SipFactories.headerFactory.createRouteHeader(address);
//				
//				clonedRequest.setHeader(rheader);
			}
			else
			{
				// CANCELs are hop-by-hop, so here must remove any existing Via
				// headers,
				// Record-Route headers. We insert Via header below so we will
				// get response.
				if (clonedRequest.getMethod().equals(Request.CANCEL)) {
					clonedRequest.removeHeader(ViaHeader.NAME);
					clonedRequest.removeHeader(RecordRouteHeader.NAME);
				}
			}

			// Decrease max forwards if available
			MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
				.getHeader(MaxForwardsHeader.NAME);
			if (mf == null) {
				mf = SipFactories.headerFactory.createMaxForwardsHeader(70);
				clonedRequest.addHeader(mf);
			} else {
				mf.setMaxForwards(mf.getMaxForwards() - 1);
			}

			String transport = JainSipUtils.findTransport(clonedRequest);			
			if (clonedRequest.getMethod().equals(Request.CANCEL)) {				
				// Cancel is hop by hop so remove all other via headers.
				clonedRequest.removeHeader(ViaHeader.NAME);				
			} 
			//Add via header
			ViaHeader viaHeader = JainSipUtils.createViaHeader(
					sipFactoryImpl.getSipProviders(), transport, generateBranchId());
			viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME,
					originalRequest.getSipSession().getKey().getApplicationName());
			viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_HANDLER_NAME,
					originalRequest.getSipSession().getHandler());
			clonedRequest.addHeader(viaHeader);				
			
			
			//Add route-record header, if enabled and if needed (if not null)
			if(params.routeRecord != null) {
				javax.sip.address.SipURI rrURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipProviders(), transport);
				
				Iterator<String> paramNames = params.routeRecord.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext()) {
					String paramName = paramNames.next();
					rrURI.setParameter(paramName,
							params.routeRecord.getParameter(paramName));
				}
				
				rrURI.setParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME,
						originalRequest.getSipSession().getKey().getApplicationName());
				rrURI.setParameter(SipApplicationDispatcherImpl.RR_PARAM_HANDLER_NAME,
						originalRequest.getSipSession().getHandler());
				rrURI.setLrParam();
				
				Address rraddress = SipFactories.addressFactory
				.createAddress(null, rrURI);
				RecordRouteHeader recordRouteHeader = SipFactories.headerFactory
				.createRecordRouteHeader(rraddress);
				
				ListIterator recordRouteHeaders = clonedRequest
				.getHeaders(RecordRouteHeader.NAME);
				clonedRequest.removeHeader(RecordRouteHeader.NAME);
				Vector v = new Vector();
				v.addElement(recordRouteHeader);
				// add the other record route headers.
				while (recordRouteHeaders != null
						&& recordRouteHeaders.hasNext()) {
					recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
					.next();
					v.addElement(recordRouteHeader);
				}
				for (int j = 0; j < v.size(); j++) {
					recordRouteHeader = (RecordRouteHeader) v.elementAt(j);
					clonedRequest.addHeader(recordRouteHeader);
				}
			}
			
			// Add path header
			if(params.path != null)
			{
				javax.sip.address.SipURI pathURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipProviders(), transport);

				Iterator<String> paramNames = params.path.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext()) {
					String paramName = paramNames.next();
					pathURI.setParameter(paramName,
							params.path.getParameter(paramName));
				}
				
				Address pathAddress = SipFactories.addressFactory
					.createAddress(null, pathURI);
				
				// Here I need to reference the header factory impl class because can't create path header otherwise
				PathHeader pathHeader = ((HeaderFactoryImpl)SipFactories.headerFactory)
					.createPathHeader(pathAddress);
				
				ListIterator pathHeaders = clonedRequest
					.getHeaders(PathHeader.NAME);
				clonedRequest.removeHeader(PathHeader.NAME);
				Vector v = new Vector();
				v.addElement(pathHeader);
				// add the other record route headers.
				while (pathHeaders != null
						&& pathHeaders.hasNext()) {
					pathHeader = (PathHeader) pathHeaders
					.next();
					v.addElement(pathHeader);
				}
				for (int j = 0; j < v.size(); j++) {
					pathHeader = (PathHeader) v.elementAt(j);
					clonedRequest.addHeader(pathHeader);
				}
			}
			
			return clonedRequest;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public SipServletResponseImpl createProxiedResponse(SipServletResponseImpl sipServetResponse, ProxyBranchImpl proxyBranch)
	{
		Response response = (Response)sipServetResponse.getMessage();
		Response clonedResponse = (Response)  response.clone();

		// 1. Update timer C for provisional responses
		if(sipServetResponse.getTransaction().getRequest().getMethod().equals(Request.INVITE))
			if(100<response.getStatusCode() && response.getStatusCode()<200)
			{
				proxyBranch.updateTimer();
			} else if(response.getStatusCode()>=200)
			{//remove it if response is final
				proxyBranch.cancelTimer();
			}
			
		// 2. Remove topmost via
		Iterator viaHeaderIt = clonedResponse.getHeaders(ViaHeader.NAME);
		viaHeaderIt.next();
		viaHeaderIt.remove();
		if (!viaHeaderIt.hasNext())
			return null; // response was meant for this proxy
		
		SipServletRequestImpl originalRequest =
			(SipServletRequestImpl) this.proxy.getOriginalRequest();
		
		return new SipServletResponseImpl(clonedResponse,
				sipFactoryImpl,
				originalRequest.getTransaction(),
				originalRequest.getSipSession(),
				null,
				originalRequest);


	}
	public static synchronized String generateBranchId() throws Exception {
		StringBuffer ret = new StringBuffer();
		StringBuffer b = new StringBuffer();

		b.append(new Integer((int) (Math.random() * 10000)).toString()
				+ System.currentTimeMillis());

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = messageDigest.digest(b.toString().getBytes());
			String hex = toHexString(bytes);

			ret.append("z9hG4bK" + hex);
		} catch (NoSuchAlgorithmException ex) {
			throw ex;
		}

		return ret.toString();
	}
	
	public static String toHexString(byte[] b) {
		int pos = 0;
		char[] c = new char[b.length * 2];

		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}

		return new String(c);
	}
	
	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
