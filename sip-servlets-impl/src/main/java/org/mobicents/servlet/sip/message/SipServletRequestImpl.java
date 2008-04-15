package org.mobicents.servlet.sip.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.sip.ClientTransaction;
import javax.sip.SipProvider;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;

public class SipServletRequestImpl extends SipServletMessageImpl  implements SipServletRequest{
	
	
	public SipServletRequestImpl (SipProvider provider, SipSession sipSession, Request request, SipFactoryImpl sipFactory) {
		
		super.provider = provider;
		super.sipSession = sipSession;
		super.message  = request;
		super.sipFactory = sipFactory;
		
	}

	
	@Override
	public boolean isSystemHeader(String headerName) {

		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses.
		 */

		// This doesnt contain contact!!!!
		boolean isSystemHeader = systemHeaders.contains(hName);

		if (isSystemHeader)
			return isSystemHeader;

		boolean isContactSystem = false;
		Request request = (Request) this.message;

		String method = request.getMethod();
		if (method.equals(Request.REGISTER)) {
			isContactSystem = false;
		} else {
			isContactSystem = true;
		}

		if (isContactSystem && hName.equals(ContactHeader.NAME)) {
			isSystemHeader = true;
		} else {
			isSystemHeader = false;
		}

		return isSystemHeader;
	}


	public SipServletRequest createCancel() {
		
		Request request = (Request) super.message;
		if ( !request.getMethod().equals(Request.INVITE)) {
			throw new IllegalStateException ("Cannot create CANCEL for non inivte");
		}
		
		
		
	}


	public SipServletResponse createResponse(int statuscode) {
		// TODO Auto-generated method stub
		return null;
	}


	public SipServletResponse createResponse(int statusCode, String reasonPhrase) {
		// TODO Auto-generated method stub
		return null;
	}


	public B2buaHelper getB2buaHelper() {
		// TODO Auto-generated method stub
		return null;
	}


	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


	public int getMaxForwards() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Address getPoppedRoute() {
		// TODO Auto-generated method stub
		return null;
	}


	public Proxy getProxy() throws TooManyHopsException {
		// TODO Auto-generated method stub
		return null;
	}


	public Proxy getProxy(boolean create) throws TooManyHopsException {
		// TODO Auto-generated method stub
		return null;
	}


	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


	public URI getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean isInitial() {
		// TODO Auto-generated method stub
		return false;
	}


	public void pushPath(Address uri) {
		// TODO Auto-generated method stub
		
	}


	public void pushRoute(Address uri) {
		// TODO Auto-generated method stub
		
	}


	public void pushRoute(SipURI uri) {
		// TODO Auto-generated method stub
		
	}


	public void setMaxForwards(int n) {
		// TODO Auto-generated method stub
		
	}


	public void setRequestURI(URI uri) {
		// TODO Auto-generated method stub
		
	}


	public void setRoutingDirective(SipApplicationRoutingDirective directive,
			SipServletRequest origRequest) throws IllegalStateException {
		// TODO Auto-generated method stub
		
	}


	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}


	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}


	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}


	public Enumeration getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}


	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}


	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}


	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}


	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	

}
