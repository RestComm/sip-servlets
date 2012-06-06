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

package org.mobicents.servlet.sip.catalina.security.authentication;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;

import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.MD5Encoder;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.catalina.SipLoginConfig;
import org.mobicents.servlet.sip.catalina.security.CatalinaSipPrincipal;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipDigestAuthenticator;
import org.mobicents.servlet.sip.core.security.SipPrincipal;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of HTTP DIGEST
 * Authentication (see RFC 2069). Modified for SIP authentication.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @author Vladimir Ralev
 */

public class DigestAuthenticator
    extends AuthenticatorBase implements SipDigestAuthenticator {
	private static final Logger log = Logger.getLogger(DigestAuthenticator.class);


    // -------------------------------------------------------------- Constants

    /**
     * The MD5 helper object for this class.
     */
    static final MD5Encoder MD5_ECNODER = new MD5Encoder();


    /**
     * Descriptive information about this implementation.
     */
    protected static final String INFO =
        "org.apache.catalina.authenticator.DigestAuthenticator/1.0";


	private HeaderFactory headerFactory;


    // ----------------------------------------------------------- Constructors


    public DigestAuthenticator(HeaderFactory headerFactory) {
        super();
        this.headerFactory = headerFactory;
    }


    // ----------------------------------------------------- Instance Variables


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
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (INFO);

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
     * @param config    Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(MobicentsSipServletRequest request,
    							MobicentsSipServletResponse response,
                                MobicentsSipLoginConfig config)
        throws IOException {
    	
    	principal = null;
    	
        // Have we already authenticated someone?
        principal = request.getUserPrincipal();
        //String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            if (log.isDebugEnabled())
                log.debug("Already authenticated '" + principal.getName() + "'");

            return (true);
        }

        // Validate any credentials already included with this request
        String authorization = request.getHeader("authorization");
        if (authorization != null) {
            principal = findPrincipal(request, authorization, context.getRealm());
            if (principal != null &&
            		// fix for http://code.google.com/p/sipservlets/issues/detail?id=88
            		principal.getPrincipal() != null) {
                String username = parseUsername(authorization);
                register(request, response, principal,
                         "DIGEST",//Constants.DIGEST_METHOD,
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
        //      hres.flushBuffer();
        return (false);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse the specified authorization credentials, and return the
     * associated Principal that these credentials authenticate (if any)
     * from the specified Realm.  If there is no such Principal, return
     * <code>null</code>.
     *
     * @param request HTTP servlet request
     * @param authorization Authorization credentials from this request
     * @param realm Realm used to authenticate Principals
     */
    protected static SipPrincipal findPrincipal(MobicentsSipServletRequest request,
                                             String authorization,
                                             Realm realm) {

        //System.out.println("Authorization token : " + authorization);
        // Validate the authorization credentials format    	
        if (authorization == null)
            return (null);
        if (!authorization.startsWith("Digest "))
            return (null);
        String tmpAuthorization = authorization.substring(7).trim();

        // Bugzilla 37132: http://issues.apache.org/bugzilla/show_bug.cgi?id=37132
        // The solution of 37132 doesn't work with : response="2d05f1206becab904c1f311f205b405b",cnonce="5644k1k670",username="admin",nc=00000001,qop=auth,nonce="b6c73ab509830b8c0897984f6b0526e8",realm="sip-servlets-realm",opaque="9ed6d115d11f505f9ee20f6a68654cc2",uri="sip:192.168.1.142",algorithm=MD5
        // That's why I am going back to simple comma (Vladimir). TODO: Review this.
        String[] tokens = tmpAuthorization.split(",");//(?=(?:[^\"]*\"[^\"]*\")+$)");

        String userName = null;
        String realmName = null;
        String nOnce = null;
        String nc = null;
        String cnonce = null;
        String qop = null;
        String uri = null;
        String response = null;
        String method = request.getMethod();

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            if (currentToken.length() == 0)
                continue;

            int equalSign = currentToken.indexOf('=');
            if (equalSign < 0)
                return null;
            String currentTokenName =
                currentToken.substring(0, equalSign).trim();
            String currentTokenValue =
                currentToken.substring(equalSign + 1).trim();
            if ("username".equals(currentTokenName))
                userName = removeQuotes(currentTokenValue);
            if ("realm".equals(currentTokenName))
                realmName = removeQuotes(currentTokenValue, true);
            if ("nonce".equals(currentTokenName))
                nOnce = removeQuotes(currentTokenValue);
            if ("nc".equals(currentTokenName))
                nc = removeQuotes(currentTokenValue);
            if ("cnonce".equals(currentTokenName))
                cnonce = removeQuotes(currentTokenValue);
            if ("qop".equals(currentTokenName))
                qop = removeQuotes(currentTokenValue);
            if ("uri".equals(currentTokenName))
                uri = removeQuotes(currentTokenValue);
            if ("response".equals(currentTokenName))
                response = removeQuotes(currentTokenValue);
        }

        if ( (userName == null) || (realmName == null) || (nOnce == null)
             || (uri == null) || (response == null) )
            return null;

        // Second MD5 digest used to calculate the digest :
        // MD5(Method + ":" + uri)
        String a2 = method + ":" + uri;
        //System.out.println("A2:" + a2);

        byte[] buffer = null;
        synchronized (md5Helper) {
            buffer = md5Helper.digest(a2.getBytes());
        }
        String md5a2 = MD5_ECNODER.encode(buffer);

        return (new CatalinaSipPrincipal(realm.authenticate(userName, response, nOnce, nc, cnonce, qop,
                                   realmName, md5a2)));

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
        nOnceValue = MD5_ECNODER.encode(buffer);

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
            + MD5_ECNODER.encode(buffer) + "\"";
        
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
    public AuthorizationHeader getAuthorizationHeader(
                String                method,
                String                uri,
                String                requestBody,
                Header 				  authHeader,
                String       		  username,
                String 				  password,
                String 				  nonce,
                int 				  nc)
    {
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
        String cnonce = MD5_ECNODER.encode(buffer);

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

	@Override
	public void logout(Request arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean authenticate(Request request, HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(Request request, String username, String password)
			throws ServletException {
		// TODO Auto-generated method stub
		
	}


}
