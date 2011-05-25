package org.mobicents.servlet.sip.proxy;

import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.URI;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

/**
 * PlaceHolder to store the information required to implement 3GPP TS 24.229 section 5.2.8.1.2. ie Termination of Session originating from the proxy.
 * @author Andrew Miller (Crocodile RCS)
 */
public class ProxyTerminationInfo implements Externalizable {
	
	private LinkedList<javax.sip.address.Address> calleeRouteSet; // Route set to reach callee from us.  
	private LinkedList<javax.sip.address.Address> callerRouteSet; // Route set to reach caller from us.

	private URI calleeContact; // Contact given by the callee in the final response to the initial INVITE  
	private URI callerContact; // Contact given by the caller in the initial INVITE

	private To toHeader;		   // To: header in the direction caller->callee
	private From fromHeader;       // From: header in the direction caller->callee
	private static final Logger logger = Logger.getLogger(ProxyTerminationInfo.class);
	private String callId;
	
	private ProxyImpl proxyImpl;
	
	// empty constructor used only for Externalizable interface
	public ProxyTerminationInfo() {
		callerRouteSet = new LinkedList<javax.sip.address.Address>();
		calleeRouteSet = new LinkedList<javax.sip.address.Address>();
	}
	
	public ProxyTerminationInfo(SipServletResponseImpl proxiedResponse, SipURI recordRouteURI, ProxyImpl proxyImpl) {
		if (proxyImpl == null)
			throw new NullPointerException("Null proxyImpl");
		if (proxiedResponse == null)
			throw new NullPointerException("Null message");
		this.proxyImpl = proxyImpl;
		SipServletRequest request = proxiedResponse.getRequest();
		SIPResponse nistResponse = (SIPResponse) ((SipServletResponseImpl) proxiedResponse).getResponse();
		SIPRequest nistRequest =  (SIPRequest) ((SipServletRequestImpl) request).getMessage(); 
		
		toHeader = (To) nistResponse.getTo();
		fromHeader = (From) nistResponse.getFrom();
		calleeContact = nistResponse.getContactHeader().getAddress().getURI();
		callerContact = nistRequest.getContactHeader().getAddress().getURI();
		
		calleeRouteSet = new LinkedList<javax.sip.address.Address>();
		callerRouteSet = new LinkedList<javax.sip.address.Address>();
		
		callId = proxiedResponse.getCallId();

		ListIterator<Address> i;
		try {
			i = proxiedResponse.getRequest().getAddressHeaders(RecordRouteHeader.NAME);
		} catch (ServletParseException e) {
			throw new IllegalArgumentException("Couldn't get the RecordRoute Headers from the request " + proxiedResponse.getRequest(), e);
		}
		while (i.hasNext()) {
			AddressImpl a = (AddressImpl) i.next();
			callerRouteSet.add(a.getAddress());
		}		

		try {		
			i = proxiedResponse.getAddressHeaders(RecordRouteHeader.NAME);
		} catch (ServletParseException e) {
			throw new IllegalArgumentException("Couldn't get the RecordRoute Headers from the response " + proxiedResponse, e);
		}
		while (i.hasNext()) { // loop through the record-routes until you reach us
			Address a = i.next();
			if (a.getURI().equals(recordRouteURI)) {
				i.previous();
				break;
			} 
		}
		while (i.hasPrevious()) { // Add the routes in reverse
			AddressImpl a = (AddressImpl) i.previous();
			calleeRouteSet.add(a.getAddress());
		}		
	}
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		try {
			calleeContact = SipFactories.addressFactory.createURI(in.readUTF());
			callerContact = SipFactories.addressFactory.createURI(in.readUTF());

			int routeSetSize = in.readInt();
			for (int i = 0; i < routeSetSize; i++) {
				javax.sip.address.Address route = SipFactories.addressFactory.createAddress(in.readUTF());
				callerRouteSet.add(route);
			} 

			routeSetSize = in.readInt();
			for (int i = 0; i < routeSetSize; i++) {
				javax.sip.address.Address route = SipFactories.addressFactory.createAddress(in.readUTF());
				calleeRouteSet.add(route);
			} 

			javax.sip.address.Address toAddress = SipFactories.addressFactory.createAddress(in.readUTF());
			toHeader = (To) SipFactories.headerFactory.createToHeader(toAddress, in.readUTF());

			javax.sip.address.Address fromAddress = SipFactories.addressFactory.createAddress(in.readUTF());
			fromHeader = (From) SipFactories.headerFactory.createFromHeader(fromAddress, in.readUTF());
			callId = in.readUTF();
		} catch (ParseException e) {
			throw new IllegalArgumentException("Problem occurred while unserializing ProxyTerminationInfo", e);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {

		out.writeUTF(calleeContact.toString());
		out.writeUTF(callerContact.toString());

		out.writeInt(calleeRouteSet.size());
		Iterator<javax.sip.address.Address> i = calleeRouteSet.iterator();
    	while (i.hasNext()) {
    		javax.sip.address.Address a = i.next();
    		out.writeUTF(a.toString());
    	}

    	out.writeInt(callerRouteSet.size());
		i = callerRouteSet.iterator();
    	while (i.hasNext()) {
    		javax.sip.address.Address a = i.next();
    		out.writeUTF(a.toString());
    	}

    	out.writeUTF(toHeader.getAddress().toString());
    	out.writeUTF(toHeader.getTag());
    	out.writeUTF(fromHeader.getAddress().toString());
		out.writeUTF(fromHeader.getTag());
		out.writeUTF(callId);
	}

	public void setProxy(ProxyImpl proxyImpl) {
		this.proxyImpl = proxyImpl;
	}
	
	private void sendBye (final SipSession session, URI requestUri, To to, From from,
			  long cSeq, LinkedList<javax.sip.address.Address> routeSet,
			  int responseCode, String responseText) throws IOException {

		Request request;

		try {
			request = SipFactories.messageFactory.createRequest(requestUri, Request.BYE,
															 	SipFactories.headerFactory.createCallIdHeader(callId),
																SipFactories.headerFactory.createCSeqHeader(cSeq, Request.BYE),
																from, to, new ArrayList(), 
																SipFactories.headerFactory.createMaxForwardsHeader(70));
		} catch (ParseException e1) {
			throw new IllegalArgumentException("Problem occurred while creating the Proxy BYE on session " + session + 
					" requestURI = " + requestUri + " From = " + from + " To = " + to + " cSeq " + cSeq + 
					" routeSet = " + routeSet, e1);
		} catch (InvalidArgumentException e1) {
			throw new IllegalArgumentException("Problem occurred while creating the Proxy BYE on session " + session + 
					" requestURI = " + requestUri + " From = " + from + " To = " + to + " cSeq " + cSeq + 
					" routeSet = " + routeSet, e1);
		}

		if ((responseText != null) && (responseCode > 299) && (responseCode < 700)) {
			ReasonHeader reason;
			try {
				reason = SipFactories.headerFactory.createReasonHeader("sip", responseCode, responseText);
				((SIPMessage)request).addHeader(reason);
			} catch (InvalidArgumentException e) {
				throw new IllegalArgumentException("Failed to build reason header " + responseCode + ":" + responseText);				
			} catch (ParseException e) {
				throw new IllegalArgumentException("Failed to build reason header " + responseCode + ":" + responseText);
			}

		}

		Iterator<javax.sip.address.Address> i = routeSet.iterator();
		while (i.hasNext()) {
			javax.sip.address.Address javaxAddress = i.next();
			RouteHeader routeHeader = new Route((gov.nist.javax.sip.address.AddressImpl) javaxAddress);
			request.addHeader(routeHeader);
		}

		SipServletRequestImpl sipServletRequest = new SipServletRequestImpl(request, proxyImpl.getSipFactoryImpl(),
													(MobicentsSipSession) session, null, null, false);
		sipServletRequest.send();		
	}
	
	public void terminate(final SipSession session, long callerCSeq, long calleeCSeq,
						  final int calleeResponseCode, final String calleeResponseText,
						  final int callerResponseCode, final String callerResponseText) throws IOException {
		sendBye (session, calleeContact, toHeader, fromHeader, callerCSeq + 1, calleeRouteSet, calleeResponseCode, calleeResponseText);
    	sendBye (session, callerContact, new To(fromHeader), new From(toHeader), calleeCSeq + 1, callerRouteSet, callerResponseCode, callerResponseText);
	}
}
