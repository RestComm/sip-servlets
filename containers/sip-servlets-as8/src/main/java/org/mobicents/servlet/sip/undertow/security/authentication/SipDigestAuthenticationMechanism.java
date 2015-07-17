/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
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
package org.mobicents.servlet.sip.undertow.security.authentication;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.text.ParseException;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.sip.SipStack;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;

import org.apache.log4j.Logger;
import org.jboss.security.PicketBoxLogger;
import org.jboss.security.PicketBoxMessages;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.security.auth.callback.DigestCallbackHandler;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipDigestAuthenticator;
import org.mobicents.servlet.sip.core.security.SipPrincipal;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.security.SIPSecurityConstants;
import org.mobicents.servlet.sip.security.SecurityActions;
import org.mobicents.servlet.sip.undertow.SipLoginConfig;
import org.mobicents.servlet.sip.undertow.security.UndertowSipPrincipal;

import gov.nist.javax.sip.SipStackImpl;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import io.undertow.security.impl.DigestQop;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.ServletInfo;

/**
 *
 * This class is based org.mobicents.servlet.sip.catalina.security.authentication.DigestAuthenticator class from sip-servlet-as7 project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
*/
public class SipDigestAuthenticationMechanism implements SipDigestAuthenticator {
    // ----------------------------------------------------- Instance Variables
    private static final Logger log = Logger.getLogger(SipDigestAuthenticationMechanism.class);
    /**
     * The MD5 helper object for this class.
     */
    static final MD5Encoder MD5_ENCODER = new MD5Encoder();
    private static final String DIGEST_AUTH_USERS_PROPERTIES = "org.mobicents.servlet.sip.DIGEST_AUTH_USERS_PROPERTIES";
    private static final String DIGEST_AUTH_PASSWORD_IS_A1HASH = "org.mobicents.servlet.sip.DIGEST_AUTH_PASSWORD_IS_A1HASH";

    /**
     * MD5 message digest provider.
     */
    protected volatile static MessageDigest md5Helper;

    static {
        try {
             md5Helper = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * Private key.
     */
    protected String key = "Catalina";
    /*
     * Principal
     */
    private SipPrincipal principal;
    // ------------------------------------------------------------- Properties
    /**
     * The default message digest algorithm to use if we cannot use
     * the requested one.
     */
    protected static final String DEFAULT_ALGORITHM = "MD5";
    /**
     * The message digest algorithm to be used when generating session
     * identifiers.  This must be an algorithm supported by the
     * <code>java.security.MessageDigest</code> class on your platform.
     */
    protected String algorithm = DEFAULT_ALGORITHM;
    /**
     * Should we cache authenticated Principals if the request is part of
     * an HTTP session?
     */
    protected boolean cache = true;
    /**
     * The Context to which this Valve is attached.
     */
    protected SipContext context = null;
    /**
     * Return the MessageDigest implementation to be used when
     * creating session identifiers.
     */
    protected MessageDigest digest = null;
    /**
     * A String initialization parameter used to increase the entropy of
     * the initialization of our random number generator.
     */
    protected String entropy = null;
    /**
     * Descriptive information about this implementation.
     */
    protected static final String INFO = "org.mobicents.servlet.sip.undertow.security.authentication.SipDigestAuthenticationMechanism/1.0";
    /**
     * A random number generator to use when generating session identifiers.
     */
    protected Random random = null;
    /**
     * The Java class name of the random number generator class to be used
     * when generating session identifiers.
     */
    protected String randomClass = "java.security.SecureRandom";
    /**
     * The string manager for this package (not supported in AS-7.2.0)
     */
    // protected static final StringManager STRING_MANAGER =
    // StringManager.getManager(Constants.Package);

    private HeaderFactory headerFactory;
    private String realmName;

    public SipDigestAuthenticationMechanism(final String realmName, HeaderFactory headerFactory) {
        this.headerFactory = headerFactory;
        this.realmName = realmName;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config Login configuration describing how authentication
     *              should be performed
     * @param securityDomain
     * @param deployment
     * @param servletInfo
     * @param sipStack
     *
     * @exception IOException if an input/output error occurs
    */
    public boolean authenticate(MobicentsSipServletRequest request, MobicentsSipServletResponse response,
            MobicentsSipLoginConfig config, String securityDomain, Deployment deployment, ServletInfo servletInfo, SipStack sipStack) throws IOException {

        principal = null;

        // Have we already authenticated someone?
        principal = request.getUserPrincipal();
        // String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            if (log.isDebugEnabled())
                log.debug("Already authenticated '" + principal.getName() + "'");

            return (true);
        }

        // Validate any credentials already included with this request
        String authorization = request.getHeader("authorization");
        if (authorization != null) {

            principal = findPrincipal(request, authorization, this.realmName, securityDomain, deployment, servletInfo, sipStack);
            if (principal != null &&
            // fix for http://code.google.com/p/sipservlets/issues/detail?id=88
                    principal.getPrincipal() != null) {
                String username = parseUsername(authorization);
                register(request, response, principal, "DIGEST",// Constants.DIGEST_METHOD,
                        username, null);
                return (true);
            }
        }

        // Send an "unauthorized" response and an appropriate challenge

        // Next, generate a nOnce token (that is a token which is supposed
        // to be unique).
        String nOnce = generateNOnce(request);

        setAuthenticateHeader(request, response, config, nOnce);
        response.send();
        // hres.flushBuffer();
        return (false);
    }

    /**
     * Parse the specified authorization credentials, and return the
     * associated Principal that these credentials authenticate (if any)
     * from the specified Realm.  If there is no such Principal, return
     * <code>null</code>.
     *
     * @param request HTTP servlet request
     * @param authorization Authorization credentials from this request
     * @param realm Realm used to authenticate Principals
     * @param sercurityDomain
     * @param deployment
     * @param servletInfo
     * @param sipStack
    */
    protected static SipPrincipal findPrincipal(MobicentsSipServletRequest request,
                                             String authorization,
                                             String realmName,
                                             String securityDomain,
                                             Deployment deployment,
                                             ServletInfo servletInfo,
                                             SipStack sipStack) {

        //System.out.println("Authorization token : " + authorization);
        // Validate the authorization credentials format
        if (authorization == null){
            return (null);
        }
        if (!authorization.startsWith("Digest ")){
            return (null);
        }
        String tmpAuthorization = authorization.substring(7).trim();

        // Bugzilla 37132: http://issues.apache.org/bugzilla/show_bug.cgi?id=37132
        // The solution of 37132 doesn't work with : response="2d05f1206becab904c1f311f205b405b",cnonce="5644k1k670",username="admin",nc=00000001,qop=auth,nonce="b6c73ab509830b8c0897984f6b0526e8",realm="sip-servlets-realm",opaque="9ed6d115d11f505f9ee20f6a68654cc2",uri="sip:192.168.1.142",algorithm=MD5
        // That's why I am going back to simple comma (Vladimir). TODO: Review this.
        String[] tokens = tmpAuthorization.split(",");//(?=(?:[^\"]*\"[^\"]*\")+$)");

        String userName = null;
        String nOnce = null;
        String nc = null;
        String cnonce = null;
        String qop = null;
        String uri = null;
        String response = null;
        String method = request.getMethod();

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            if (currentToken.length() == 0){
                continue;
            }
            int equalSign = currentToken.indexOf('=');
            if (equalSign < 0){
                return null;
            }
            String currentTokenName =
                currentToken.substring(0, equalSign).trim();
            String currentTokenValue =
                currentToken.substring(equalSign + 1).trim();
            if ("username".equals(currentTokenName)){
                userName = removeQuotes(currentTokenValue);
            }
            if ("realm".equals(currentTokenName)){
                realmName = removeQuotes(currentTokenValue, true);
            }
            if ("nonce".equals(currentTokenName)){
                nOnce = removeQuotes(currentTokenValue);
            }
            if ("nc".equals(currentTokenName)){
                nc = removeQuotes(currentTokenValue);
            }
            if ("cnonce".equals(currentTokenName)){
                cnonce = removeQuotes(currentTokenValue);
            }
            if ("qop".equals(currentTokenName)){
                qop = removeQuotes(currentTokenValue);
                DigestQop qopObj = DigestQop.forName(qop);
            }
            if ("uri".equals(currentTokenName)){
                uri = removeQuotes(currentTokenValue);
            }
            if ("response".equals(currentTokenName)){
                response = removeQuotes(currentTokenValue);
            }
        }

        if ( (userName == null) || (realmName == null) || (nOnce == null)
             || (uri == null) || (response == null) ){
            return null;
        }

        // Second MD5 digest used to calculate the digest :
        // MD5(Method + ":" + uri)
        String a2 = method + ":" + uri;
        //System.out.println("A2:" + a2);

        byte[] buffer = null;
        synchronized (md5Helper){
            buffer = md5Helper.digest(a2.getBytes());
        }
        String md5a2 = MD5_ENCODER.encode(buffer);

        //taken from https://github.com/jbossas/jboss-as/blob/7.1.2.Final/web/src/main/java/org/jboss/as/web/security/SecurityContextAssociationValve.java#L86
        SecurityContext sc = SecurityActions.getSecurityContext();

        if (sc == null){
            if (log.isDebugEnabled()){
                log.debug("Security Domain " + securityDomain + " for Realm " + realmName);
            }
            if (securityDomain == null){
                if (log.isDebugEnabled()){
                    log.debug("Security Domain is null using default security domain " + SIPSecurityConstants.DEFAULT_SIP_APPLICATION_POLICY + " for Realm " + realmName);
                }
                securityDomain = SIPSecurityConstants.DEFAULT_SIP_APPLICATION_POLICY;
            }
            sc = SecurityActions.createSecurityContext(securityDomain);

            SecurityActions.setSecurityContextOnAssociation(sc);
        }

        try {
            Properties users=new Properties();
            String dir = System.getProperty("jboss.server.config.dir");
            String fileName = "sip-servlets-users.properties";
            boolean storedPasswordIsA1Hash = true;

            if(sipStack instanceof SipStackImpl){
                Properties stackProperties = ((SipStackImpl)sipStack).getConfigurationProperties();
                fileName = stackProperties.getProperty(SipDigestAuthenticationMechanism.DIGEST_AUTH_USERS_PROPERTIES, "sip-servlets-users.properties");
                storedPasswordIsA1Hash = Boolean.parseBoolean(stackProperties.getProperty(SipDigestAuthenticationMechanism.DIGEST_AUTH_PASSWORD_IS_A1HASH, "true"));
            }

            try {
                users = SipDigestAuthenticationMechanism.loadProperties(dir+"/"+fileName, dir+"/"+fileName);
            } catch (IOException e) {
                log.warn("Failed to load user properties file from location " + dir+"/"+fileName + ", please check digest security config in standalone and mss sip stack property files!");
            }

            String storedPassword = users.getProperty(userName, "");
            Account account = SipDigestAuthenticationMechanism.authenticate(userName, storedPassword, response, nOnce, nc, cnonce, method, uri, qop, realmName, md5a2, deployment, storedPasswordIsA1Hash);

            if(account==null){
                return null;
            }else{
                return (new UndertowSipPrincipal(account,deployment,servletInfo));
            }
        } finally {
            SecurityActions.clearSecurityContext();
            SecurityRolesAssociation.setSecurityRoles(null);
        }
    }

    /*
     * Return the Account associated with the specified username, which matches the digest calculated using the given parameters using the method described in rfc2617; otherwise return null.
     * @param userName Username of the Principal to look up
     * @param storedPassword
     * @param clientDigest Digest which has been submitted by the client
     * @param nOnce Unique (or supposedly unique) token which has been used for this request
     * @param nc
     * @param cnonce
     * @param method
     * @param uri
     * @param qop
     * @param realmName Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest : MD5(Method + ":" + uri)
     * @param deployment
     * @param storedPasswordIsA1Hash
    */
    private static Account authenticate(String userName,
            String storedPassword,
            String clientDigest,
            String nOnce,
            String nc,
            String cnonce,
            String method,
            String uri,
            String qop,
            String realmName,
            String md5a2,
            Deployment deployment,
            boolean storedPasswordIsA1Hash) {

        CallbackHandlerPolicyContextHandler.setCallbackHandler(new DigestCallbackHandler(userName, nOnce, nc, cnonce, qop, realmName, md5a2));

        String serverDigest = "";
        if(storedPasswordIsA1Hash){
            //storedPassword is HA1 in this case
            serverDigest = MessageDigestResponseAlgorithm.calculateResponse(md5Helper.getAlgorithm(), storedPassword, nOnce, nc, cnonce, method, uri, "", qop) ;
        }else{
            serverDigest = MessageDigestResponseAlgorithm.calculateResponse(md5Helper.getAlgorithm(), userName, realmName, storedPassword, nOnce, nc, cnonce, method, uri, "", qop) ;
        }

        if (serverDigest.equals(clientDigest)) {
            //lest's reauth with stored password (to force successful authentication) to make wildfly to create Account and Principal for us
            //this is because wildfly bug: https://issues.jboss.org/browse/WFLY-3659
            final IdentityManager identityManager = deployment.getDeploymentInfo().getIdentityManager();
            PasswordCredential credential = new PasswordCredential(storedPassword.toCharArray());
            Account account = identityManager.verify(userName, credential);

            return account;
       }
       return null;
    }

    //helper method to load digest user property file. Copied from org.jboss.security.auth.spi.Util
    private static Properties loadProperties(String defaultsName, String propertiesName)
        throws IOException{
        Properties bundle = new Properties();
        ClassLoader loader = SecurityActions.getContextClassLoader();
        URL defaultUrl = null;
        URL url = null;
        // First check for local visibility via a URLClassLoader.findResource
        if( loader instanceof URLClassLoader ){
            URLClassLoader ucl = (URLClassLoader) loader;
            defaultUrl = SecurityActions.findResource(ucl,defaultsName);
            url = SecurityActions.findResource(ucl,propertiesName);
            PicketBoxLogger.LOGGER.traceAttemptToLoadResource(propertiesName);
        }
        // Do a general resource search
        if( defaultUrl == null ) {
            defaultUrl = loader.getResource(defaultsName);
            if (defaultUrl == null) {
                try {
                    defaultUrl = new URL(defaultsName);
                } catch (MalformedURLException mue) {
                    PicketBoxLogger.LOGGER.debugFailureToOpenPropertiesFromURL(mue);
                    File tmp = new File(defaultsName);
                    if (tmp.exists()){
                        defaultUrl = tmp.toURI().toURL();
                    }
                }
            }
        }
        if( url == null ) {
            url = loader.getResource(propertiesName);
            if (url == null) {
                try {
                    url = new URL(propertiesName);
                } catch (MalformedURLException mue) {
                    PicketBoxLogger.LOGGER.debugFailureToOpenPropertiesFromURL(mue);
                    File tmp = new File(propertiesName);
                    if (tmp.exists()){
                        url = tmp.toURI().toURL();
                    }
                }
            }
        }
        if( url == null && defaultUrl == null ){

            String propertiesFiles = propertiesName + "/" + defaultsName;
            throw PicketBoxMessages.MESSAGES.unableToFindPropertiesFile(propertiesFiles);
        }

        if (url != null) {
            InputStream is = null;
            try {
                is = SecurityActions.openStream(url);
            } catch (PrivilegedActionException e) {
                throw new IOException(e.getLocalizedMessage());
            }
            if (is != null) {
                try {
                    bundle.load(is);
                } finally {
                    safeClose(is);
                }
            } else {
                throw PicketBoxMessages.MESSAGES.unableToLoadPropertiesFile(propertiesName);
            }
            PicketBoxLogger.LOGGER.tracePropertiesFileLoaded(propertiesName, bundle.keySet());
        } else {
            InputStream is = null;
            try {
                is = defaultUrl.openStream();
                bundle.load(is);
                PicketBoxLogger.LOGGER.tracePropertiesFileLoaded(defaultsName, bundle.keySet());
            } catch (Throwable e) {
                PicketBoxLogger.LOGGER.debugFailureToLoadPropertiesFile(defaultsName, e);
            } finally {
                safeClose(is);
            }
        }
        return bundle;
    }

    private static void safeClose(InputStream fis){
        try{
            if(fis != null){
                fis.close();
            }
        }
        catch(Exception e)
        {}
    }

    /**
     * Parse the username from the specified authorization string.  If none
     * can be identified, return <code>null</code>
     *
     * @param authorization Authorization string to be parsed
     */
    protected String parseUsername(String authorization) {
        //System.out.println("Authorization token : " + authorization);
        // Validate the authorization credentials format
        if (authorization == null)
            return (null);
        if (!authorization.startsWith("Digest "))
            return (null);
        String tmpAuthorization = authorization.substring(7).trim();

        StringTokenizer commaTokenizer =
            new StringTokenizer(tmpAuthorization, ",");

        while (commaTokenizer.hasMoreTokens()) {
            String currentToken = commaTokenizer.nextToken();
            int equalSign = currentToken.indexOf('=');
            if (equalSign < 0)
                return null;
            String currentTokenName =
                currentToken.substring(0, equalSign).trim();
            String currentTokenValue =
                currentToken.substring(equalSign + 1).trim();
            if ("username".equals(currentTokenName))
                return (removeQuotes(currentTokenValue));
        }
        return (null);
    }

    /**
     * Removes the quotes on a string. RFC2617 states quotes are optional for
     * all parameters except realm.
     */
    protected static String removeQuotes(String quotedString,
                                         boolean quotesRequired) {
        //support both quoted and non-quoted
        if (quotedString.length() > 0 && quotedString.charAt(0) != '"' &&
                !quotesRequired) {
            return quotedString;
        } else if (quotedString.length() > 2) {
            return quotedString.substring(1, quotedString.length() - 1);
        } else {
            return "";
        }
    }

    /**
     * Removes the quotes on a string.
     */
    protected static String removeQuotes(String quotedString) {
        return removeQuotes(quotedString, false);
    }

    /**
     * Generate a unique token. The token is generated according to the
     * following pattern. NOnceToken = Base64 ( MD5 ( client-IP ":"
     * time-stamp ":" private-key ) ).
     *
     * @param request HTTP Servlet request
     */
    protected String generateNOnce(MobicentsSipServletRequest request) {
        long currentTime = System.currentTimeMillis();

        String nOnceValue = request.getRemoteAddr() + ":" +
            currentTime + ":" + key;

        byte[] buffer = null;
        synchronized (md5Helper) {
            buffer = md5Helper.digest(nOnceValue.getBytes());
        }
        nOnceValue = MD5_ENCODER.encode(buffer);

        return nOnceValue;
    }

    /**
     * Generates the WWW-Authenticate header.
     * <p>
     * The header MUST follow this template :
     * <pre>
     *      WWW-Authenticate    = "WWW-Authenticate" ":" "Digest"
     *                            digest-challenge
     *
     *      digest-challenge    = 1#( realm | [ domain ] | nOnce |
     *                  [ digest-opaque ] |[ stale ] | [ algorithm ] )
     *
     *      realm               = "realm" "=" realm-value
     *      realm-value         = quoted-string
     *      domain              = "domain" "=" <"> 1#URI <">
     *      nonce               = "nonce" "=" nonce-value
     *      nonce-value         = quoted-string
     *      opaque              = "opaque" "=" quoted-string
     *      stale               = "stale" "=" ( "true" | "false" )
     *      algorithm           = "algorithm" "=" ( "MD5" | token )
     * </pre>
     *
     * @param request HTTP Servlet request
     * @param response HTTP Servlet response
     * @param config    Login configuration describing how authentication
     *              should be performed
     * @param nOnce nonce token
    */
    protected void setAuthenticateHeader(MobicentsSipServletRequest request,
                                         MobicentsSipServletResponse response,
                                         MobicentsSipLoginConfig config,
                                         String nOnce) {
        // Get the realm name
        String realmName = ((SipLoginConfig)config).getRealmName();
        if (realmName == null)
            realmName = request.getServerName() + ":"
                + request.getServerPort();

        byte[] buffer = null;
        synchronized (md5Helper) {
            buffer = md5Helper.digest(nOnce.getBytes());
        }

        String authenticateHeader = "Digest realm=\"" + realmName + "\", "
            +  "qop=\"auth\", nonce=\"" + nOnce + "\", " + "opaque=\""
            + MD5_ENCODER.encode(buffer) + "\"";

        // There are different headers for different types of auth
        if(response.getStatus() ==
            MobicentsSipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
            response.setHeader("Proxy-Authenticate", authenticateHeader);
        } else {
            response.setHeader("WWW-Authenticate", authenticateHeader);
        }
    }

    /**
     * Generates an authorisation header in response to wwwAuthHeader.
     *
     * @param method method of the request being authenticated
     * @param uri digest-uri
     * @param requestBody the body of the request.
     * @param authHeader the challenge that we should respond to
     * @param username
     * @param password
     *
     * @return an authorisation header in response to authHeader.
     *
     * @throws OperationFailedException if auth header was malformated.
    */
    @Override
    public AuthorizationHeader getAuthorizationHeader(
                String                method,
                String                uri,
                String                requestBody,
                Header                authHeader,
                String                username,
                String                password,
                String                nonce,
                int                   nc){
        String response = null;
        boolean isResponseHeader = true;
        WWWAuthenticateHeader wwwAuthenticateHeader = null;
        if(authHeader instanceof WWWAuthenticateHeader) {
            wwwAuthenticateHeader = (WWWAuthenticateHeader) authHeader;
        }

        AuthorizationHeader authorizationHeader = null;
        if(authHeader instanceof AuthorizationHeader) {
            authorizationHeader =  (AuthorizationHeader) authHeader;
            isResponseHeader = false;
        }

        // JvB: authHeader.getQop() is a quoted _list_ of qop values
        // (e.g. "auth,auth-int") Client is supposed to pick one
        String qopList = null;
        if(isResponseHeader) {
            qopList = wwwAuthenticateHeader.getQop();
        } else {
            qopList = authorizationHeader.getQop();
        }
        String algorithm = null;
        if(isResponseHeader) {
            algorithm = wwwAuthenticateHeader.getAlgorithm();
        } else {
            algorithm = authorizationHeader.getAlgorithm();
        }
        String realm = null;
        if(isResponseHeader) {
            realm = wwwAuthenticateHeader.getRealm();
        } else {
            realm = authorizationHeader.getRealm();
        }
        String scheme = null;
        if(isResponseHeader) {
            scheme = wwwAuthenticateHeader.getScheme();
        } else {
            scheme = authorizationHeader.getScheme();
        }
        String opaque = null;
        if(isResponseHeader) {
            opaque = wwwAuthenticateHeader.getOpaque();
        } else {
            opaque = authorizationHeader.getOpaque();
        }
        String qop = (qopList != null) ? "auth" : null;
        //String nc_value = "00000001";
        String nc_value = String.format("%08x", nc);
        //String cnonce = "xyz";
        long currentTime = System.currentTimeMillis();
        String nOnceValue = currentTime + ":" + "mobicents" + response;
        byte[] buffer = null;
        synchronized (md5Helper) {
            buffer = md5Helper.digest(nOnceValue.getBytes());
        }
        String cnonce = MD5_ENCODER.encode(buffer);

        try {
            response = MessageDigestResponseAlgorithm.calculateResponse(
                algorithm,
                username,
                realm,
                password,
                nonce,
                nc_value, // JvB added
                cnonce,   // JvB added
                method,
                uri,
                requestBody,
                qop);//jvb changed
        } catch (NullPointerException exc) {
            throw new IllegalStateException(
                "The authenticate header was malformatted", exc);
        }

        AuthorizationHeader authorization = null;
        try {
            if (authHeader instanceof ProxyAuthenticateHeader || authHeader instanceof ProxyAuthorizationHeader) {
                authorization = headerFactory.createProxyAuthorizationHeader(scheme);
            } else {
                authorization = headerFactory.createAuthorizationHeader(scheme);
            }

            authorization.setUsername(username);
            authorization.setRealm(realm);
            authorization.setNonce(nonce);
            authorization.setParameter("uri", uri);
            authorization.setResponse(response);
            if (algorithm != null) {
                authorization.setAlgorithm(algorithm);
            }

            if (opaque != null && opaque.length() > 0) {
                authorization.setOpaque(opaque);
            }

            // jvb added
            if (qop!=null) {
                authorization.setQop(qop);
                authorization.setCNonce(cnonce);
                authorization.setNonceCount( Integer.parseInt(nc_value, 16) );
            }

            authorization.setResponse(response);

        }
        catch (ParseException ex) {
            throw new SecurityException(
                "Failed to create an authorization header!", ex);
        }

        return authorization;
    }

    public SipPrincipal getPrincipal() {
        return principal;
    }

    /**
     * Return the MessageDigest object to be used for calculating
     * session identifiers.  If none has been created yet, initialize
     * one the first time this method is called.
     */
    protected synchronized MessageDigest getDigest() {
        if (this.digest == null) {
            try {
                this.digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                try {
                    this.digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException f) {
                    this.digest = null;
                }
            }
        }
        return (this.digest);
    }

    /**
     * Return the random number generator instance we should use for
     * generating session identifiers.  If there is no such generator
     * currently defined, construct and seed a new one.
     */
    protected synchronized Random getRandom() {
        if (this.random == null) {
            try {
                Class clazz = Class.forName(randomClass);
                this.random = (Random) clazz.newInstance();
                long seed = System.currentTimeMillis();
                char entropy[] = getEntropy().toCharArray();
                for (int i = 0; i < entropy.length; i++) {
                    long update = ((byte) entropy[i]) << ((i % 8) * 8);
                    seed ^= update;
                }
                this.random.setSeed(seed);
            } catch (Exception e) {
                this.random = new java.util.Random();
            }
        }
        return (this.random);
    }

    /**
     * Register an authenticated Principal and authentication type in our
     * request, in the current session (if there is one), and with our
     * SingleSignOn valve, if there is one.  Set the appropriate cookie
     * to be returned.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are generating
     * @param principal The authenticated Principal to be registered
     * @param authType The authentication type to be registered
     * @param username Username used to authenticate (if any)
     * @param password Password used to authenticate (if any)
     */
    protected void register(MobicentsSipServletRequest request, MobicentsSipServletResponse response,
            SipPrincipal principal, String authType, String username, String password) {

        if (log.isDebugEnabled()) {
            // Bugzilla 39255: http://issues.apache.org/bugzilla/show_bug.cgi?id=39255
            String name = (principal == null) ? "none" : principal.getName();
            log.debug("Authenticated '" + name + "' with type '" + authType + "'");
        }
        request.setUserPrincipal(principal);
        MobicentsSipSession session = request.getSipSession();
        if (session != null) {
            session.setUserPrincipal(principal);
        }
    }

    // ------------------------------------------------------------- Properties

    /**
     * Return the message digest algorithm for this Manager.
     */
    public String getAlgorithm() {
        return (this.algorithm);
    }

    /**
     * Set the message digest algorithm for this Manager.
     *
     * @param algorithm The new message digest algorithm
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Return the cache authenticated Principals flag.
     */
    public boolean getCache() {
        return (this.cache);
    }

    /**
     * Set the cache authenticated Principals flag.
     *
     * @param cache The new cache flag
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

    /**
     * Return the entropy increaser value, or compute a semi-useful value
     * if this String has not yet been set.
     */
    public String getEntropy() {
        // Calculate a semi-useful value if this has not been set
        if (this.entropy == null){
            setEntropy(this.toString());
        }
        return (this.entropy);
    }

    /**
     * Set the entropy increaser value.
     *
     * @param entropy The new entropy increaser value
     */
    public void setEntropy(String entropy) {
        this.entropy = entropy;
    }

    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (INFO);
    }

    /**
     * Return the random number generator class name.
     */
    public String getRandomClass() {
        return (this.randomClass);
    }

    /**
     * Set the random number generator class name.
     *
     * @param randomClass The new random number generator class name
     */
    public void setRandomClass(String randomClass) {
        this.randomClass = randomClass;
    }

    public SipContext getContext() {
        return context;
    }

    public void setContext(SipContext context) {
        this.context = context;
    }
}