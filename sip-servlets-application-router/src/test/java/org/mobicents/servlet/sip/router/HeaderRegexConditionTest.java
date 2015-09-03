package org.mobicents.servlet.sip.router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
import javax.servlet.sip.ar.SipRouteModifier;
import junit.framework.Assert;
import junit.framework.TestCase;

public class HeaderRegexConditionTest extends TestCase {
    private static final HeaderRegexCondition condition = new HeaderRegexCondition();

    public HeaderRegexConditionTest(String testName) {
        super(testName);
    }

    class MyReq implements SipServletRequest {

        @Override
        public void addAuthHeader(SipServletResponse ssr, AuthInfo ai) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void addAuthHeader(SipServletResponse ssr, String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipServletRequest createCancel() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipServletResponse createResponse(int i) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipServletResponse createResponse(int i, String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public B2buaHelper getB2buaHelper() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Address getInitialPoppedRoute() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getMaxForwards() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Address getPoppedRoute() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Proxy getProxy() throws TooManyHopsException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Proxy getProxy(boolean bln) throws TooManyHopsException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipApplicationRoutingRegion getRegion() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public URI getSubscriberURI() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipApplicationRoutingDirective getRoutingDirective() throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public URI getRequestURI() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isInitial() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void pushPath(Address adrs) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void pushRoute(Address adrs) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void pushRoute(SipURI sipuri) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void send() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setMaxForwards(int i) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setRequestURI(URI uri) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setRoutingDirective(SipApplicationRoutingDirective sard, SipServletRequest ssr)
                throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void addAcceptLanguage(Locale locale) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void addAddressHeader(String string, Address adrs, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void addHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void addParameterableHeader(String string, Parameterable p, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Locale getAcceptLanguage() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Iterator<Locale> getAcceptLanguages() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Address getAddressHeader(String string) throws ServletParseException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public ListIterator<Address> getAddressHeaders(String string) throws ServletParseException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipApplicationSession getApplicationSession() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipApplicationSession getApplicationSession(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Object getAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getCallId() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Object getContent() throws IOException, UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Locale getContentLanguage() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getExpires() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Address getFrom() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getHeader(String string) {
            return "sip:test@sip-servlets.com";
        }

        @Override
        public HeaderForm getHeaderForm() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Iterator<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public ListIterator<String> getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getInitialRemoteAddr() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getInitialRemotePort() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getInitialTransport() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Parameterable getParameterableHeader(String string) throws ServletParseException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public ListIterator<? extends Parameterable> getParameterableHeaders(String string) throws ServletParseException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public byte[] getRawContent() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getRemoteAddr() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipSession getSession() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public SipSession getSession(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Address getTo() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getTransport() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isUserInRole(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void removeAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void removeHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setAcceptLanguage(Locale locale) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setAddressHeader(String string, Address adrs) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setAttribute(String string, Object o) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setContent(Object o, String string) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setContentLanguage(Locale locale) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setContentLength(int i) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setContentType(String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setExpires(int i) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setHeaderForm(HeaderForm hf) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public void setParameterableHeader(String string, Parameterable p) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getParameter(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String[] getParameterValues(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getRealPath(String path) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                           // | Templates.
        }

    }

    public void testConditionMet() {
        DefaultSipApplicationRouterInfo info = new DefaultSipApplicationRouterInfo("TestApp", "",
                SipApplicationRoutingRegion.TERMINATING_REGION, null, SipRouteModifier.NO_ROUTE, 0, "HEADER_TO="
                        + "sip:.*@sip-servlets\\.com");
        boolean enabled = condition.checkCondition(new MyReq(), info);
        Assert.assertTrue(enabled);
    }

    public void testConditionNotMet() {
        DefaultSipApplicationRouterInfo info = new DefaultSipApplicationRouterInfo("TestApp", "",
                SipApplicationRoutingRegion.TERMINATING_REGION, null, SipRouteModifier.NO_ROUTE, 0, "HEADER_TO="
                        + "sip:.*@sip-otherdomain\\.com");
        boolean enabled = condition.checkCondition(new MyReq(), info);
        Assert.assertFalse(enabled);
    }

}
