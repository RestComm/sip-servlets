package org.mobicents.servlet.sip.proxy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;

public class ProxyUtils {
	
	private SipProvider provider;
	private ProxyBranchImpl proxyBranch;
	
	public ProxyUtils(SipProvider provider, ProxyBranchImpl proxyBranch)
	{
		this.provider = provider;
		this.proxyBranch = proxyBranch;
	}
	
	public SipServletRequestImpl createProxiedRequest(SipServletRequestImpl originalRequest, ProxyParams params)
	{// String user, String host, String stackIP, String transport, int port, int stackPort){
		try {
			Request clonedRequest = (Request) originalRequest.getMessage().clone();

			javax.sip.address.SipURI target = SipFactories.addressFactory.createSipURI(
					params.target.getUser(), params.target.getHost());
			clonedRequest.setRequestURI(target);

			// Decrease max forwards if available
			MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
				.getHeader(MaxForwardsHeader.NAME);
			if (mf == null) {
				mf = SipFactories.headerFactory.createMaxForwardsHeader(70);
				clonedRequest.addHeader(mf);
			} else {
				mf.setMaxForwards(mf.getMaxForwards() - 1);
			}

			// Add route header
			javax.sip.address.SipURI recordRouteUri = SipFactories.addressFactory.createSipURI(
					params.target.getUser(), params.target.getHost());
			recordRouteUri.setPort(params.target.getPort());
			recordRouteUri.setLrParam();
			javax.sip.address.Address address = SipFactories.addressFactory.createAddress(params.target.getUser(),
					recordRouteUri);
			RouteHeader rheader = SipFactories.headerFactory.createRouteHeader(address);
			
			clonedRequest.addFirst(rheader);
			
			//Add via header
			Iterator lps = provider.getSipStack().getListeningPoints();
			ListeningPoint lp = (ListeningPoint) lps.next();
			String stackIPAddress = params.stack.getHost() == null ? lp.getIPAddress() : params.stack.getHost();

			ViaHeader viaHeader = null;

			if (clonedRequest.getMethod().equals(Request.CANCEL)) {
				// Branch Id will be assigned by the stack.
				viaHeader = SipFactories.headerFactory.createViaHeader(stackIPAddress,
						lp.getPort(), lp.getTransport(), null);

				if (clonedRequest.getMethod().equals(Request.CANCEL)) {
					// Cancel is hop by hop so remove all other via headers.
					clonedRequest.removeHeader(ViaHeader.NAME);
				}
			} else {

				viaHeader = SipFactories.headerFactory.createViaHeader(stackIPAddress,
						lp.getPort(), lp.getTransport(), generateBranchId());
			}

			if (viaHeader != null) {
				clonedRequest.addHeader(viaHeader);
				
			}
			
			//Add route-record header, if enabled and if needed (if not null)
			if(params.routeRecord != null)
			{
				javax.sip.address.SipURI rrURI = SipFactories.addressFactory.createSipURI(null,
						stackIPAddress);
				rrURI.setPort(lp.getPort());

				Address rraddress = SipFactories.addressFactory
				.createAddress(null, rrURI);
				RecordRouteHeader recordRouteHeader = SipFactories.headerFactory
				.createRecordRouteHeader(rraddress);

				// lr parameter to add:
				recordRouteHeader.setParameter("lr", null);
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

			Transaction clientTransaction = provider.getNewClientTransaction(clonedRequest);
			SipServletRequestImpl ret = new	SipServletRequestImpl(provider,
					originalRequest.getSession(),
					clientTransaction, originalRequest.getDialog());

			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
