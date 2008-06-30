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
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;

/**
 * <p>
 * This class offers a read only view of an underlying sip servlet request.
 * This is used for passing the request to the Sip Application Router and when
 * the @SipApplicationKey annotated method is called
 * </p>
 * TODO : protect also in a read only way underlying objects returned such as URI, Address, ...
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipServletRequestReadOnly implements SipServletRequest {

	private static final String EXCEPTION_MESSAGE = "The context does not allow you to modify this request !";

	private SipServletRequest writeableRequest;
	
	/**
	 * @param writeableRequest
	 */
	public SipServletRequestReadOnly(SipServletRequest writeableRequest) {
		super();
		this.writeableRequest = writeableRequest;
	}

	public void addAuthHeader(SipServletResponse arg0, AuthInfo arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void addAuthHeader(SipServletResponse arg0, String arg1, String arg2) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public SipServletRequest createCancel() {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public SipServletResponse createResponse(int arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public SipServletResponse createResponse(int arg0, String arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public B2buaHelper getB2buaHelper() {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public Address getInitialPoppedRoute() {
		return writeableRequest.getInitialPoppedRoute();
	}

	public ServletInputStream getInputStream() throws IOException {
		return writeableRequest.getInputStream();
	}

	public int getMaxForwards() {
		return writeableRequest.getMaxForwards();
	}

	public Address getPoppedRoute() {
		return writeableRequest.getPoppedRoute();
	}

	public Proxy getProxy() throws TooManyHopsException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public Proxy getProxy(boolean arg0) throws TooManyHopsException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public BufferedReader getReader() throws IOException {
		return writeableRequest.getReader();
	}

	public SipApplicationRoutingRegion getRegion() {
		return writeableRequest.getRegion();
	}

	public URI getRequestURI() {
		return writeableRequest.getRequestURI();
	}

	public SipApplicationRoutingDirective getRoutingDirective()
			throws IllegalStateException {
		return writeableRequest.getRoutingDirective();
	}

	public URI getSubscriberURI() {
		return writeableRequest.getSubscriberURI();
	}

	public boolean isInitial() {
		return writeableRequest.isInitial();
	}

	public void pushPath(Address arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void pushRoute(Address arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void pushRoute(SipURI arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void send() throws IOException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setMaxForwards(int arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setRequestURI(URI arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setRoutingDirective(SipApplicationRoutingDirective arg0,
			SipServletRequest arg1) throws IllegalStateException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void addAcceptLanguage(Locale arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void addAddressHeader(String arg0, Address arg1, boolean arg2) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void addHeader(String arg0, String arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void addParameterableHeader(String arg0, Parameterable arg1,
			boolean arg2) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public Locale getAcceptLanguage() {
		return writeableRequest.getAcceptLanguage();
	}

	public Iterator<Locale> getAcceptLanguages() {		
		return writeableRequest.getAcceptLanguages();
	}

	public Address getAddressHeader(String arg0) throws ServletParseException {
		return writeableRequest.getAddressHeader(arg0);
	}

	public ListIterator<Address> getAddressHeaders(String arg0)
			throws ServletParseException {
		return writeableRequest.getAddressHeaders(arg0);
	}

	public SipApplicationSession getApplicationSession() {
		return writeableRequest.getApplicationSession(false);
	}

	public SipApplicationSession getApplicationSession(boolean arg0) {
		return writeableRequest.getApplicationSession(false);
	}

	public Object getAttribute(String arg0) {
		return writeableRequest.getAttribute(arg0);
	}

	public Enumeration<String> getAttributeNames() {
		return writeableRequest.getAttributeNames();
	}
	
	public String getCallId() {
		return writeableRequest.getCallId();
	}

	public String getCharacterEncoding() {
		return writeableRequest.getCharacterEncoding();
	}

	public Object getContent() throws IOException, UnsupportedEncodingException {
		return writeableRequest.getContent();
	}

	public Locale getContentLanguage() {
		return writeableRequest.getContentLanguage();
	}

	public int getContentLength() {
		return writeableRequest.getContentLength();
	}

	public String getContentType() {
		return writeableRequest.getContentType();
	}

	public int getExpires() {
		return writeableRequest.getExpires();
	}

	public Address getFrom() {
		return writeableRequest.getFrom();
	}

	public String getHeader(String arg0) {
		return writeableRequest.getHeader(arg0);
	}

	public HeaderForm getHeaderForm() {
		return writeableRequest.getHeaderForm();
	}

	public Iterator<String> getHeaderNames() {
		return writeableRequest.getHeaderNames();
	}

	public ListIterator<String> getHeaders(String arg0) {
		return writeableRequest.getHeaders(arg0);
	}

	public String getInitialRemoteAddr() {
		return writeableRequest.getInitialRemoteAddr();
	}

	public int getInitialRemotePort() {
		return writeableRequest.getInitialRemotePort();
	}

	public String getInitialTransport() {
		return writeableRequest.getInitialTransport();
	}

	public String getLocalAddr() {
		return writeableRequest.getLocalAddr();
	}

	public int getLocalPort() {
		return writeableRequest.getLocalPort();
	}

	public String getMethod() {
		return writeableRequest.getMethod();
	}

	public Parameterable getParameterableHeader(String arg0)
			throws ServletParseException {
		return writeableRequest.getParameterableHeader(arg0);
	}

	public ListIterator<? extends Parameterable> getParameterableHeaders(
			String arg0) throws ServletParseException {
		return writeableRequest.getParameterableHeaders(arg0);
	}

	public String getProtocol() {
		return writeableRequest.getProtocol();
	}

	public byte[] getRawContent() throws IOException {
		return writeableRequest.getRawContent();
	}

	public String getRemoteAddr() {
		return writeableRequest.getRemoteAddr();
	}

	public int getRemotePort() {
		return writeableRequest.getRemotePort();
	}

	public String getRemoteUser() {
		return writeableRequest.getRemoteUser();
	}

	public SipSession getSession() {
		return writeableRequest.getSession(false);
	}

	public SipSession getSession(boolean arg0) {
		return writeableRequest.getSession(false);
	}

	public Address getTo() {
		return writeableRequest.getTo();
	}

	public String getTransport() {
		return writeableRequest.getTransport();
	}

	public Principal getUserPrincipal() {
		return writeableRequest.getUserPrincipal();
	}

	public boolean isCommitted() {
		return writeableRequest.isCommitted();
	}

	public boolean isSecure() {
		return writeableRequest.isSecure();
	}

	public boolean isUserInRole(String arg0) {
		return writeableRequest.isUserInRole(arg0);
	}

	public void removeAttribute(String arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void removeHeader(String arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setAcceptLanguage(Locale arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setAddressHeader(String arg0, Address arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setAttribute(String arg0, Object arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setContent(Object arg0, String arg1)
			throws UnsupportedEncodingException {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setContentLanguage(Locale arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setContentLength(int arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setContentType(String arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setExpires(int arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setHeader(String arg0, String arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setHeaderForm(HeaderForm arg0) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public void setParameterableHeader(String arg0, Parameterable arg1) {
		throw new IllegalStateException(EXCEPTION_MESSAGE);
	}

	public String getLocalName() {
		return writeableRequest.getLocalName();
	}

	public Locale getLocale() {
		return writeableRequest.getLocale();
	}

	public Enumeration getLocales() {
		return writeableRequest.getLocales();
	}

	public String getParameter(String arg0) {
		return writeableRequest.getParameter(arg0);
	}

	public Map getParameterMap() {
		return writeableRequest.getParameterMap();
	}

	public Enumeration getParameterNames() {
		return writeableRequest.getParameterNames();
	}

	public String[] getParameterValues(String arg0) {
		return writeableRequest.getParameterValues(arg0);
	}

	public String getRealPath(String arg0) {
		return writeableRequest.getRealPath(arg0);
	}

	public String getRemoteHost() {
		return writeableRequest.getRemoteHost();
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return writeableRequest.getRequestDispatcher(arg0);
	}

	public String getScheme() {
		return writeableRequest.getScheme();
	}

	public String getServerName() {
		return writeableRequest.getServerName();
	}

	public int getServerPort() {
		return writeableRequest.getServerPort();
	}

}
